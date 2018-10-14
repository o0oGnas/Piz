package main.java.xyz.gnas.piz;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.xyz.gnas.piz.common.CommonUtility;
import main.java.xyz.gnas.piz.common.ResourceManager;
import main.java.xyz.gnas.piz.controllers.AppController;

public class Main extends Application {
	@Override
	public void start(Stage stage) {
		try {
			FXMLLoader loader = new FXMLLoader(ResourceManager.getAppFXML());
			Scene scene = new Scene((Parent) loader.load());
			AppController controlller = loader.getController();
			controlller.setStage(stage);
			controlller.initialiseTabs();
			scene.getStylesheets().addAll(ResourceManager.getAppCSS());
			stage.setScene(scene);
			stage.setTitle("Piz");
			stage.getIcons().add(ResourceManager.getAppIcon());
			stage.show();
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not start the application", true);
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}