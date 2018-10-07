package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	private static Stage stage;

	public static Stage getStage() {
		return stage;
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			stage = primaryStage;
			FXMLLoader loader = new FXMLLoader(Main.class.getResource("App.fxml"));
			Scene scene = new Scene((Parent) loader.load());
			scene.getStylesheets().addAll(getClass().getResource("application.css").toExternalForm());
			stage.setScene(scene);
			stage.setTitle("Batch Zip");
			stage.show();
		} catch (Exception e) {
			Utility.showError(e, "Could not start the application", true);
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
