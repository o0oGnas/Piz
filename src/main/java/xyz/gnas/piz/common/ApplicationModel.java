package xyz.gnas.piz.common;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import xyz.gnas.piz.common.utility.runner.RunnerUtility;
import xyz.gnas.piz.common.utility.runner.VoidRunner;
import xyz.gnas.piz.reference.ReferenceModel;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Model of the application, contains a list of references, uses singleton pattern
 */
public class ApplicationModel {
    private static ApplicationModel instance = null;
    private ObjectProperty<ObservableList<ReferenceModel>> referenceList = new SimpleObjectProperty<>();

    private ApplicationModel() {
    }

    private static void executeVoidRunnerOrExit(String errorMessage, VoidRunner runner) {
        RunnerUtility.executeVoidRunnerOrExit(ApplicationModel.class, errorMessage, runner);
    }

    public static ApplicationModel getInstance() {
        if (instance == null) {
            instance = new ApplicationModel();
            addListenerToReferenceList();
            executeVoidRunnerOrExit("Error when loading references", () -> instance.loadReferences());
        }

        return instance;
    }

    private static void addListenerToReferenceList() {
        instance.referenceList.addListener(propertyListener -> {
            ObservableList<ReferenceModel> list = instance.referenceList.get();

            if (list != null) {
                list.addListener((ListChangeListener<ReferenceModel>) listListener ->
                        executeVoidRunnerOrExit("Error when handling changes to reference list",
                                () -> instance.saveReferences()));
            }
        });
    }

    public ObservableList<ReferenceModel> getReferenceList() {
        return referenceList.get();
    }

    public ObjectProperty<ObservableList<ReferenceModel>> getReferenceListProperty() {
        return referenceList;
    }

    private void executeRunner(String errorMessage, VoidRunner runner) {
        RunnerUtility.executeVoidRunner(getClass(), errorMessage, runner);
    }

    private void loadReferences() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        File file = new File(Constants.REFERENCE_FILE);

        if (file.exists()) {
            ReferenceModel[] zipArray = mapper.readValue(file, ReferenceModel[].class);
            instance.referenceList.set(FXCollections.observableArrayList(List.of(zipArray)));
        } else {
            instance.referenceList.set(FXCollections.observableArrayList());
        }
    }

    /**
     * Save references to file.
     */
    public void saveReferences() {
        executeRunner("Error when saving references", () -> {
            File file = new File(Constants.REFERENCE_FILE);
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
            prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
            mapper.writeValue(file, referenceList.get().toArray());
        });
    }
}
