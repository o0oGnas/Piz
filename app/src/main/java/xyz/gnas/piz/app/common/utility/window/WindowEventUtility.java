package xyz.gnas.piz.app.common.utility.window;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import xyz.gnas.piz.app.common.utility.code.CodeRunnerUtility;
import xyz.gnas.piz.app.common.utility.code.Runner;

public class WindowEventUtility {
    private static void executeRunner(Class callerClass, String errorMessage, Runner runner) {
        CodeRunnerUtility.executeRunner(callerClass, errorMessage, runner);
    }

    /**
     * Bind window event handler.
     *
     * @param callerClass the caller class
     * @param node        a control in the calling class to get its window
     * @param handler     the handler
     */
    public static void bindWindowEventHandler(Class callerClass, Node node, WindowEventHandler handler) {
        bindSceneListener(callerClass, node, handler);
        node.sceneProperty().addListener(l -> executeRunner(callerClass, "Error when handling scene change event",
                () -> bindSceneListener(callerClass, node, handler)));
    }

    private static void bindSceneListener(Class callerClass, Node node, WindowEventHandler handler) {
        Scene scene = node.getScene();

        if (scene != null) {
            bindWindowListener(callerClass, scene, handler);
            scene.windowProperty().addListener(
                    l -> executeRunner(callerClass, "Error when handling window change event",
                            () -> bindWindowListener(callerClass, scene, handler)));
        }
    }

    private static void bindWindowListener(Class callerClass, Scene scene, WindowEventHandler handler) {
        Window window = scene.getWindow();

        if (window != null) {
            window.setOnCloseRequest((WindowEvent windowEvent) ->
                    executeRunner(callerClass, "Error when handling window close event",
                            () -> handler.handleCloseEvent(windowEvent)));
        }
    }
}
