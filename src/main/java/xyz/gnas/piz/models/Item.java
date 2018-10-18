package main.java.xyz.gnas.piz.models;

import java.io.File;
import java.util.Calendar;

import org.apache.commons.io.FilenameUtils;

public class Item {
	private File file;
	private String name;
	private String extension;
	private long size;
	private Calendar date;

	public File getFile() {
		return file;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public Calendar getDate() {
		return date;
	}

	public void setDate(Calendar date) {
		this.date = date;
	}

	public Item(File file) {
		this.file = file;
		name = FilenameUtils.removeExtension(file.getName());
		extension = FilenameUtils.getExtension(file.getName());
		size = file.isDirectory() ? -1 : file.length();
		date = Calendar.getInstance();
		date.setTimeInMillis(file.lastModified());
	}
}
