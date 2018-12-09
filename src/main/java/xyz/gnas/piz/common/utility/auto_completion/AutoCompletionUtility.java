package xyz.gnas.piz.common.utility.auto_completion;

import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.TextFields;
import xyz.gnas.piz.common.ApplicationModel;
import xyz.gnas.piz.common.utility.runner.RunnerUtility;
import xyz.gnas.piz.common.utility.runner.VoidRunner;
import xyz.gnas.piz.reference.ReferenceModel;

import java.util.HashSet;
import java.util.Set;

/**
 * Contains logic to generate a list of suggestion for a text field
 */
public class AutoCompletionUtility {
    public static final int SUGGESTION_COUNT = 10;

    private static void executeRunner(Class callerClass, String errorMessage, VoidRunner runner) {
        RunnerUtility.executeVoidRunner(callerClass, errorMessage, runner);
    }

    /**
     * Bind auto complete.
     *
     * @param callerClass the caller class, used for error logging
     * @param ttf         the TextField
     * @param callback    the callback
     */
    public static void bindAutoCompletion(Class callerClass, TextField ttf, AutoCompletionCallback callback) {
        executeRunner(callerClass, "Error when updating auto_completion",
                () -> TextFields.bindAutoCompletion(ttf, s -> {
                    Set<String> result = new HashSet<>();
                    ObservableList<ReferenceModel> referenceList = ApplicationModel.getInstance().getReferenceList();

                    if (referenceList != null) {
                        String text = ttf.getText();

                        for (int i = 0; i < referenceList.size() && result.size() < SUGGESTION_COUNT; ++i) {
                            String suggestion = callback.getSuggestion(referenceList.get(i));

                            if (StringUtils.isEmpty(text) || (suggestion != null && suggestion.toUpperCase().contains(text.toUpperCase()))) {
                                result.add(suggestion);
                            }
                        }
                    }

                    return result;
                }));
    }
}