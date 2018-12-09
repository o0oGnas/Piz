package xyz.gnas.piz.zip.event;

import java.io.File;

public class FinishProcessEvent extends ZipEvent {
    public FinishProcessEvent(File file) {
        super(file);
    }
}
