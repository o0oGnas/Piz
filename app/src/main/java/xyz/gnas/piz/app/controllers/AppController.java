package xyz.gnas.piz.app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import xyz.gnas.piz.app.common.Configurations;
import xyz.gnas.piz.app.common.ResourceManager;
import xyz.gnas.piz.app.common.Utility;
import xyz.gnas.piz.app.events.ChangeTabEvent;
import xyz.gnas.piz.app.events.SaveReferenceEvent;
import xyz.gnas.piz.app.models.ApplicationModel;
import xyz.gnas.piz.core.logic.ReferenceLogic;
import xyz.gnas.piz.core.models.ReferenceModel;

import java.io.IOException;
import java.net.URL;

public class AppController {
    @FXML
    private TabPane tpTabs;

    @FXML
    private Tab tabZip;

    @FXML
    private Tab tabReference;

    private void showError(Exception e, String message, boolean exit) {
        Utility.showError(getClass(), e, message, exit);
    }

    private void writeInfoLog(String log) {
        Utility.writeInfoLog(getClass(), log);
    }

    @Subscribe
    public void onSaveReferenceEvent(SaveReferenceEvent event) {
        try {
            writeInfoLog("Saving references to file");
            ReferenceLogic.saveReferences(ApplicationModel.getInstance().getReferenceList(), Configurations.REFERENCE_FILE);
        } catch (Exception e) {
            showError(e, "Could not save references", false);
        }
    }

    @FXML
    private void initialize() {
        try {
            EventBus.getDefault().register(this);

            // zip tab
            initialiseTab(tabZip, ResourceManager.getZipFXML());

            // reference tab
            initialiseTab(tabReference, ResourceManager.getReferenceFXML());

            tpTabs.getSelectionModel().selectedItemProperty().addListener(l -> EventBus.getDefault().post(new ChangeTabEvent(tpTabs.getSelectionModel().getSelectedItem())));

            initialiseReferenceList();
        } catch (Exception e) {
            showError(e, "Could not initialise app", true);
        }
    }

    private void initialiseTab(Tab tab, URL path) throws IOException {
        FXMLLoader loader = new FXMLLoader(path);
        tab.setContent(loader.load());
    }

    private void initialiseReferenceList() throws IOException {
        writeInfoLog("Initialising references");
        ApplicationModel model = ApplicationModel.getInstance();
        model.setReferenceList(
                FXCollections.observableArrayList(ReferenceLogic.loadReferences(Configurations.REFERENCE_FILE)));

        model.getReferenceList().addListener((ListChangeListener<ReferenceModel>) listener -> {
            try {
                onSaveReferenceEvent(null);
            } catch (Exception e) {
                Utility.showError(getClass(), e, "Error when saving references to file", false);
            }
        });
    }
}
