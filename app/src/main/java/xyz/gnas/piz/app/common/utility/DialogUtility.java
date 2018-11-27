package xyz.gnas.piz.app.common.utility;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import static javafx.application.Platform.runLater;

/**
 * Contains common methods used in the application
 */
public final class DialogUtility {
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
        runLater(() -> {
            try {
                String stackTrace = getStrackTrace(e);
                GridPane expContent = getExpandableContent(stackTrace);
                Alert alert = new Alert(AlertType.ERROR);
                initialiseAlert(alert, "Error", "An error has occurred!", message + ". See details below");
                alert.getDialogPane().setExpandableContent(expContent);
                alert.showAndWait();
                writeErrorLog(callingClass, message, e);
            } catch (Exception ex) {
                writeErrorLog(DialogUtility.class, "Could not display error", ex);
            }

            if (exit) {
                System.exit(1);
            }
        });
    }

    private static String getStrackTrace(Throwable e) throws IOException {
        try (StringWriter sw = new StringWriter()) {
            try (PrintWriter pw = new PrintWriter(sw)) {
                e.printStackTrace(pw);
                return sw.toString();
            }
        }
    }

    private static void initialiseAlert(Alert alert, String title, String headerText, String contentText) {
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
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

    /**
     * Show alert dialog
     *
     * @param headerText the header text
     * @param message    the message
     */
    public static void showAlert(String headerText, String message) {
        runLater(() -> {
            try {
                Alert alert = new Alert(AlertType.NONE);
                initialiseAlert(alert, "Message", headerText, message);
                alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
                alert.showAndWait();
            } catch (Exception ex) {
                writeErrorLog(DialogUtility.class, "Could not display alert", ex);
            }
        });
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