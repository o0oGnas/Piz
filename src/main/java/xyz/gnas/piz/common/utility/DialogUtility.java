package xyz.gnas.piz.common.utility;

import static xyz.gnas.piz.common.utility.runner.RunnerUtility.executeMainThreadAndExceptionRunner;

import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Shows different types of dialog
 */
public final class DialogUtility {
	private static void writeErrorLog(String message, Throwable e) {
		LogUtility.writeErrorLog(DialogUtility.class, message, e);
	}

	/**
	 * Show error message with the exception stack trace and write a log,
	 * additionally exit the application if exit flag is true
	 *
	 * @param callingClass the calling class
	 * @param message      the message to display to user
	 * @param e            the exception
	 * @param exit         flag to exit the application
	 */
	public static void showError(Class callingClass, String message, Throwable e, boolean exit) {
		executeMainThreadAndExceptionRunner(() -> {
			String stackTrace = ExceptionUtils.getStackTrace(e);
			GridPane expContent = getExpandableContent(stackTrace);
			Alert alert = new Alert(AlertType.ERROR);
			initialiseAlert(alert, "Error", "An error has occurred!", message + ". See details below");
			alert.getDialogPane().setExpandableContent(expContent);
			alert.showAndWait();
			LogUtility.writeErrorLog(callingClass, message, e);
			checkExitFlagAndExit(exit);
		}, (Exception ex) -> {
			writeErrorLog("Could not display error", ex);
			checkExitFlagAndExit(exit);
		});
	}

	private static void checkExitFlagAndExit(boolean exit) {
		if (exit) {
			Platform.exit();
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
		GridPane result = new GridPane();
		result.setMaxWidth(Double.MAX_VALUE);
		result.add(textArea, 0, 0);
		return result;
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
	public static void showInformation(String headerText, String message) {
		executeMainThreadAndExceptionRunner(() -> {
			Alert alert = new Alert(AlertType.INFORMATION);
			initialiseAlert(alert, "Message", headerText, message);
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