package xyz.gnas.piz.common.utility.auto_completion;

import xyz.gnas.piz.reference.ReferenceModel;

/**
 * Interface used by AutoCompletionUtility to define how to get a string for each ReferenceObject
 */
public interface AutoCompletionCallback {
    /**
     * Gets suggestion.
     *
     * @param reference the reference object
     * @return the suggestion string
     */
    String getSuggestion(ReferenceModel reference);
}
