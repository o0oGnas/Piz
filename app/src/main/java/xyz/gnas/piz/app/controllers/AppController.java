package xyz.gnas.piz.app.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.greenrobot.eventbus.EventBus;
import xyz.gnas.piz.app.common.ResourceManager;
import xyz.gnas.piz.app.common.utility.LogUtility;
import xyz.gnas.piz.app.common.utility.code.CodeRunnerUtility;
import xyz.gnas.piz.app.common.utility.code.Runner;
import xyz.gnas.piz.app.events.ChangeTabEvent;

import java.io.IOException;
import java.net.URL;

public class AppController {
    @FXML
    private TabPane tpTabs;

    @FXML
    private Tab tabZip;

    @FXML
    private Tab tabReference;

    private void executeRunnerOrExit(String errorMessage, Runner runner) {
        CodeRunnerUtility.executeRunnerOrExit(getClass(), errorMessage, runner);
    }

    private void writeInfoLog(String log) {
        LogUtility.writeInfoLog(getClass(), log);
    }

    @FXML
    private void initialize() {
        executeRunnerOrExit("Could not initialise app", () -> {
            initialiseTab(tabZip, ResourceManager.getZipFXML());
            initialiseTab(tabReference, ResourceManager.getReferenceFXML());
            tpTabs.getSelectionModel().selectedItemProperty().addListener(
                    l -> executeRunnerOrExit("Error when handling change to tab selection",
                            () -> EventBus.getDefault().post(new ChangeTabEvent(tpTabs.getSelectionModel().getSelectedItem()))));
        });
    }

    private void initialiseTab(Tab tab, URL path) throws IOException {
        FXMLLoader loader = new FXMLLoader(path);
        tab.setContent(loader.load());
    }
}
