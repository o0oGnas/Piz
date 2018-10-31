package xyz.gnas.piz.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
import java.util.SortedMap;

import org.junit.jupiter.api.Test;

import xyz.gnas.piz.core.logic.ZipLogic;
import xyz.gnas.piz.core.models.zip.AbbreviationModel;
import xyz.gnas.piz.core.models.zip.ZipInputModel;
import xyz.gnas.piz.core.models.zip.ZipProcessModel;

public class ProcessFileTest {
	@Test
	public void output_test() throws Exception {
		ZipProcessModel process = new ZipProcessModel();
		File folder = new File("folder");
		folder.mkdir();
		File file = new File(folder.getAbsolutePath() + "\\abc.txt");
		List<File> fileList = List.of(file);
		SortedMap<AbbreviationModel, AbbreviationModel> abbreviationSet = ZipLogic.getAbbreviationList(fileList, true);

		for (AbbreviationModel abbreviation : abbreviationSet.keySet()) {
			ZipInputModel input = new ZipInputModel(file, file, folder, abbreviation, "password", "tag", true, true);
			ZipLogic.processFile(input, process);
			assertTrue(process.isComplete());
			assertNotNull(process.getOutputFile());
		}
	}
}
