package xyz.gnas.piz;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import xyz.gnas.piz.common.CommonUtility;
import xyz.gnas.piz.common.ResourceManager;
import xyz.gnas.piz.controllers.AppController;

public class Main extends Application {
	@Override
	public void start(Stage stage) {
		try {
			FXMLLoader loader = new FXMLLoader(ResourceManager.getAppFXML());
			Scene scene = new Scene((Parent) loader.load());
			AppController controlller = loader.getController();
			controlller.setStage(stage);
			controlller.initialiseTabs();
			scene.getStylesheets().add(ResourceManager.getCss());
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