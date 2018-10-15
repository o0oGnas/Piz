package test.java.xyz.gnas.piz.zip;

import java.io.IOException;

import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
		stage.show();
	}
}