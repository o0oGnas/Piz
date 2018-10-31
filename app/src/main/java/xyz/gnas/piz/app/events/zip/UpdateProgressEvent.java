package xyz.gnas.piz.app.events.zip;

import java.io.File;

import xyz.gnas.piz.core.models.zip.ZipProcessModel;

public class UpdateProgressEvent extends ZipEvent {
	private ZipProcessModel process;

	public ZipProcessModel getProcess() {
		return process;
	}

	public UpdateProgressEvent(File file, ZipProcessModel process) {
		super(file);
		this.process = process;
	}
}
