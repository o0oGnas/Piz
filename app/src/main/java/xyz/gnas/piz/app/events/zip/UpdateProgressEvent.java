package xyz.gnas.piz.app.events.zip;

import java.io.File;

import xyz.gnas.piz.core.models.ZipProcess;

public class UpdateProgressEvent extends ZipEvent {
	private ZipProcess process;

	public ZipProcess getProcess() {
		return process;
	}

	public UpdateProgressEvent(File file, ZipProcess process) {
		super(file);
		this.process = process;
	}
}
