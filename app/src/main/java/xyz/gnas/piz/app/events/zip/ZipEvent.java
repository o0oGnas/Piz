package xyz.gnas.piz.app.events.zip;

import java.io.File;

public abstract class ZipEvent {
	private File file;

    ZipEvent(File file) {
        this.file = file;
    }

	public File getFile() {
		return file;
	}
}
