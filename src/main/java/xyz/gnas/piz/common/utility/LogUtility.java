package xyz.gnas.piz.common.utility;

import static org.slf4j.LoggerFactory.getLogger;
import static xyz.gnas.piz.common.utility.runner.RunnerUtility.executeVoidAndExceptionRunner;

/**
 * Provides standard ways to write logs
 */
public class LogUtility {
    /**
     * Write error log.
     *
     * @param callerClass  the caller class
     * @param message      the message
     * @param e            the exception
     */
    public static void writeErrorLog(Class callerClass, String message, Throwable e) {
        executeVoidAndExceptionRunner(() -> getLogger(callerClass).error(message, e),
                ex -> System.out.println("Error writing error log " + ex.toString()));
    }

    /**
     * Write info log.
     *
     * @param callerClass the caller class
     * @param log         the log
     */
    public static void writeInfoLog(Class callerClass, String log) {
        executeVoidAndExceptionRunner(() -> getLogger(callerClass).info(log),
                e -> System.out.println("Error writing info log " + e.toString()));
    }
}
