package xyz.gnas.piz.app.events.zip;

import xyz.gnas.piz.core.models.zip.ZipProcessModel;

import java.io.File;

public class UpdateProgressEvent extends ZipEvent {
    private ZipProcessModel process;

    public UpdateProgressEvent(File file, ZipProcessModel process) {
        super(file);
        this.process = process;
    }

    public ZipProcessModel getProcess() {
        return process;
    }
}
