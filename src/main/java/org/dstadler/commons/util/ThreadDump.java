package org.dstadler.commons.util;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * Creates a Thread Dump of all Java Threads
 */
public class ThreadDump {
	private static ThreadMXBean bean = ManagementFactory.getThreadMXBean();
	private ThreadInfo [] infos = null;

	/**
	 * Creates a Thread Dump of all Java Threads
	 * @param  lockedMonitors if <tt>true</tt>, dump all locked monitors.
	 * @param  lockedSynchronizers if <tt>true</tt>, dump all locked
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
		StringBuffer sb = new StringBuffer();
		for (ThreadInfo info : infos) {
			sb.append(threadInfoToString(info));
		}
		return sb.toString();
	}

	/**
	 * This method is a copy of {@link ThreadInfo#toString} without a limit
	 * on the stack trace depth.
	 * @param info the ThreadInfo object to stringify
	 * @return a string representation of the ThreadInfo object passed
	 */
	private static String threadInfoToString(ThreadInfo info) {
		StringBuilder sb = new StringBuilder(100).append("\"").append(info.getThreadName()).append("\"")
				.append(" Id=").append(info.getThreadId()).append(" ").append(info.getThreadState());
		if (info.getLockName() != null) {
			sb.append(" on ").append(info.getLockName());
		}
		if (info.getLockOwnerName() != null) {
			sb.append(" owned by \"").append(info.getLockOwnerName()).append("\" Id=").append(info.getLockOwnerId());
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
			sb.append("\tat " + ste.toString());
			sb.append('\n');
			if (i == 0 && info.getLockInfo() != null) {
				Thread.State ts = info.getThreadState();
				switch (ts) {
				case BLOCKED:
					sb.append("\t-  blocked on ").append(info.getLockInfo());
					sb.append('\n');
					break;
				case WAITING:
					sb.append("\t-  waiting on ").append(info.getLockInfo());
					sb.append('\n');
					break;
				case TIMED_WAITING:
					sb.append("\t-  waiting on ").append(info.getLockInfo());
					sb.append('\n');
					break;
				default:
				}
			}

			MonitorInfo [] lockedMonitors = info.getLockedMonitors();
			for (MonitorInfo mi : lockedMonitors) {
				if (mi.getLockedStackDepth() == i) {
					sb.append("\t-  locked ").append(mi);
					sb.append('\n');
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
}
