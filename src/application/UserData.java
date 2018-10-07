package application;

import java.io.Serializable;

public class UserData implements Serializable {
	private String folderPath;
	private String password;

	private String[] fileFolder;

	private boolean encrypt;
	private boolean hideFileName;
	private boolean addReference;

	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

	public String[] getFileFolder() {
		return fileFolder;
	}

	public void setFileFolder(String[] fileFolder) {
		this.fileFolder = fileFolder;
	}

	public boolean isEncrypt() {
		return encrypt;
	}

	public void setEncrypt(boolean encrypt) {
		this.encrypt = encrypt;
	}

	public boolean isHideFileName() {
		return hideFileName;
	}

	public void setHideFileName(boolean hideFileName) {
		this.hideFileName = hideFileName;
	}

	public boolean isAddReference() {
		return addReference;
	}

	public void setAddReference(boolean addReference) {
		this.addReference = addReference;
	}

	public UserData(String folderPath, String password, String[] fileFolder, boolean encrypt, boolean hideFileName,
			boolean addReference) {
		this.folderPath = folderPath;
		this.password = password;
		this.fileFolder = fileFolder;
		this.encrypt = encrypt;
		this.hideFileName = hideFileName;
		this.addReference = addReference;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public UserData(String folderPath, String password) {
		this.folderPath = folderPath;
		this.password = password;
	}
}
