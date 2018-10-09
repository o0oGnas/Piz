package xyz.gnas.piz.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * @author Gnas
 * @Description Contains common methods used in the application
 * @Date Oct 9, 2018
 */
public final class CommonUtility {
	/**
	 * @Description Show error dialog with exception stack trace in expandable
	 *              dialog
	 * @Date Oct 9, 2018
	 * @param e       The exception object
	 * @param message A useful message for the user
	 * @param exit    Flag to whether exit the application after showing the error
	 */
	public static void showError(Exception e, String message, boolean exit) {
		// Get stack trace as string
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String sStackTrace = sw.toString();
		System.out.println(sStackTrace);

		GridPane expContent = getExpandableContent(sStackTrace);
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText("An error has occurred!");
		alert.setContentText(message + ". See details below");
		alert.getDialogPane().setExpandableContent(expContent);
		alert.showAndWait();

		if (exit) {
			System.exit(1);
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

	/**
	 * @Description Show a message dialog
	 * @Date Oct 9, 2018
	 * @param headerText Short sentence describe the type of message
	 * @param message    Detailed message
	 */
	public static void showAlert(String headerText, String message) {
		Alert alert = new Alert(AlertType.NONE);
		alert.setTitle("Message");
		alert.setHeaderText(headerText);
		alert.setContentText(message);
		alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
		alert.showAndWait();
	}

	/**
	 * @Description show confirmation dialog
	 * @Date Oct 9, 2018
	 * @param message the displayed message
	 * @return confirmation result
	 */
	public static Optional<ButtonType> showConfirmation(String message) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmation");
		alert.setHeaderText("Please confirm this action");
		alert.setContentText(message);
		return alert.showAndWait();
	}
}