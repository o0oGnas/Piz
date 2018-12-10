package xyz.gnas.piz.zip.model;

import java.io.File;

/**
 * Convenient class to pass input from ZipController to ZipLogic
 */
public class ZipInputModel {
    private File originalFile;
    private File fileToZip;
    private File outputFolder;

    private AbbreviationModel abbreviation;

    private String password;
    private String tag;

    private boolean encrypt;
    private boolean obfuscate;

    public ZipInputModel(File originalFile, File fileToZip, File outputFolder, AbbreviationModel abbreviation,
                         String password, String tag, boolean encrypt, boolean obfuscate) {
        this.originalFile = originalFile;
        this.fileToZip = fileToZip;
        this.outputFolder = outputFolder;
        this.abbreviation = abbreviation;
        this.password = password;
        this.tag = tag;
        this.encrypt = encrypt;
        this.obfuscate = obfuscate;
    }

    public File getOriginalFile() {
        return originalFile;
    }

    public File getFileToZip() {
        return fileToZip;
    }

    public void setFileToZip(File fileToZip) {
        this.fileToZip = fileToZip;
    }

    public File getOutputFolder() {
        return outputFolder;
    }

    public AbbreviationModel getAbbreviation() {
        return abbreviation;
    }

    public String getPassword() {
        return password;
    }

    public String getTag() {
        return tag;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }

    public boolean isObfuscate() {
        return obfuscate;
    }
}
