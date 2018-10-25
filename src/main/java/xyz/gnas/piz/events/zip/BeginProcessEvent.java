package main.java.xyz.gnas.piz.events.zip;

import java.io.File;

import javafx.beans.property.BooleanProperty;
import net.lingala.zip4j.progress.ProgressMonitor;

public class BeginProcessEvent extends ZipEvent {
	private ProgressMonitor progressMonitor;
	private String zipName;
	private BooleanProperty isMasterPaused;

	public ProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	public String getZipName() {
		return zipName;
	}

	public BooleanProperty getIsMasterPaused() {
		return isMasterPaused;
	}

	public BeginProcessEvent(File file, ProgressMonitor progressMonitor, String zipName,
			BooleanProperty isMasterPaused) {
		super(file);
		this.progressMonitor = progressMonitor;
		this.zipName = zipName;
		this.isMasterPaused = isMasterPaused;
	}
}
