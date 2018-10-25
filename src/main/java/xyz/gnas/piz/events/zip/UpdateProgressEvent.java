package main.java.xyz.gnas.piz.events.zip;

import java.io.File;

public class UpdateProgressEvent extends ZipEvent {
	private boolean isOuter;

	public boolean isOuter() {
		return isOuter;
	}

	public UpdateProgressEvent(File file, boolean isOuter) {
		super(file);
		this.isOuter = isOuter;
	}
}
