package xyz.gnas.piz.app.common.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtility {
    /**
     * Write error log.
     *
     * @param callingClass the calling class
     * @param message      the message
     * @param e            the exception
     */
    public static void writeErrorLog(Class callingClass, String message, Throwable e) {
        try {
            Logger logger = LoggerFactory.getLogger(callingClass);
            logger.error(message, e);
        } catch (Exception ex) {
            System.out.println("Error writing error log");
        }
    }

    /**
     * Write info log.
     *
     * @param callingClass the calling class
     * @param log          the log
     */
    public static void writeInfoLog(Class callingClass, String log) {
        try {
            Logger logger = LoggerFactory.getLogger(callingClass);
            logger.info(log);
        } catch (Exception ex) {
            System.out.println("Error writing info log");
        }
    }
}
