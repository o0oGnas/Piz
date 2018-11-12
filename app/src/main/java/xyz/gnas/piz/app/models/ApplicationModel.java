package xyz.gnas.piz.app.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import xyz.gnas.piz.core.models.ReferenceModel;

public class ApplicationModel {
	private static ApplicationModel instance = null;

	private UserSettingModel setting;

	/**
	 * List of references, shared between tabs
	 */
    private ObjectProperty<ObservableList<ReferenceModel>> referenceList = new SimpleObjectProperty<>();

    public static ApplicationModel getInstance() {
        if (instance == null) {
            instance = new ApplicationModel();
        }

        return instance;
    }

	public UserSettingModel getSetting() {
		return setting;
	}

	public void setSetting(UserSettingModel setting) {
		this.setting = setting;
	}

	public ObservableList<ReferenceModel> getReferenceList() {
		return referenceList.get();
	}

	public void setReferenceList(ObservableList<ReferenceModel> referenceList) {
		this.referenceList.set(referenceList);
	}

    public ObjectProperty<ObservableList<ReferenceModel>> getReferenceListPropery() {
        return referenceList;
	}
}
