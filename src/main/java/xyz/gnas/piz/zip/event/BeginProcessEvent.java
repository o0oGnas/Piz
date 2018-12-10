package xyz.gnas.piz.zip.event;

import javafx.beans.property.BooleanProperty;

import java.io.File;

/**
 * Event raised when a process begins
 */
public class BeginProcessEvent extends ZipEvent {
    private final BooleanProperty isMasterPaused;

    public BeginProcessEvent(File file, BooleanProperty isMasterPaused) {
        super(file, ZipEventType.Begin);
        this.isMasterPaused = isMasterPaused;
    }

    public BooleanProperty getIsMasterPaused() {
        return isMasterPaused;
    }
}