package xyz.gnas.piz.common;

import javafx.scene.image.Image;
import javafx.scene.media.Media;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Manage resources, including lazy initialisation
 */
public class ResourceManager {
    private static String resourceFolder = "xyz/gnas/piz/";
    private static String commonResourceFolder = resourceFolder + "common/";
    private static String zipResourceFolder = resourceFolder + "zip/";

    private static Image appIcon;

    private static List<String> cssList;

    private static URL appFXML;
    private static URL zipFXML;
    private static URL zipItemFXML;
    private static URL referenceFXML;

    private static Media notificationSound;

    public static Image getAppIcon() {
        if (appIcon == null) {
            appIcon = new Image(getClassLoader().getResourceAsStream(commonResourceFolder + "icon.png"));
        }

        return appIcon;
    }

    private static ClassLoader getClassLoader() {
        return ResourceManager.class.getClassLoader();
    }

    public static List<String> getCSSList() {
        if (cssList == null) {
            cssList = new LinkedList<>();
            cssList.add(getCSS("app"));
            cssList.add(getCSS("theme"));
        }

        return cssList;
    }

    private static String getCSS(String cssName) {
        return getCommonResourceString("css/" + cssName + ".css");
    }

    private static String getCommonResourceString(String resource) {
        return getResourceString(commonResourceFolder + resource);
    }

    private static String getResourceString(String resource) {
        return getResourceURL(resource).toExternalForm();
    }

    private static URL getResourceURL(String resource) {
        return getClassLoader().getResource(resource);
    }

    public static URL getAppFXML() {
        if (appFXML == null) {
            appFXML = getFXML(commonResourceFolder + "Application");
        }

        return appFXML;
    }

    private static URL getFXML(String fxml) {
        return getResourceURL(fxml + ".fxml");
    }

    public static URL getZipFXML() {
        if (zipFXML == null) {
            zipFXML = getZipFXMLWrapper("Zip");
        }

        return zipFXML;
    }

    private static URL getZipFXMLWrapper(String fxml) {
        return getFXML(zipResourceFolder + "fxml/" + fxml);
    }

    public static URL getZipItemFXML() {
        if (zipItemFXML == null) {
            zipItemFXML = getZipFXMLWrapper("ZipItem");
        }

        return zipItemFXML;
    }

    public static URL getReferenceFXML() {
        if (referenceFXML == null) {
            referenceFXML = getFXML(resourceFolder + "reference/Reference");
        }

        return referenceFXML;
    }

    public static Media getNotificationSound() {
        if (notificationSound == null) {
            notificationSound = new Media(getResourceString(zipResourceFolder + "notification.wav"));
        }

        return notificationSound;
    }
}