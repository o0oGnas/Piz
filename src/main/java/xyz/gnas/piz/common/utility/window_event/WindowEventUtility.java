package xyz.gnas.piz.common.utility.window_event;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import xyz.gnas.piz.common.utility.runner.RunnerUtility;
import xyz.gnas.piz.common.utility.runner.VoidRunner;

/**
 * Provides standard way to handle window events
 */
public class WindowEventUtility {
    private static void executeVoidRunner(Class callerClass, String errorMessage, VoidRunner runner) {
        RunnerUtility.executeVoidRunner(callerClass, errorMessage, runner);
    }

    /**
     * Bind a WindowEventHandler object to window events
     *
     * @param callerClass the caller class
     * @param node        a control in the calling class to get its window
     * @param handler     the handler
     */
    public static void bindWindowEventHandler(Class callerClass, Node node, WindowEventHandler handler) {
        bindSceneListener(callerClass, node, handler);
        node.sceneProperty().addListener(l -> executeVoidRunner(callerClass, "Error when handling scene change event",
                () -> bindSceneListener(callerClass, node, handler)));
    }

    private static void bindSceneListener(Class callerClass, Node node, WindowEventHandler handler) {
        Scene scene = node.getScene();

        if (scene != null) {
            bindWindowListener(callerClass, scene, handler);
            scene.windowProperty().addListener(
                    l -> executeVoidRunner(callerClass, "Error when handling window_event change event",
                            () -> bindWindowListener(callerClass, scene, handler)));
        }
    }

    private static void bindWindowListener(Class callerClass, Scene scene, WindowEventHandler handler) {
        Window window = scene.getWindow();

        if (window != null) {
            window.setOnCloseRequest((WindowEvent windowEvent) ->
                    executeVoidRunner(callerClass, "Error when handling window_event close event",
                            () -> handler.handleCloseEvent(windowEvent)));
        }
    }
}
