package xyz.gnas.piz;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import xyz.gnas.piz.common.CommonConstants;
import xyz.gnas.piz.common.CommonUtility;

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
			stage.setScene(scene);
			stage.setResizable(false);
			stage.setTitle("Batch Zip");
			stage.getIcons()
					.add(new Image(getClass().getResourceAsStream(CommonConstants.RESOURCE_FOLDER + "/icon.png")));
			stage.show();
		} catch (Exception e) {
			CommonUtility.showError(e, "Could not start the application", true);
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}