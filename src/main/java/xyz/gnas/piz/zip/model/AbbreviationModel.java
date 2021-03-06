package xyz.gnas.piz.zip.model;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to make handling abbreviation easier, especially when multiple files/folder have the same abbreviation in
 * the most simple case
 */
public class AbbreviationModel implements Comparable<AbbreviationModel>, Comparator<AbbreviationModel> {
    /**
     * Original result of the most simple case of abbreviation
     */
    private String abbreviation;

    /**
     * Length of the longest word, used by uniquifying algorithm
     */
    private int longestWordLength = 0;

    /**
     * Map a file and its abbreviation
     */
    private Map<File, String> fileAbbreviationMap = new HashMap<>();

    public AbbreviationModel(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public Map<File, String> getFileAbbreviationMap() {
        return fileAbbreviationMap;
    }

    public void setFileAbbreviationMap(Map<File, String> fileAbbreviationMap) {
        this.fileAbbreviationMap = fileAbbreviationMap;
    }

    public int getLongestWordLength() {
        return longestWordLength;
    }

    public void setLongestWordLength(int longestWordLength) {
        this.longestWordLength = longestWordLength;
    }

    @Override
    public int compare(AbbreviationModel o1, AbbreviationModel o2) {
        return o1.abbreviation.compareTo(o2.abbreviation);
    }

    @Override
    public int compareTo(AbbreviationModel o) {
        return abbreviation.compareTo(o.abbreviation);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof AbbreviationModel) {
            AbbreviationModel a = (AbbreviationModel) obj;
            return abbreviation.equalsIgnoreCase(a.abbreviation);
        } else {
            return false;
        }
    }
}
