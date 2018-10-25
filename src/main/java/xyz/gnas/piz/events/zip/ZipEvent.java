package main.java.xyz.gnas.piz.events.zip;

import java.io.File;

public abstract class ZipEvent {
	private File file;

	public File getFile() {
		return file;
	}

	public ZipEvent(File file) {
		this.file = file;
	}
}
