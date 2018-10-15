package main.java.xyz.gnas.piz.common;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import main.java.xyz.gnas.piz.Main;

/**
 * @author Gnas
 * @Description Manage resources, including lazy load and caching
 * @Date Oct 10, 2018
 */
public class ResourceManager {
	private static final String RESOURCE_FOLDER = "main/resources/";
	private static final String CSS_FOLDER = RESOURCE_FOLDER + "css/";
	private static final String ICON_FOLDER = RESOURCE_FOLDER + "icons/";
	private static final String FXML_FOLDER = RESOURCE_FOLDER + "fxml/";
	private static final String ZIP_FXML_FOLDER = FXML_FOLDER + "zip/";

	private static Image appIcon;
	private static Image maskedIcon;
	private static Image unmaskedIcon;
	private static Image resumeIcon;
	private static Image pauseIcon;

	private static Media notificationSound;

	private static List<String> cssList;

	private static URL appFXML;
	private static URL zipFXML;
	private static URL zipItemFXML;
	private static URL referenceFXML;

	public static Image getAppIcon() {
		if (appIcon == null) {
			appIcon = new Image(Main.class.getClassLoader().getResourceAsStream(ICON_FOLDER + "app.png"));
		}

		return appIcon;
	}

	public static Media getNotificationSound() {
		if (notificationSound == null) {
			notificationSound = new Media(
					Main.class.getClassLoader().getResource(RESOURCE_FOLDER + "notification.wav").toExternalForm());
		}

		return notificationSound;
	}

	public static Image getMaskedIcon() {
		if (maskedIcon == null) {
			maskedIcon = new Image(Main.class.getClassLoader().getResourceAsStream(ICON_FOLDER + "masked.png"));
		}

		return maskedIcon;
	}

	public static Image getUnmaskedIcon() {
		if (unmaskedIcon == null) {
			unmaskedIcon = new Image(Main.class.getClassLoader().getResourceAsStream(ICON_FOLDER + "unmasked.png"));
		}

		return unmaskedIcon;
	}

	public static Image getPauseIcon() {
		if (pauseIcon == null) {
			pauseIcon = new Image(Main.class.getClassLoader().getResourceAsStream(ICON_FOLDER + "pause.png"));
		}

		return pauseIcon;
	}

	public static Image getResumeIcon() {
		if (resumeIcon == null) {
			resumeIcon = new Image(Main.class.getClassLoader().getResourceAsStream(ICON_FOLDER + "resume.png"));
		}

		return resumeIcon;
	}

	public static List<String> getCSSList() {
		if (cssList == null) {
			cssList = new LinkedList<String>();
			cssList.add(Main.class.getClassLoader().getResource(CSS_FOLDER + "app.css").toExternalForm());
			cssList.add(Main.class.getClassLoader().getResource(CSS_FOLDER + "theme.css").toExternalForm());
		}

		return cssList;
	}

	public static URL getAppFXML() {
		if (appFXML == null) {
			appFXML = Main.class.getClassLoader().getResource(FXML_FOLDER + "App.fxml");
		}

		return appFXML;
	}

	public static URL getZipFXML() {
		if (zipFXML == null) {
			zipFXML = Main.class.getClassLoader().getResource(ZIP_FXML_FOLDER + "Zip.fxml");
		}

		return zipFXML;
	}

	public static URL getZipItemFXML() {
		if (zipItemFXML == null) {
			zipItemFXML = Main.class.getClassLoader().getResource(ZIP_FXML_FOLDER + "ZipItem.fxml");
		}

		return zipItemFXML;
	}

	public static URL getReferenceFXML() {
		if (referenceFXML == null) {
			referenceFXML = Main.class.getClassLoader().getResource(FXML_FOLDER + "reference/Reference.fxml");
		}

		return referenceFXML;
	}
}
