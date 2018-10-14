package main.java.xyz.gnas.piz.common;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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
	private static File errorFile = new File("error_log.txt");

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
		String stackTrace = sw.toString();

		// print stack trace to console
		System.out.println(stackTrace);

		GridPane expContent = getExpandableContent(stackTrace);
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText("An error has occurred!");
		alert.setContentText(message + ". See details below");
		alert.getDialogPane().setExpandableContent(expContent);
		alert.showAndWait();

		saveErrorTofile(new ErrorLog(message, stackTrace));

		if (exit) {
			System.exit(1);
		}
	}

	private static void saveErrorTofile(ErrorLog errorLog) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
			prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
			mapper.writeValue(errorFile, errorLog);
		} catch (Exception ex) {

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

	public static class ErrorLog {
		private Calendar date;

		private String errorMessage;
		private String stackTrace;

		public Calendar getDate() {
			return date;
		}

		public void setDate(Calendar date) {
			this.date = date;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

		public void setErrorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
		}

		public String getStackTrace() {
			return stackTrace;
		}

		public void setStackTrace(String stackTrace) {
			this.stackTrace = stackTrace;
		}

		public ErrorLog() {
		}

		public ErrorLog(String errorMessage, String stackTrace) {
			this.date = Calendar.getInstance();
			this.errorMessage = errorMessage;
			this.stackTrace = stackTrace;
		}
	}
}