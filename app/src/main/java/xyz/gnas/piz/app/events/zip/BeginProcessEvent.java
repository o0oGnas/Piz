package xyz.gnas.piz.app.events.zip;

import javafx.beans.property.BooleanProperty;

import java.io.File;

public class BeginProcessEvent extends ZipEvent {
    private final BooleanProperty isMasterPaused;

	public BeginProcessEvent(File file, BooleanProperty isMasterPaused) {
		super(file);
		this.isMasterPaused = isMasterPaused;
	}

    public BooleanProperty getIsMasterPaused() {
        return isMasterPaused;
    }
}