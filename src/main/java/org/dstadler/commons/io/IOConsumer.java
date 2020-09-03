package org.dstadler.commons.io;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * An extension of {@link Consumer} which allows
 * {@link IOException} to be thrown and wraps them into
 * {@link IllegalStateException} so that exception
 * handling inside the consumer can be simplified and
 * the caller can handle the exception if necessary.
 *
 * You can use an instance of this class instead of a normal
 * Consumer whenever the accept method may throw an
 * IOExceptions.
 *
 * You will simply need to implement method "acceptWithException"
 * instead of accept().
 *
 * If using lambda functions, you can simplify the call to:
 *
 * <code>new Hosts.IOConsumer() {
     {@literal @}Override
     public void acceptWithException(HttpEntity entity) throws IOException {
         log.info("Had result for create: " +
                 (entity != null ? IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8) : "&lt;empty&gt;"));
     }
 }</code>
 */
public abstract class IOConsumer<T> implements Consumer<T> {
    @Override
    public final void accept(T input) {
        try {
            acceptWithException(input);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public abstract void acceptWithException(T input) throws IOException;
}
