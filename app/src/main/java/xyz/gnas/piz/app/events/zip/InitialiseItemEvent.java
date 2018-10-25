package xyz.gnas.piz.app.events.zip;

import java.io.File;

public class InitialiseItemEvent extends ZipEvent {
	private boolean isObfuscated;

	public boolean isObfuscated() {
		return isObfuscated;
	}

	public InitialiseItemEvent(File file, boolean isObfuscated) {
		super(file);
		this.isObfuscated = isObfuscated;
	}
}
