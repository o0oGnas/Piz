package xyz.gnas.piz.common.utility.runner;

/**
 * Interface used for handling an exception
 */
public interface ExceptionRunner {
    /**
     * handle the exception
     *
     * @param e the exception
     */
    void run(Exception e);
}