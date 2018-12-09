package xyz.gnas.piz.common;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.greenrobot.eventbus.EventBus;
import xyz.gnas.piz.common.utility.runner.RunnerUtility;
import xyz.gnas.piz.common.utility.runner.VoidRunner;

import java.io.IOException;
import java.net.URL;

public class ApplicationController {
    @FXML
    private TabPane tpTabs;

    @FXML
    private Tab tabZip;

    @FXML
    private Tab tabReference;

    private void executeVoidRunnerOrExit(String errorMessage, VoidRunner runner) {
        RunnerUtility.executeVoidRunnerOrExit(getClass(), errorMessage, runner);
    }

    @FXML
    private void initialize() {
        executeVoidRunnerOrExit("Could not initialise app", () -> {
            initialiseTab(tabZip, ResourceManager.getZipFXML());
            initialiseTab(tabReference, ResourceManager.getReferenceFXML());
            tpTabs.getSelectionModel().selectedItemProperty().addListener(
                    l -> executeVoidRunnerOrExit("Error when handling change to tab selection",
                            () -> EventBus.getDefault().post(new ChangeTabEvent(tpTabs.getSelectionModel().getSelectedItem()))));
        });
    }

    private void initialiseTab(Tab tab, URL path) throws IOException {
        FXMLLoader loader = new FXMLLoader(path);
        tab.setContent(loader.load());
    }
}
