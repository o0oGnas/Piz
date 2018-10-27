package xyz.gnas.piz.app;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.controlsfx.control.CheckComboBox;
import org.testfx.api.FxRobot;
import org.testfx.service.query.NodeQuery;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import xyz.gnas.piz.app.common.ResourceManager;
import tornadofx.control.DateTimePicker;

/**
 * @author ADMIN
 * @date Oct 16, 2018
 * @description handles all UI control searching
 */
public class TestUtility {
	public static void initialiseStage(Stage stage) throws FileNotFoundException, IOException {
		FXMLLoader loader = new FXMLLoader(ResourceManager.getAppFXML());
		Scene scene = new Scene((Parent) loader.load());
		scene.getStylesheets().addAll(ResourceManager.getCSSList());
		stage.setScene(scene);
		stage.setTitle("Piz");
		stage.getIcons().add(ResourceManager.getAppIcon());
		stage.show();
	}

	public static Label getLabel(FxRobot robot, String id) {
		return getNodeQueryByID(robot, id).queryAs(Label.class);
	}

	public static NodeQuery getNodeQueryByID(FxRobot robot, String id) {
		return robot.lookup(a -> a.getId() != null && a.getId().equalsIgnoreCase(id));
	}

	public static CheckComboBox getCheckComboBox(FxRobot robot, String id) {
		return getNodeQueryByID(robot, id).queryAs(CheckComboBox.class);
	}

	public static CheckBox getCheckBox(FxRobot robot, String id) {
		return getNodeQueryByID(robot, id).queryAs(CheckBox.class);
	}

	public static HBox getHBox(FxRobot robot, String id) {
		return getNodeQueryByID(robot, id).queryAs(HBox.class);
	}

	public static PasswordField getPasswordField(FxRobot robot, String id) {
		return getNodeQueryByID(robot, id).queryAs(PasswordField.class);
	}

	public static TextField getTextField(FxRobot robot, String id) {
		return getNodeQueryByID(robot, id).queryAs(TextField.class);
	}

	public static ImageView getImageView(FxRobot robot, String id) {
		return getNodeQueryByID(robot, id).queryAs(ImageView.class);
	}

	public static Button getButtonByID(FxRobot robot, String id) {
		return getNodeQueryByID(robot, id).queryAs(Button.class);
	}

	public static Button getButtonByText(FxRobot robot, String text) {
		return robot.lookup(a -> a instanceof Button && ((Button) a).getText().equalsIgnoreCase(text))
				.queryAs(Button.class);
	}

	public static DateTimePicker getDateTimePicker(FxRobot robot, String id) {
		return getNodeQueryByID(robot, id).queryAs(DateTimePicker.class);
	}

	public static ComboBox getComBoBox(FxRobot robot, String id) {
		return getNodeQueryByID(robot, id).queryAs(ComboBox.class);
	}

	public static TableView getTableView(FxRobot robot, String id) {
		return getNodeQueryByID(robot, id).queryAs(TableView.class);
	}
}