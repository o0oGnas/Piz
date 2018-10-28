package xyz.gnas.piz.core.models;

import java.io.File;

import net.lingala.zip4j.progress.ProgressMonitor;

public class ZipProcess {
	private ProgressMonitor progressMonitor;

	private File outputFile;

	private boolean isOuter;
	private boolean isComplete;

	public ProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	public void setProgressMonitor(ProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	public boolean isOuter() {
		return isOuter;
	}

	public void setOuter(boolean isOuter) {
		this.isOuter = isOuter;
	}

	public boolean isComplete() {
		return isComplete;
	}

	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}
}
