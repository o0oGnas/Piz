package xyz.gnas.piz.common.utility.runner;

import xyz.gnas.piz.common.utility.DialogUtility;

import static javafx.application.Platform.runLater;

/**
 * Provides wrapper to execute code, which either uses default exception handling or an explicit ExceptionRunner
 */
public class RunnerUtility {
    /**
     * Execute runner
     *
     * @param callerClass  the caller class
     * @param errorMessage the error message
     * @param runner       the runner
     */
    public static void executeVoidRunner(Class callerClass, String errorMessage, VoidRunner runner) {
        try {
            runner.run();
        } catch (Exception e) {
            DialogUtility.showError(callerClass, errorMessage, e, false);
        }
    }

    /**
     * Execute runner and exit if an exception is thrown
     *
     * @param callerClass  the caller class
     * @param errorMessage the error message
     * @param runner       the runner
     */
    public static void executeVoidRunnerOrExit(Class callerClass, String errorMessage, VoidRunner runner) {
        try {
            runner.run();
        } catch (Exception e) {
            DialogUtility.showError(callerClass, errorMessage, e, true);
        }
    }

    /**
     * Execute runner with custom exception handling
     *
     * @param runner  the runner
     * @param handler the exception handler
     */
    public static void executeVoidAndExceptionRunner(VoidRunner runner, ExceptionRunner handler) {
        try {
            runner.run();
        } catch (Exception e) {
            handler.run(e);
        }
    }

    /**
     * Run runner in a new thread
     *
     * @param callerClass  the caller class
     * @param errorMessage the error message
     * @param runner       the runner
     * @return the thread
     */
    public static Thread executeSideThreadRunner(Class callerClass, String errorMessage, VoidRunner runner) {
        Thread t = new Thread(new SideThreadRunner(callerClass, errorMessage, runner));
        t.start();
        return t;
    }

    /**
     * Run runner in the main thread
     *
     * @param callerClass  the caller class
     * @param errorMessage the error message
     * @param runner       the runner
     */
    public static void executeMainThreadRunner(Class callerClass, String errorMessage, VoidRunner runner) {
        runLater(new MainThreadRunner(callerClass, errorMessage, runner));
    }

    /**
     * Run runner in the main thread and handle exception.
     *
     * @param runner  the runner
     * @param handler the handler
     */
    public static void executeMainThreadAndExceptionRunner(VoidRunner runner, ExceptionRunner handler) {
        runLater(new MainThreadRunner(runner, handler));
    }
}
