package test.java.xyz.gnas.piz.app;

import org.junit.jupiter.api.Test;
import xyz.gnas.piz.app.common.models.zip.AbbreviationModel;
import xyz.gnas.piz.core.logic.ZipLogic;

import java.io.File;
import java.util.List;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GetAbbreviationTest {
    @Test
    public void single_file() {
        List<File> fileList = List.of(new File("abc 123.txt"));
        SortedMap<AbbreviationModel, AbbreviationModel> abbreviationSet = ZipLogic.getAbbreviationList(fileList, true);
        assertNotNull(abbreviationSet.get(new AbbreviationModel("A1")));
    }

    @Test
    public void two_files_with_different_names() {
        List<File> fileList = List.of(new File("abc 123.txt"), new File("xyz 456.txt"));
        SortedMap<AbbreviationModel, AbbreviationModel> abbreviationSet = ZipLogic.getAbbreviationList(fileList, true);
        assertNotNull(abbreviationSet.get(new AbbreviationModel("A1")));
        assertNotNull(abbreviationSet.get(new AbbreviationModel("X4")));
    }

    @Test
    public void two_files_with_same_names() {
        List<File> fileList = List.of(new File("abc 123.txt"), new File("abc 123.ini"));
        SortedMap<AbbreviationModel, AbbreviationModel> abbreviationSet = ZipLogic.getAbbreviationList(fileList, true);
        assertNotNull(abbreviationSet.get(new AbbreviationModel("A1_txt")));
        assertNotNull(abbreviationSet.get(new AbbreviationModel("A1_ini")));
    }

    @Test
    public void two_files_with_same_simple_abbreviation() {
        List<File> fileList = List.of(new File("abc 123.txt"), new File("acb 123.txt"));
        SortedMap<AbbreviationModel, AbbreviationModel> abbreviationSet = ZipLogic.getAbbreviationList(fileList, true);
        assertNotNull(abbreviationSet.get(new AbbreviationModel("AB12")));
        assertNotNull(abbreviationSet.get(new AbbreviationModel("AC12")));
    }
}