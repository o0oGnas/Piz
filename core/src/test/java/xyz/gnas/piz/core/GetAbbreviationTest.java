package xyz.gnas.piz.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.util.List;
import java.util.SortedMap;

import org.junit.jupiter.api.Test;

import xyz.gnas.piz.core.models.Abbreviation;

public class GetAbbreviationTest {
	@Test
	public void single_file() {
		List<File> fileList = List.of(new File("abc 123.txt"));
		SortedMap<Abbreviation, Abbreviation> abbreviationSet = Zip.getAbbreviationList(fileList, true);
		assertNotNull(abbreviationSet.get(new Abbreviation("A1")));
	}

	@Test
	public void two_files_with_different_names() {
		List<File> fileList = List.of(new File("abc 123.txt"), new File("xyz 456.txt"));
		SortedMap<Abbreviation, Abbreviation> abbreviationSet = Zip.getAbbreviationList(fileList, true);
		assertNotNull(abbreviationSet.get(new Abbreviation("A1")));
		assertNotNull(abbreviationSet.get(new Abbreviation("X4")));
	}

	@Test
	public void two_files_with_same_names() {
		List<File> fileList = List.of(new File("abc 123.txt"), new File("abc 123.ini"));
		SortedMap<Abbreviation, Abbreviation> abbreviationSet = Zip.getAbbreviationList(fileList, true);
		assertNotNull(abbreviationSet.get(new Abbreviation("A1_txt")));
		assertNotNull(abbreviationSet.get(new Abbreviation("A1_ini")));
	}

	@Test
	public void two_files_with_same_simple_abbreviation() {
		List<File> fileList = List.of(new File("abc 123.txt"), new File("acb 123.txt"));
		SortedMap<Abbreviation, Abbreviation> abbreviationSet = Zip.getAbbreviationList(fileList, true);
		assertNotNull(abbreviationSet.get(new Abbreviation("AB12")));
		assertNotNull(abbreviationSet.get(new Abbreviation("AC12")));
	}
}