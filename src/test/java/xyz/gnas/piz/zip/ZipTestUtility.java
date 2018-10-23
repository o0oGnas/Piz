package test.java.xyz.gnas.piz.zip;

import org.controlsfx.control.CheckComboBox;
import org.testfx.api.FxRobot;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import test.java.xyz.gnas.piz.TestUtility;

public class ZipTestUtility {
	private static CheckComboBox<String> ccbFileFolder;

	private static HBox hboPassword;
	private static HBox hboReference;
	private static HBox hboTag;

	private static CheckBox chkEncrypt;
	private static CheckBox chkObfuscateFileName;
	private static CheckBox chkAddReferences;

	private static PasswordField pwfPassword;

	private static TextField txtProcessCount;
	private static TextField txtPassword;
	private static TextField txtReferenceTag;

	private static ImageView imvMaskUnmask;

	private static Button btnStart;

	public static CheckComboBox<String> getFileFolderCheckComboBox(FxRobot robot) {
		if (ccbFileFolder == null) {
			ccbFileFolder = TestUtility.getCheckComboBox(robot, "ccbFileFolder");
		}

		return ccbFileFolder;
	}

	public static HBox getPasswordHBox(FxRobot robot) {
		if (hboPassword == null) {
			hboPassword = TestUtility.getHBox(robot, "hboPassword");
		}

		return hboPassword;
	}

	public static HBox getReferenceHBox(FxRobot robot) {
		if (hboReference == null) {
			hboReference = TestUtility.getHBox(robot, "hboReference");
		}

		return hboReference;
	}

	public static HBox getTagHBox(FxRobot robot) {
		if (hboTag == null) {
			hboTag = TestUtility.getHBox(robot, "hboTag");
		}

		return hboTag;
	}

	public static CheckBox getEncryptCheckBox(FxRobot robot) {
		if (chkEncrypt == null) {
			chkEncrypt = TestUtility.getCheckBox(robot, "chkEncrypt");
		}

		return chkEncrypt;
	}

	public static CheckBox getObfuscateCheckBox(FxRobot robot) {
		if (chkObfuscateFileName == null) {
			chkObfuscateFileName = TestUtility.getCheckBox(robot, "chkObfuscateFileName");
		}

		return chkObfuscateFileName;
	}

	public static CheckBox getAddReferenceCheckBox(FxRobot robot) {
		if (chkAddReferences == null) {
			chkAddReferences = TestUtility.getCheckBox(robot, "chkAddReferences");
		}

		return chkAddReferences;
	}

	public static PasswordField getPasswordField(FxRobot robot) {
		if (pwfPassword == null) {
			pwfPassword = TestUtility.getPasswordField(robot, "pwfPassword");
		}

		return pwfPassword;
	}

	public static TextField getProcessCountTextField(FxRobot robot) {
		if (txtProcessCount == null) {
			txtProcessCount = TestUtility.getTextField(robot, "txtProcessCount");
		}

		return txtProcessCount;
	}

	public static TextField getPasswordTextField(FxRobot robot) {
		if (txtPassword == null) {
			txtPassword = TestUtility.getTextField(robot, "txtPassword");
		}

		return txtPassword;
	}

	public static TextField getReferenceTagTextField(FxRobot robot) {
		if (txtReferenceTag == null) {
			txtReferenceTag = TestUtility.getTextField(robot, "txtReferenceTag");
		}

		return txtReferenceTag;
	}

	public static ImageView getMaskUnmaskIcon(FxRobot robot) {
		if (imvMaskUnmask == null) {
			imvMaskUnmask = TestUtility.getImageView(robot, "imvMaskUnmask");
		}

		return imvMaskUnmask;
	}

	public static Button getStartButton(FxRobot robot) {
		if (btnStart == null) {
			btnStart = TestUtility.getButtonByID(robot, "btnStart");
		}

		return btnStart;
	}
}
