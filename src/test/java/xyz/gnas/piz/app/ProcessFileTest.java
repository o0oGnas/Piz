package test.java.xyz.gnas.piz.app;

import org.junit.jupiter.api.Test;
import xyz.gnas.piz.app.common.models.zip.AbbreviationModel;
import xyz.gnas.piz.app.common.models.zip.ZipInputModel;
import xyz.gnas.piz.app.common.models.zip.ZipProcessModel;
import xyz.gnas.piz.core.logic.ZipLogic;

import java.io.File;
import java.util.List;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
