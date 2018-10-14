package xyz.gnas.piz.common;

import java.net.URL;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import xyz.gnas.piz.Main;

/**
 * @author Gnas
 * @Description Manage resources, including lazy load and caching
 * @Date Oct 10, 2018
 */
public class ResourceManager {
	private static final String RESOURCE_FOLDER = "resources/";
	private static final String ICON_FOLDER = RESOURCE_FOLDER + "icons/";
	private static final String FXML_FOLDER = RESOURCE_FOLDER + "fxml/";
	private static final String ZIP_FXML_FOLDER = FXML_FOLDER + "zip/";

	private static Image appIcon;
	private static Image maskedIcon;
	private static Image unmaskedIcon;
	private static Image resumeIcon;
	private static Image pauseIcon;

	private static Media notificationSound;

	private static String css;

	private static URL appFXML;
	private static URL zipFXML;
	private static URL zipItemFXML;
	private static URL referenceFXML;

	public static Image getAppIcon() {
		if (appIcon == null) {
			appIcon = new Image(Main.class.getResourceAsStream(ICON_FOLDER + "app.png"));
		}

		return appIcon;
	}

	public static Media getNotificationSound() {
		if (notificationSound == null) {
			notificationSound = new Media(
					Main.class.getResource(RESOURCE_FOLDER + "notification.wav").toExternalForm());
		}

		return notificationSound;
	}

	public static Image getMaskedIcon() {
		if (maskedIcon == null) {
			maskedIcon = new Image(Main.class.getResourceAsStream(ICON_FOLDER + "masked.png"));
		}

		return maskedIcon;
	}

	public static Image getUnmaskedIcon() {
		if (unmaskedIcon == null) {
			unmaskedIcon = new Image(Main.class.getResourceAsStream(ICON_FOLDER + "unmasked.png"));
		}

		return unmaskedIcon;
	}

	public static Image getPauseIcon() {
		if (pauseIcon == null) {
			pauseIcon = new Image(Main.class.getResourceAsStream(ICON_FOLDER + "pause.png"));
		}

		return pauseIcon;
	}

	public static Image getResumeIcon() {
		if (resumeIcon == null) {
			resumeIcon = new Image(Main.class.getResourceAsStream(ICON_FOLDER + "resume.png"));
		}

		return resumeIcon;
	}

	public static String getCss() {
		if (css == null) {
			css = Main.class.getResource(RESOURCE_FOLDER + "app.css").toExternalForm();
		}

		return css;
	}

	public static URL getAppFXML() {
		if (appFXML == null) {
			appFXML = Main.class.getResource(FXML_FOLDER + "App.fxml");
		}

		return appFXML;
	}

	public static URL getZipFXML() {
		if (zipFXML == null) {
			zipFXML = Main.class.getResource(ZIP_FXML_FOLDER + "Zip.fxml");
		}

		return zipFXML;
	}

	public static URL getZipItemFXML() {
		if (zipItemFXML == null) {
			zipItemFXML = Main.class.getResource(ZIP_FXML_FOLDER + "ZipItem.fxml");
		}

		return zipItemFXML;
	}

	public static URL getReferenceFXML() {
		if (referenceFXML == null) {
			referenceFXML = Main.class.getResource(FXML_FOLDER + "reference/Reference.fxml");
		}

		return referenceFXML;
	}
}
