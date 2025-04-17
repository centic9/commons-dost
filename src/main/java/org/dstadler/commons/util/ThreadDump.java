package org.dstadler.commons.util;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import org.apache.commons.lang3.SystemUtils;

/**
 * Creates a Thread Dump of all Java Threads
 */
public class ThreadDump {
	public static final String NEWLINE = System.lineSeparator();

	private static final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
	private final ThreadInfo [] infos;
	public static final String THREADDUMP_START =	"----- BEGIN THREAD DUMP -----";
	public static final String THREADDUMP_END =	"------ END THREAD DUMP ------";

	/**
	 * Creates a Thread Dump of all Java Threads
	 * @param  lockedMonitors if <code>true</code>, dump all locked monitors.
	 * @param  lockedSynchronizers if <code>true</code>, dump all locked
	 *             ownable synchronizers.
	 */
	public ThreadDump(boolean lockedMonitors, boolean lockedSynchronizers) {
		lockedMonitors &= bean.isObjectMonitorUsageSupported();
		lockedSynchronizers &= bean.isSynchronizerUsageSupported();
		infos = bean.dumpAllThreads(lockedMonitors, lockedSynchronizers);
	}

	/**
	 * Returns a String representation of the current Thread Dump
	 * @return a String representation of the current Thread Dump
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(NEWLINE);
		sb.append(THREADDUMP_START);
		sb.append(NEWLINE);
		sb.append("Full thread dump ").append(SystemUtils.JAVA_VERSION).append('(').append(SystemUtils.JAVA_VM_INFO).append("):");
		sb.append(NEWLINE);

		for (ThreadInfo info : infos) {
			sb.append(threadInfoToString(info));
		}

		sb.append(THREADDUMP_END);
		return sb.toString();
	}

	/**
	 * This method is a copy of {@link ThreadInfo#toString} without a limit
	 * on the stack trace depth.
	 * @param info the ThreadInfo object to stringify
	 * @return a string representation of the ThreadInfo object passed
	 */
	public static String threadInfoToString(ThreadInfo info) {
		StringBuilder sb = new StringBuilder("\"")
				.append(info.getThreadName())
				.append("\" #")
				.append(info.getThreadId())
				.append(" tid=")
				.append(toHexString(info.getThreadId()));

		final String lockName = info.getLockName();

		if (lockName != null) {
			String action = " waiting on condition";
			if (lockName.contains("Object.wait")) {
				action = " in Object.wait()";
			}
			sb.append(action);
		}
		sb.append(" [").append(toHexString(info.hashCode())).append("]\n");
		sb.append("   java.lang.Thread.State: ").append(info.getThreadState());
		if (info.getLockOwnerName() != null) {
			sb.append("(on lock owned by \"");
			sb.append(info.getLockOwnerName()).append("\" <").append(toHexString(info.getLockOwnerId())).append(">)");
		}
		if (info.isSuspended()) {
			sb.append(" (suspended)");
		}
		if (info.isInNative()) {
			sb.append(" (in native)");
		}
		sb.append('\n');

		StackTraceElement [] stackTrace = info.getStackTrace();
		for (int i = 0; i < stackTrace.length; i++) {
			StackTraceElement ste = stackTrace[i];
			sb.append("\tat ").append(ste);
			sb.append('\n');
			if (i == 0 && info.getLockInfo() != null) {
				Thread.State ts = info.getThreadState();
				LockInfo lockInfo = info.getLockInfo();
				long lockId = lockInfo.getIdentityHashCode();
				switch (ts) {
					case BLOCKED:
						sb.append("\t- waiting to lock ").append('<').append(toHexString(lockId)).append("> (a ").append(lockInfo.getClassName()).append(")\n");
						break;
					case WAITING:
					case TIMED_WAITING:
						sb.append("\t- parking to wait for ").append('<').append(toHexString(lockId)).append("> (a ").append(lockInfo.getClassName()).append(")\n");
						break;
					default:
				}
			}

			MonitorInfo [] lockedMonitors = info.getLockedMonitors();
			for (MonitorInfo mi : lockedMonitors) {
				if (mi.getLockedStackDepth() == i) {
					sb.append("\t- locked ").append('<').append(toHexString(mi.getIdentityHashCode())).append("> (a ").append(mi.getClassName()).append(")\n");
				}
			}
		}

		LockInfo [] locks = info.getLockedSynchronizers();
		if (locks.length > 0) {
			sb.append("\n\tNumber of locked synchronizers = ").append(locks.length);
			sb.append('\n');
			for (LockInfo li : locks) {
				sb.append("\t- ").append(li);
				sb.append('\n');
			}
		}
		sb.append('\n');
		return sb.toString();
	}

	/**
	 * @param value a {@code long} to be converted to a string
	 * @return a string representation of the {@code long} argument in hexadecimal that begins with
	 *         the radix indicator <code>0x</code> and is left padded with <code>0</code>
	 */
	public static String toHexString(final long value) {
		return "%#018x".formatted(value);
	}
}
