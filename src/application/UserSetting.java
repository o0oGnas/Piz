package application;

import java.io.Serializable;

public class UserSetting implements Serializable {
	private String folderPath;
	private String password;
	private String referenceTag;

	private String[] fileFolder;

	private boolean encrypt;
	private boolean obfuscateFileName;
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

	public String getReferenceTag() {
		return referenceTag;
	}

	public void setReferenceTag(String referenceTag) {
		this.referenceTag = referenceTag;
	}

	public boolean isEncrypt() {
		return encrypt;
	}

	public void setEncrypt(boolean encrypt) {
		this.encrypt = encrypt;
	}

	public boolean isObfuscateFileName() {
		return obfuscateFileName;
	}

	public void setObfuscateFileName(boolean obfuscateFileName) {
		this.obfuscateFileName = obfuscateFileName;
	}

	public boolean isAddReference() {
		return addReference;
	}

	public void setAddReference(boolean addReference) {
		this.addReference = addReference;
	}

	public UserSetting(String folderPath, String password, String referenceTag, String[] fileFolder, boolean encrypt,
			boolean obfuscateFileName, boolean addReference) {
		this.folderPath = folderPath;
		this.password = password;
		this.referenceTag = referenceTag;
		this.fileFolder = fileFolder;
		this.encrypt = encrypt;
		this.obfuscateFileName = obfuscateFileName;
		this.addReference = addReference;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public UserSetting(String folderPath, String password) {
		this.folderPath = folderPath;
		this.password = password;
	}
}