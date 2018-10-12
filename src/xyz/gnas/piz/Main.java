package xyz.gnas.piz;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import xyz.gnas.piz.common.CommonUtility;
import xyz.gnas.piz.common.ResourceManager;

public class Main extends Application {
	private static Stage stage;

	public static Stage getStage() {
		return stage;
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			stage = primaryStage;
			FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/App.fxml"));
			Scene scene = new Scene((Parent) loader.load());
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