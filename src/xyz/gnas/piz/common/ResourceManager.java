package xyz.gnas.piz.common;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import xyz.gnas.piz.Main;

/**
 * @author Gnas
 * @Description Manage resources, including lazy load and caching
 * @Date Oct 10, 2018
 */
public class ResourceManager {
	private static final String RESOURCE_FOLDER = "resources";
	private static final String ICON_FOLDER = RESOURCE_FOLDER + "/icons";

	private static Image appIcon;
	private static Image maskIcon;
	private static Image unmaskIcon;
	private static Image resumeIcon;
	private static Image pauseIcon;

	private static Media notificationSound;

	public static Image getAppIcon() {
		if (appIcon == null) {
			appIcon = new Image(Main.class.getResourceAsStream(ICON_FOLDER + "/app.png"));
		}

		return appIcon;
	}

	public static Media getNotificationSound() {
		if (notificationSound == null) {
			notificationSound = new Media(
					Main.class.getResource(RESOURCE_FOLDER + "/notification.wav").toExternalForm());
		}

		return notificationSound;
	}

	public static Image getMaskIcon() {
		if (maskIcon == null) {
			maskIcon = new Image(Main.class.getResourceAsStream(ICON_FOLDER + "/mask.png"));
		}

		return maskIcon;
	}

	public static Image getUnmaskIcon() {
		if (unmaskIcon == null) {
			unmaskIcon = new Image(Main.class.getResourceAsStream(ICON_FOLDER + "/unmask.png"));
		}

		return unmaskIcon;
	}

	public static Image getPauseIcon() {
		if (pauseIcon == null) {
			pauseIcon = new Image(Main.class.getResourceAsStream(ICON_FOLDER + "/pause.png"));
		}

		return pauseIcon;
	}

	public static Image getResumeIcon() {
		if (resumeIcon == null) {
			resumeIcon = new Image(Main.class.getResourceAsStream(ICON_FOLDER + "/resume.png"));
		}

		return resumeIcon;
	}
}
