package xyz.gnas.piz.app.common.utility.autocomplete;

import xyz.gnas.piz.core.models.ReferenceModel;

public interface AutocompleteCallback {
    String getSuggestion(ReferenceModel reference);
}
