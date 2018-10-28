package xyz.gnas.piz.core.models;

import java.io.File;
import java.util.List;

public class ZipInput {
	private File originalFile;
	private File fileToZip;
	private File outputFolder;

	private String password;
	private String tag;

	private boolean encrypt;
	private boolean obfuscate;
	private boolean addReference;

	private List<ZipReference> referenceList;

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

	public String getPassword() {
		return password;
	}

	public String getTag() {
		return tag;
	}

	public boolean isEncrypt() {
		return encrypt;
	}

	public boolean isObfuscate() {
		return obfuscate;
	}

	public boolean isAddReference() {
		return addReference;
	}

	public List<ZipReference> getReferenceList() {
		return referenceList;
	}

	public ZipInput(File originalFile, File fileToZip, File outputFolder, String password, String tag, boolean encrypt,
			boolean obfuscate, boolean addReference, List<ZipReference> referenceList) {
		this.originalFile = originalFile;
		this.fileToZip = fileToZip;
		this.outputFolder = outputFolder;
		this.password = password;
		this.tag = tag;
		this.encrypt = encrypt;
		this.obfuscate = obfuscate;
		this.addReference = addReference;
		this.referenceList = referenceList;
	}
}
