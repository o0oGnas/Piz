package main.java.xyz.gnas.piz.events.zip;

import java.io.File;

public class FinishProcessEvent extends ZipEvent {
	public FinishProcessEvent(File file) {
		super(file);
	}
}
