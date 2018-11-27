package xyz.gnas.piz.app.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import xyz.gnas.piz.app.common.Configurations;
import xyz.gnas.piz.app.common.utility.code.CodeRunnerUtility;
import xyz.gnas.piz.app.common.utility.code.Runner;
import xyz.gnas.piz.app.events.SaveReferenceEvent;
import xyz.gnas.piz.core.logic.ReferenceLogic;
import xyz.gnas.piz.core.models.ReferenceModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import static javafx.collections.FXCollections.observableArrayList;
import static xyz.gnas.piz.app.common.utility.DialogUtility.showError;
import static xyz.gnas.piz.app.common.utility.code.CodeRunnerUtility.executeRunnerAndHandleException;

public class ApplicationModel {
	private static ApplicationModel instance = null;

    public static ApplicationModel getInstance() {
        if (instance == null) {
            instance = new ApplicationModel();
            EventBus.getDefault().register(instance);
            addListenerToReferenceList();

            executeRunnerOrExit("Error when loading references",
                    () -> instance.referenceList.set(observableArrayList(ReferenceLogic.loadReferences(Configurations.REFERENCE_FILE))));
        }

        return instance;
    }

    private static void addListenerToReferenceList() {
        instance.referenceList.addListener(propertyListener -> {
            ObservableList<ReferenceModel> list = instance.referenceList.get();

            if (list != null) {
                list.addListener((ListChangeListener<ReferenceModel>) listListener ->
                        executeRunnerOrExit("Error when handling changes to reference list", () ->
                                instance.onSaveReferenceEvent(null)));
            }
        });
    }

    private static void executeRunnerOrExit(String errorMessage, Runner runner) {
        CodeRunnerUtility.executeRunnerOrExit(ApplicationModel.class, errorMessage, runner);
    }

    private SettingModel setting;

	/**
	 * List of references, shared between tabs
	 */
    private ObjectProperty<ObservableList<ReferenceModel>> referenceList = new SimpleObjectProperty<>();

    public SettingModel getSetting() {
        if (setting == null) {
            executeRunnerAndHandleException(() -> {
                try (FileInputStream fis = new FileInputStream(new File(Configurations.SETTING_FILE))) {
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    setting = (SettingModel) ois.readObject();
                }
            }, (Exception e) -> {
                showError(SettingModel.class, "Error getting setting", e, false);
                setting = new SettingModel(null, null, null, null, true, true, true, 5);
            });
        }

        return setting;
    }

    public ObservableList<ReferenceModel> getReferenceList() {
        return referenceList.get();
    }

    public ObjectProperty<ObservableList<ReferenceModel>> getReferenceListProperty() {
        return referenceList;
    }

    private void executeRunner(String errorMessage, Runner runner) {
        CodeRunnerUtility.executeRunner(getClass(), errorMessage, runner);
    }

    @Subscribe
    public void onSaveReferenceEvent(SaveReferenceEvent event) {
        executeRunner("Error when handling save references event",
                () -> ReferenceLogic.saveReferences(getInstance().getReferenceList(), Configurations.REFERENCE_FILE));
    }
}
