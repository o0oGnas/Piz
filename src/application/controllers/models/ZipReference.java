package application.controllers.models;

import java.util.Calendar;

/**
 * @author Gnas
 * @Description Model of the references
 * @Date Oct 9, 2018
 */
public class ZipReference {
	private Calendar date;
	private String tag;
	private String original;
	private String zip;

	public Calendar getDate() {
		return date;
	}

	public void setDate(Calendar date) {
		this.date = date;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getOriginal() {
		return original;
	}

	public void setOriginal(String original) {
		this.original = original;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public ZipReference() {
	}

	public ZipReference(Calendar date, String tag, String original, String zip) {
		this.date = date;
		this.tag = tag;
		this.original = original;
		this.zip = zip;
	}
}
