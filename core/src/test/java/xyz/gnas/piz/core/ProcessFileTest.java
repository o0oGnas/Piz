package xyz.gnas.piz.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
import java.util.SortedMap;

import org.junit.jupiter.api.Test;

import xyz.gnas.piz.core.models.Abbreviation;
import xyz.gnas.piz.core.models.ZipInput;
import xyz.gnas.piz.core.models.ZipProcess;

public class ProcessFileTest {
	@Test
	public void output_test() throws Exception {
		ZipProcess process = new ZipProcess();
		File folder = new File("folder");
		folder.mkdir();
		File file = new File(folder.getAbsolutePath() + "\\abc.txt");
		List<File> fileList = List.of(file);
		SortedMap<Abbreviation, Abbreviation> abbreviationSet = Zip.getAbbreviationList(fileList, true);

		for (Abbreviation abbreviation : abbreviationSet.keySet()) {
			ZipInput input = new ZipInput(file, file, folder, abbreviation, "password", "tag", true, true);
			Zip.processFile(input, process);
			assertTrue(process.isComplete());
			assertNotNull(process.getOutputFile());
		}
	}
}
