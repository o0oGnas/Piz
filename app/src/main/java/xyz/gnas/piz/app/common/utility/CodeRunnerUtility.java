package xyz.gnas.piz.app.common.utility;

import javafx.concurrent.Task;

import static xyz.gnas.piz.app.common.utility.DialogUtility.showError;

/**
 * Provides wrapper to execute code and catch exception, which is usually shown by calling DialogUtility.showError()
 */
public class CodeRunnerUtility {
    public interface Runner {
        void run() throws Exception;
    }

    public interface ExceptionHandler {
        void run(Exception e);
    }

    public static class SideThreadTaskRunner extends Task {
        private Class callerClass;
        private String errorMessage;
        private Runner runner;

        public SideThreadTaskRunner(Class callerClass, String errorMessage, Runner runner) {
            this.callerClass = callerClass;
            this.errorMessage = errorMessage;
            this.runner = runner;
        }

        @Override
        protected Object call() {
            try {
                runner.run();
            } catch (Exception e) {
                showError(callerClass, errorMessage, e, false);
            }

            return null;
        }
    }

    public static class MainThreadTaskRunner implements Runnable {
        private Class callerClass;
        private String errorMessage;
        private Runner runner;

        public MainThreadTaskRunner(Class callerClass, String errorMessage, Runner runner) {
            this.callerClass = callerClass;
            this.errorMessage = errorMessage;
            this.runner = runner;
        }

        @Override
        public void run() {
            try {
                runner.run();
            } catch (Exception e) {
                showError(callerClass, errorMessage, e, false);
            }
        }
    }

    /**
     * Execute runner
     *
     * @param callerClass  the caller class
     * @param errorMessage the error message
     * @param runner       the runner
     */
    public static void executeRunner(Class callerClass, String errorMessage, Runner runner) {
        try {
            runner.run();
        } catch (Exception e) {
            showError(callerClass, errorMessage, e, false);
        }
    }

    /**
     * Execute runner and exit if an exception is thrown
     *
     * @param callerClass  the caller class
     * @param errorMessage the error message
     * @param runner       the runner
     */
    public static void executeRunnerOrExit(Class callerClass, String errorMessage, Runner runner) {
        try {
            runner.run();
        } catch (Exception e) {
            showError(callerClass, errorMessage, e, true);
        }
    }

    /**
     * Execute runner with custom exception handling
     *
     * @param runner  the runner
     * @param handler the exception handler
     */
    public static void executeRunnerAndHandleException(Runner runner, ExceptionHandler handler) {

        try {
            runner.run();
        } catch (Exception e) {
            handler.run(e);
        }
    }
}
