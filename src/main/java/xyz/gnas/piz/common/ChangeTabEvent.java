package xyz.gnas.piz.common;

import javafx.scene.control.Tab;

/**
 * Event raised when the user changes
 */
public class ChangeTabEvent {
    private Tab newTab;

    public ChangeTabEvent(Tab newTab) {
        this.newTab = newTab;
    }

    public Tab getNewTab() {
        return newTab;
    }
}