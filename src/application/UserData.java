package application;

import java.io.Serializable;

public class UserData implements Serializable {
	private String folderPath;
	private String password;

	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
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
