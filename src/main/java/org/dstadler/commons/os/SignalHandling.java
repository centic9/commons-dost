package org.dstadler.commons.os;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SignalHandling {
    private static final Logger log = Logger.getLogger(SignalHandling.class.getName());

    public static void setShutdownHook(Consumer<Object> shutdownHook) {
        try {
            /*
            This stopped working in Java 11:

            SignalHandler shutdownHandler = sig -> {
                log.info("Stopping on " + sig);
                piRadioManager.stopAllThreads();
            };
            Signal.handle(new Signal("INT"), shutdownHandler);
            Signal.handle(new Signal("TERM"), shutdownHandler);
            */

            // there is jdk.internal.misc.Signal and sun.misc.Signal with slightly different
            // implementation of the signal-handler interface!
            try {
                // first try to use the version from "sun.misc"
                installSignalHandler(shutdownHook, "sun.misc", "SignalHandler");
            } catch (Exception e) {
                log.info("Failed to install signal-handler from 'sun.misc', trying with 'jdk.internal.misc': " + e);

                // if that one failed, let's try with version from "jdk.internal.misc"
                installSignalHandler(shutdownHook, "jdk.internal.misc", "Signal$Handler");
            }

            return;
        } catch (Exception e) {
            log.log(Level.WARNING, "Could not install SignalHandler, using shutdownHook instead: " + e);
        }

        log.info("Could not install Signal-Handler, thus using shutdown-hook instead");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdownHook.accept("hook")));
    }

    private static void installSignalHandler(Consumer<Object> shutdownHook, String packageName, String handlerName) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> signalHandlerClazz = Class.forName(packageName + "." + handlerName);
        Class<?> signalClazz = Class.forName(packageName + ".Signal");
        Method handleMethod = signalClazz.getMethod("handle", signalClazz, signalHandlerClazz);

        //noinspection SuspiciousInvocationHandlerImplementation
        Object handlerProxy = Proxy.newProxyInstance(signalHandlerClazz.getClassLoader(),
                new Class[]{signalHandlerClazz}, (proxy, method, args) -> {
                    shutdownHook.accept(args[0]);
                    return null;
                });

        Constructor<?> constructor = signalClazz.getConstructor(String.class);
        handleMethod.invoke(null, constructor.newInstance("INT"), handlerProxy);
        handleMethod.invoke(null, constructor.newInstance("TERM"), handlerProxy);

        log.info("Installed signal-handler from '" + packageName + "' via reflection");
    }
}
