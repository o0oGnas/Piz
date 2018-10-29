package xyz.gnas.piz.app.common;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import xyz.gnas.piz.app.FXMain;

/**
 * @author Gnas
 * @Description Manage resources, including lazy initialisation
 * @Date Oct 10, 2018
 */
public class ResourceManager {
	private static Image appIcon;

	private static Media notificationSound;

	private static List<String> cssList;

	private static URL appFXML;
	private static URL zipFXML;
	private static URL zipItemFXML;
	private static URL referenceFXML;

	public static Image getAppIcon() {
		if (appIcon == null) {
			appIcon = new Image(getClassLoader().getResourceAsStream("icon.png"));
		}

		return appIcon;
	}

	private static ClassLoader getClassLoader() {
		return FXMain.class.getClassLoader();
	}

	public static Media getNotificationSound() {
		if (notificationSound == null) {
			notificationSound = new Media(getResourceString("notification.wav"));
		}

		return notificationSound;
	}

	private static String getResourceString(String resource) {
		return getResourceURL(resource).toExternalForm();
	}

	private static URL getResourceURL(String resource) {
		return getClassLoader().getResource(resource);
	}

	public static List<String> getCSSList() {
		if (cssList == null) {
			cssList = new LinkedList<String>();
			cssList.add(getCSS("app"));
			cssList.add(getCSS("theme"));
		}

		return cssList;
	}

	private static String getCSS(String cssName) {
		return getResourceString("css/" + cssName + ".css");
	}

	public static URL getAppFXML() {
		if (appFXML == null) {
			appFXML = getFXML("App");
		}

		return appFXML;
	}

	private static URL getFXML(String fxml) {
		return getResourceURL("fxml/" + fxml + ".fxml");
	}

	public static URL getZipFXML() {
		if (zipFXML == null) {
			zipFXML = getZipFXMLWrapper("Zip");
		}

		return zipFXML;
	}

	private static URL getZipFXMLWrapper(String fxml) {
		return getFXML("zip/" + fxml);
	}

	public static URL getZipItemFXML() {
		if (zipItemFXML == null) {
			zipItemFXML = getZipFXMLWrapper("ZipItem");
		}

		return zipItemFXML;
	}

	public static URL getReferenceFXML() {
		if (referenceFXML == null) {
			referenceFXML = getFXML("reference/Reference");
		}

		return referenceFXML;
	}
}
