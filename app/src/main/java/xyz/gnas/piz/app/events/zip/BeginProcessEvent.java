package xyz.gnas.piz.app.events.zip;

import java.io.File;

import javafx.beans.property.BooleanProperty;

public class BeginProcessEvent extends ZipEvent {
	private BooleanProperty isMasterPaused;

	public BooleanProperty getIsMasterPaused() {
		return isMasterPaused;
	}

	public BeginProcessEvent(File file, BooleanProperty isMasterPaused) {
		super(file);
		this.isMasterPaused = isMasterPaused;
	}
}
