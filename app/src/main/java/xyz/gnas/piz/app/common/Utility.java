package xyz.gnas.piz.app.common;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static javafx.application.Platform.runLater;

/**
 * Contains common methods used in the application
 */
public final class Utility {
    public static void showError(Class callingClass, Throwable e, String message, boolean exit) {
        runLater(() -> {
            try {
                // Get stack trace as string
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String stackTrace = sw.toString();

                GridPane expContent = getExpandableContent(stackTrace);

                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("An error has occurred!");
                alert.setContentText(message + ". See details below");
                alert.getDialogPane().setExpandableContent(expContent);
                alert.showAndWait();

                writeErrorLog(callingClass, message, e);
            } catch (Exception ex) {
                writeErrorLog(Utility.class, "Could not display error", ex);
            }

            if (exit) {
                System.exit(1);
            }
        });
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

    public static void writeErrorLog(Class callingClass, String message, Throwable e) {
        try {
            Logger logger = LoggerFactory.getLogger(callingClass);
            logger.error(message, e);
        } catch (Exception ex) {
            System.out.println("Error writing error log");
        }
    }

    public static void writeInfoLog(Class callingClass, String log) {
        try {
            Logger logger = LoggerFactory.getLogger(callingClass);
            logger.info(log);
        } catch (Exception ex) {
            System.out.println("Error writing info log");
        }
    }

    public static void showAlert(String headerText, String message) {
        runLater(() -> {
            try {
                Alert alert = new Alert(AlertType.NONE);
                alert.setTitle("Message");
                alert.setHeaderText(headerText);
                alert.setContentText(message);
                alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
                alert.showAndWait();
            } catch (Exception ex) {
                writeErrorLog(Utility.class, "Could not display alert", ex);
            }
        });
    }

    public static boolean showConfirmation(String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Please confirm this action");
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static Calendar convertLocalDateTimeToCalendar(LocalDateTime ldt) {
        Calendar c = Calendar.getInstance();
        Date dt = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        c.setTime(dt);
        return c;
    }

    public static LocalDateTime convertCalendarToLocalDateTime(Calendar c) {
        return LocalDateTime.ofInstant(c.getTime().toInstant(), ZoneId.systemDefault());
    }
}