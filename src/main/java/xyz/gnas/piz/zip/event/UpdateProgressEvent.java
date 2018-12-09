package xyz.gnas.piz.zip.event;

import xyz.gnas.piz.zip.model.ZipProcessModel;

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
