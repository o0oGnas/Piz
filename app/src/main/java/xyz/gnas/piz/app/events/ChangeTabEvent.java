package xyz.gnas.piz.app.events;

import javafx.scene.control.Tab;

public class ChangeTabEvent {
	private Tab newTab;

	public ChangeTabEvent(Tab newTab) {
		this.newTab = newTab;
	}

    public Tab getNewTab() {
        return newTab;
    }
}
