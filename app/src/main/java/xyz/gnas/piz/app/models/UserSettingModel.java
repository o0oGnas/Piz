package xyz.gnas.piz.app.models;

import java.io.Serializable;

/**
 * @author Gnas
 * @Description Model of the settings, serializable to save in binary file
 * @Date Oct 9, 2018
 */
public class UserSettingModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private String inputFolder;
    private String outputFolder;

    private String password;
    private String tag;

    private String[] fileFolder;

    private boolean encrypt;
    private boolean obfuscate;
    private boolean addReference;

    private int processCount;

    public UserSettingModel(String inputFolder, String password, String tag, String[] fileFolder, boolean encrypt,
                            boolean obfuscate, boolean addReference, int processCount) {
        this.inputFolder = inputFolder;
        this.password = password;
        this.tag = tag;
        this.fileFolder = fileFolder;
        this.encrypt = encrypt;
        this.obfuscate = obfuscate;
        this.addReference = addReference;
        this.processCount = processCount;
    }

    public String getInputFolder() {
        return inputFolder;
    }

    public void setInputFolder(String folderPath) {
        this.inputFolder = folderPath;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    public String[] getFileFolder() {
        return fileFolder;
    }

    public void setFileFolder(String[] fileFolder) {
        this.fileFolder = fileFolder;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
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

    public void setObfuscate(boolean obfuscate) {
        this.obfuscate = obfuscate;
    }

    public boolean isAddReference() {
        return addReference;
    }

    public void setAddReference(boolean addReference) {
        this.addReference = addReference;
    }

    public int getProcessCount() {
        return processCount;
    }

    public void setProcessCount(int processCount) {
        this.processCount = processCount;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
