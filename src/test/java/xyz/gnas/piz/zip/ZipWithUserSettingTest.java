package test.java.xyz.gnas.piz.zip;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import main.java.xyz.gnas.piz.common.ResourceManager;
import main.java.xyz.gnas.piz.controllers.AppController;

@ExtendWith(ApplicationExtension.class)
public class ZipWithUserSettingTest {
	@Start
	void onStart(Stage stage) throws IOException {
		FXMLLoader loader = new FXMLLoader(ResourceManager.getAppFXML());
		Scene scene = new Scene((Parent) loader.load());
		AppController controlller = loader.getController();
		controlller.setStage(stage);
		controlller.initialiseTabs();
		scene.getStylesheets().add(ResourceManager.getAppCSS());
		stage.setScene(scene);
		stage.setTitle("Piz");
		stage.getIcons().add(ResourceManager.getAppIcon());
		stage.show();
	}

	private

	@AfterEach void afterEach(TestInfo info) {
		if (info.getTags().contains("cleanUp")) {

		}
	}

	@Test
	void password_is_masked(FxRobot robot) {
		assertThat(robot.lookup(a -> a.getId() != null && a.getId().equalsIgnoreCase("pwfPassword"))
				.queryAs(PasswordField.class).visibleProperty().get());
	}
}
