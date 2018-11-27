package xyz.gnas.piz.app.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import xyz.gnas.piz.app.common.ResourceManager;
import xyz.gnas.piz.app.common.utility.LogUtility;

public class FXMain extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private void writeErrorLog(String errorMessage, Throwable e) {
        LogUtility.writeErrorLog(getClass(), errorMessage, e);
    }

    @Override
    public void start(Stage stage) {
        try {
            Thread.setDefaultUncaughtExceptionHandler(
                    (Thread t, Throwable e) -> writeErrorLog("Uncaught exception", e));
            FXMLLoader loader = new FXMLLoader(ResourceManager.getAppFXML());
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().addAll(ResourceManager.getCSSList());
            stage.setScene(scene);
            stage.setTitle("Piz");
            stage.getIcons().add(ResourceManager.getAppIcon());
            stage.show();
        } catch (Exception e) {
            writeErrorLog("Could not start the application", e);
        }
    }
}