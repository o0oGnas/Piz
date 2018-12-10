package xyz.gnas.piz.zip.model;

import net.lingala.zip4j.progress.ProgressMonitor;
import xyz.gnas.piz.reference.ReferenceModel;

import java.io.File;

/**
 * Contains relevant progress info of a zip process
 */
public class ZipProcessModel {
    private ProgressMonitor progressMonitor;

    private File outputFile;

    private ReferenceModel reference;

    private boolean isOuter;
    private boolean isComplete;

    public ProgressMonitor getProgressMonitor() {
        return progressMonitor;
    }

    public void setProgressMonitor(ProgressMonitor progressMonitor) {
        this.progressMonitor = progressMonitor;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public ReferenceModel getReference() {
        return reference;
    }

    public void setReference(ReferenceModel reference) {
        this.reference = reference;
    }

    public boolean isOuter() {
        return isOuter;
    }

    public void setOuter(boolean isOuter) {
        this.isOuter = isOuter;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }
}
