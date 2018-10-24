package main.java.xyz.gnas.piz.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

public class ApplicationModel {
	private static ApplicationModel instance = null;

	private UserSetting setting;

	/**
	 * List of references, shared between tabs
	 */
	private ObjectProperty<ObservableList<ZipReference>> referenceList = new SimpleObjectProperty<ObservableList<ZipReference>>();

	public UserSetting getSetting() {
		return setting;
	}

	public void setSetting(UserSetting setting) {
		this.setting = setting;
	}

	public ObservableList<ZipReference> getReferenceList() {
		return referenceList.get();
	}

	public ObjectProperty<ObservableList<ZipReference>> getReferenceListPropery() {
		return referenceList;
	}

	public void setReferenceList(ObservableList<ZipReference> referenceList) {
		this.referenceList.set(referenceList);
	}

	public static ApplicationModel getInstance() {
		if (instance == null) {
			instance = new ApplicationModel();
		}

		return instance;
	}
}
