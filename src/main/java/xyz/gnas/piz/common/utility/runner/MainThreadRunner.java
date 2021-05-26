package xyz.gnas.piz.common.utility.runner;

import xyz.gnas.piz.common.utility.DialogUtility;

/**
 * Runnable class to use in Platform.runLater
 */
class MainThreadRunner implements Runnable {
    private Class callerClass;

    private String errorMessage;

    private VoidRunner runner;

    private ExceptionRunner exceptionHandler;

    /**
     * MainThreadRunner without ExceptionRunner
     *
     * @param callerClass  the caller class
     * @param errorMessage the error message
     * @param runner       the runner
     */
    public MainThreadRunner(Class callerClass, String errorMessage, VoidRunner runner) {
        this.callerClass = callerClass;
        this.errorMessage = errorMessage;
        this.runner = runner;
    }

    /**
     * MainThreadRunner with ExceptionRunner
     *
     * @param runner           the runner
     * @param exceptionHandler the exception handler
     */
    public MainThreadRunner(VoidRunner runner, ExceptionRunner exceptionHandler) {
        this.runner = runner;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void run() {
        try {
            runner.run();
        } catch (Exception e) {
            if (exceptionHandler == null) {
                DialogUtility.showError(callerClass, errorMessage, e, false);
            } else {
                exceptionHandler.run(e);
            }
        }
    }
}
