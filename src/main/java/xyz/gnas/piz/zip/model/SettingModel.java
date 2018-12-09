package xyz.gnas.piz.zip.model;

import xyz.gnas.piz.common.Constants;
import xyz.gnas.piz.common.utility.DialogUtility;
import xyz.gnas.piz.common.utility.runner.RunnerUtility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Model of the settings, serializable to save in binary file
 */
public class SettingModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private static SettingModel instance;

    private String inputFolder;
    private String outputFolder;
    private String password;
    private String tag;

    private LinkedList<String> fileFolder;

    private boolean encrypt = true;
    private boolean obfuscate = true;
    private boolean addReference = true;

    private int processCount = 5;

    /**
     * Gets the setting in zip tab, lazily loads from setting file
     *
     * @return the setting model
     */
    public static SettingModel getInstance() {
        if (instance == null) {
            RunnerUtility.executeVoidAndExceptionRunner(() -> {
                File file = new File(Constants.SETTING_FILE);

                if (file.exists()) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        ObjectInputStream ois = new ObjectInputStream(fis);
                        instance = (SettingModel) ois.readObject();
                    }
                } else {
                    setDefaultSetting();
                }
            }, (Exception e) -> {
                DialogUtility.showError(SettingModel.class, "Error getting setting", e, false);
                setDefaultSetting();
            });
        }

        return instance;
    }

    private static void setDefaultSetting() {
        instance = new SettingModel();
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

    public List<String> getFileFolder() {
        return fileFolder;
    }

    public void setFileFolder(LinkedList<String> fileFolder) {
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

    public void saveToFile() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(Constants.SETTING_FILE)) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(instance);
        }
    }
}
