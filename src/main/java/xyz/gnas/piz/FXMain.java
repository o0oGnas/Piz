package main.java.xyz.gnas.piz;

import org.greenrobot.eventbus.EventBus;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import main.java.xyz.gnas.piz.common.Utility;
import main.java.xyz.gnas.piz.common.ResourceManager;
import main.java.xyz.gnas.piz.events.ExitEvent;

public class FXMain extends Application {
	@Override
	public void start(Stage stage) {
		try {
			stage.setOnCloseRequest((WindowEvent arg0) -> {
				// raise exit event
				EventBus.getDefault().post(new ExitEvent(arg0));
			});

			FXMLLoader loader = new FXMLLoader(ResourceManager.getAppFXML());
			Scene scene = new Scene((Parent) loader.load());
			scene.getStylesheets().addAll(ResourceManager.getCSSList());
			stage.setScene(scene);
			stage.setTitle("Piz");
			stage.getIcons().add(ResourceManager.getAppIcon());
			stage.show();
		} catch (Exception e) {
			Utility.showError(getClass(), e, "Could not start the application", true);
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}