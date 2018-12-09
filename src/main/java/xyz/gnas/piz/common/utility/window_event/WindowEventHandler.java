package xyz.gnas.piz.common.utility.window_event;

import javafx.stage.WindowEvent;

/**
 * Interface used by WindowEventUtility for handling window events
 */
public interface WindowEventHandler {
    /**
     * Handle close event.
     *
     * @param windowEvent the window event
     */
    void handleCloseEvent(WindowEvent windowEvent);
}