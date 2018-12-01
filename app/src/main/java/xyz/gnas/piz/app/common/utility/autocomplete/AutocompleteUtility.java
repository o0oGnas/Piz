package xyz.gnas.piz.app.common.utility.autocomplete;

import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import org.controlsfx.control.textfield.TextFields;
import xyz.gnas.piz.app.common.Configurations;
import xyz.gnas.piz.app.common.utility.code.CodeRunnerUtility;
import xyz.gnas.piz.app.common.utility.code.Runner;
import xyz.gnas.piz.app.models.ApplicationModel;
import xyz.gnas.piz.core.models.ReferenceModel;

import java.util.HashSet;
import java.util.Set;

public class AutocompleteUtility {
    private static void executeRunner(Class callerClass, String errorMessage, Runner runner) {
        CodeRunnerUtility.executeRunner(callerClass, errorMessage, runner);
    }

    public static void bindAutoComplete(Class callerClass, TextField ttf, AutocompleteCallback callback) {
        executeRunner(callerClass, "Error when updating autocomplete", () -> {
            ObservableList<ReferenceModel> referenceList = ApplicationModel.getInstance().getReferenceList();

            TextFields.bindAutoCompletion(ttf, s -> {
                Set<String> result = new HashSet<>();

                for (int i = 0; i < referenceList.size() && result.size() < Configurations.SUGGESTION_COUNT; ++i) {
                    String suggestion = callback.getSuggestion(referenceList.get(i));

                    if (suggestion != null && suggestion.toUpperCase().contains(ttf.getText().toUpperCase())) {
                        result.add(suggestion);
                    }
                }

                return result;
            });
        });
    }
}
