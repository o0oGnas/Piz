package xyz.gnas.piz.app.common.utility;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import xyz.gnas.piz.app.common.utility.code.ExceptionHandler;
import xyz.gnas.piz.app.common.utility.code.MainThreadTaskRunner;
import xyz.gnas.piz.app.common.utility.code.Runner;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import static javafx.application.Platform.runLater;

public final class DialogUtility {
    private static void runInMainThread(Runner runner, ExceptionHandler handler) {
        runLater(new MainThreadTaskRunner(runner, handler));
    }

    private static void writeErrorLog(String message, Throwable e) {
        LogUtility.writeErrorLog(DialogUtility.class, message, e);
    }

    /**
     * Show error message with the exception stack trace and write a log, additionally exit the application if exit
     * flag is true
     *
     * @param callingClass the calling class
     * @param message      the message to display to user
     * @param e            the exception
     * @param exit         flag to exit the application
     */
    public static void showError(Class callingClass, String message, Throwable e, boolean exit) {
        runInMainThread(() -> {
            String stackTrace = getStackTrace(e);
            GridPane expContent = getExpandableContent(stackTrace);
            Alert alert = new Alert(AlertType.ERROR);
            initialiseAlert(alert, "Error", "An error has occurred!", message + ". See details below");
            alert.getDialogPane().setExpandableContent(expContent);
            alert.showAndWait();
            LogUtility.writeErrorLog(callingClass, message, e);
        }, (Exception ex) -> writeErrorLog("Could not display error", ex));
    }

    private static String getStackTrace(Throwable e) throws IOException {
        try (StringWriter sw = new StringWriter()) {
            try (PrintWriter pw = new PrintWriter(sw)) {
                e.printStackTrace(pw);
                return sw.toString();
            }
        }
    }

    private static GridPane getExpandableContent(String sStackTrace) {
        TextArea textArea = new TextArea(sStackTrace);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);
        return expContent;
    }

    private static void initialiseAlert(Alert alert, String title, String headerText, String contentText) {
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
    }

    /**
     * Show alert dialog
     *
     * @param headerText the header text
     * @param message    the message
     */
    public static void showAlert(String headerText, String message) {
        runInMainThread(() -> {
            Alert alert = new Alert(AlertType.NONE);
            initialiseAlert(alert, "Message", headerText, message);
            alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
            alert.showAndWait();
        }, (Exception e) -> writeErrorLog("Could not display alert", e));
    }

    /**
     * Show confirmation dialog, must be called in main thread
     *
     * @param message the message
     * @return the boolean result - true is OK, false is cancel
     */
    public static boolean showConfirmation(String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        initialiseAlert(alert, "Confirmation", "Please confirm this action", message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}