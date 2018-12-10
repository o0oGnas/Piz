package xyz.gnas.piz.zip;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.io.FilenameUtils;
import xyz.gnas.piz.reference.ReferenceModel;
import xyz.gnas.piz.zip.model.AbbreviationModel;
import xyz.gnas.piz.zip.model.ZipInputModel;
import xyz.gnas.piz.zip.model.ZipProcessModel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Handles zip logic, including getting the list of abbreviations and zipping
 */
public class ZipLogic {
    /**
     * Get a set of Abbreviation objects based on a list of files
     *
     * @param fileList     input list of files
     * @param isObfuscated flag to tell if obfuscation is needed
     * @return the abbreviation list
     */
    public static SortedMap<AbbreviationModel, AbbreviationModel> getAbbreviationList(List<File> fileList,
                                                                                      boolean isObfuscated) {
        SortedMap<AbbreviationModel, AbbreviationModel> result = new TreeMap<>();

        for (File file : fileList) {
            // remove trailing spaces and replace multiple spaces by one space
            String fileName = file.getName().trim().replaceAll(" +", " ");
            String zipFileName = isObfuscated ? getAbbreviatedFileName(fileName, file.isDirectory()) :
                    FilenameUtils.removeExtension(fileName);
            AbbreviationModel abbreviation = new AbbreviationModel(zipFileName);

            if (result.containsKey(abbreviation)) {
                abbreviation = result.get(abbreviation);
            }

            abbreviation.getFileAbbreviationMap().put(file, abbreviation.getAbbreviation());
            result.put(abbreviation, abbreviation);
        }

        if (isObfuscated) {
            result = generateObfuscatedAbbreviationList(result);
        }

        return result;
    }

    /**
     * Most simple case of abbreviation
     *
     * @param fileName name of the original file
     * @return the abbreviated name
     */
    private static String getAbbreviatedFileName(String fileName, boolean isFolder) {
        String delimiter = " ";
        String[] split = isFolder ? fileName.split(delimiter) :
                FilenameUtils.removeExtension(fileName).split(delimiter);
        StringBuilder sb = new StringBuilder();

        // get the first character of each word in upper case and append to result
        for (String word : split) {
            sb.append(word, 0, 1);
        }

        return sb.toString().toUpperCase();
    }

    private static SortedMap<AbbreviationModel, AbbreviationModel> generateObfuscatedAbbreviationList(SortedMap<AbbreviationModel, AbbreviationModel> abbreviationList) {
        for (AbbreviationModel abbreviation : abbreviationList.keySet()) {
            for (File file : abbreviation.getFileAbbreviationMap().keySet()) {
                String[] split = FilenameUtils.removeExtension(file.getName()).split(" ");

                for (String word : split) {
                    int length = word.length();

                    if (abbreviation.getLongestWordLength() < length) {
                        abbreviation.setLongestWordLength(length);
                    }
                }
            }
        }

        uniquifyAbbreviationList(abbreviationList);

        // It's not possible to guarantee unique names even after trying to uniquify
        return mergeDuplicateAbbreviations(abbreviationList);
    }

    /**
     * create unique abbreviations when there are multiple  files/folders with the same abbreviation under the most
     * simple case
     */
    private static void uniquifyAbbreviationList(SortedMap<AbbreviationModel, AbbreviationModel> abbreviationList) {
        for (AbbreviationModel abbreviation : abbreviationList.keySet()) {
            Map<File, String> map = abbreviation.getFileAbbreviationMap();

            if (map.size() > 1) {
                // first try to add file extension to remove duplicate
                Map<File, String> uniqueMap = uniquifyByExtension(abbreviation);

                // map that contains original files and their rebuilt names used for  abbreviation
                Map<File, String> fileRebuiltNameMap = new HashMap<>();

                for (File file : map.keySet()) {
                    fileRebuiltNameMap.put(file, file.getName());
                }

                int characterCount = 1;

                // if there are still duplicates, add a character recursively until there are no duplicates
                while (uniqueMap.values().stream().distinct().count() < uniqueMap.keySet().size() && characterCount < abbreviation.getLongestWordLength()) {
                    uniqueMap = uniquifyByAddingCharacters(uniqueMap, characterCount, fileRebuiltNameMap);
                    ++characterCount;
                }

                abbreviation.setFileAbbreviationMap(uniqueMap);
            }
        }
    }

    private static Map<File, String> uniquifyByExtension(AbbreviationModel abbreviation) {
        Map<File, String> result = new HashMap<>();
        Map<File, String> map = abbreviation.getFileAbbreviationMap();

        for (File file : map.keySet()) {
            String fileName = map.get(file);

            if (file.isDirectory()) {
                result.put(file, fileName);
            } else {
                String name = file.getName();
                String abbreviatedName = getAbbreviatedFileName(name, false);
                result.put(file, abbreviatedName + "_" + FilenameUtils.getExtension(name));
            }
        }

        return result;
    }

    private static Map<File, String> uniquifyByAddingCharacters(Map<File, String> map, int characterCount,
                                                                Map<File, String> fileRebuiltNameMap) {
        // temporary new abbreviation list
        Map<AbbreviationModel, AbbreviationModel> newAbbreviationList = getNewAbbreviationList(map);
        updateNewAbbreviationList(newAbbreviationList, characterCount, fileRebuiltNameMap);
        Map<File, String> result = new HashMap<>();

        for (AbbreviationModel abbreviation : newAbbreviationList.keySet()) {
            Map<File, String> fileAbbreviationMap = abbreviation.getFileAbbreviationMap();

            for (File file : fileAbbreviationMap.keySet()) {
                String newAbbreviatedName = map.get(file);

                // get abbreviation of each file in abbreviation with duplicates using their rebuilt name
                if (fileAbbreviationMap.size() > 1) {
                    newAbbreviatedName = getAbbreviatedFileName(fileRebuiltNameMap.get(file), file.isDirectory());
                }

                result.put(file, newAbbreviatedName);
            }
        }

        return result;
    }

    private static Map<AbbreviationModel, AbbreviationModel> getNewAbbreviationList(Map<File, String> map) {
        Map<AbbreviationModel, AbbreviationModel> result = new HashMap<>();

        for (String value : map.values()) {
            AbbreviationModel abbreviation = new AbbreviationModel(value);

            if (result.containsKey(abbreviation)) {
                abbreviation = result.get(abbreviation);
            }

            for (File file : map.keySet()) {
                if (map.get(file).equalsIgnoreCase(value)) {
                    abbreviation.getFileAbbreviationMap().put(file, value);
                }
            }

            result.put(abbreviation, abbreviation);
        }

        return result;
    }

    private static void updateNewAbbreviationList(Map<AbbreviationModel, AbbreviationModel> newAbbreviationList,
                                                  int characterCount, Map<File, String> fileRebuiltNameMap) {
        for (AbbreviationModel abbreviation : newAbbreviationList.keySet()) {
            Map<File, String> map = abbreviation.getFileAbbreviationMap();

            // rebuild the original file names of abbreviation with multiple files
            if (map.size() > 1) {
                for (File file : map.keySet()) {
                    fileRebuiltNameMap.put(file, getRebuiltName(file, characterCount));
                }
            }
        }
    }

    private static String getRebuiltName(File file, int characterCount) {
        String space = " ";
        String[] split = FilenameUtils.removeExtension(file.getName()).split(" ");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < split.length; ++i) {
            if (i > 0) {
                sb.append(space);
            }

            String word = split[i];

            // separate each consecutive character by a space
            for (int j = 0; j <= characterCount && j < word.length(); ++j) {
                if (j > 0) {
                    sb.append(space);
                }

                sb.append(word.charAt(j));
            }
        }

        return sb.toString().toUpperCase();
    }

    private static SortedMap<AbbreviationModel, AbbreviationModel> mergeDuplicateAbbreviations(SortedMap<AbbreviationModel, AbbreviationModel> abbreviationList) {
        SortedMap<AbbreviationModel, AbbreviationModel> newAbbreviationList = new TreeMap<>();

        for (AbbreviationModel abbreviation : abbreviationList.keySet()) {
            Map<File, String> map = abbreviation.getFileAbbreviationMap();

            for (File file : map.keySet()) {
                // create a new Abbreviation object for each newly generated abbreviation
                AbbreviationModel newAbbreviation = new AbbreviationModel(map.get(file));

                if (newAbbreviationList.containsKey(newAbbreviation)) {
                    newAbbreviation = newAbbreviationList.get(newAbbreviation);
                }

                newAbbreviation.getFileAbbreviationMap().put(file, newAbbreviation.getAbbreviation());
                newAbbreviationList.put(newAbbreviation, newAbbreviation);
            }
        }

        return newAbbreviationList;
    }

    /**
     * perform zipping on a file
     *
     * @param input   wrapper around necessary inputs
     * @param process object to contain updates to the process
     * @throws Exception the exception
     */
    public static void processFile(ZipInputModel input, ZipProcessModel process) throws Exception {
        if (input.isEncrypt() && input.isObfuscate()) {
            obfuscateFileNameAndZip(input, process);
        } else {
            prepareToZip(input, process, input.getAbbreviation().getAbbreviation());
        }

        process.setComplete(true);
    }

    private static void obfuscateFileNameAndZip(ZipInputModel input, ZipProcessModel process)
            throws ZipException, InterruptedException {
        AbbreviationModel abbreviation = input.getAbbreviation();
        String zipName = abbreviation.getAbbreviation();
        Map<File, String> map = abbreviation.getFileAbbreviationMap();

        if (map.size() > 1) {
            zipName = getSuffixedZipName(input, zipName, map);
        }

        // create inner zip without encryption
        boolean encrypt = input.isEncrypt();
        input.setEncrypt(false);
        File innerZipFile = prepareToZip(input, process, zipName + "_inner");

        // only create outer zip if it's not cancelled
        if (innerZipFile != null) {
            input.setEncrypt(encrypt);
            processOuterZip(input, process, innerZipFile, zipName);
        }
    }

    private static String getSuffixedZipName(ZipInputModel input, String zipName, Map<File, String> map) {
        ArrayList<File> temp = new ArrayList<>(map.keySet());

        for (int i = 0; i < temp.size(); ++i) {
            if (input.getOriginalFile().equals(temp.get(i))) {
                // add suffix to make zip name unique
                zipName += "_" + (i + 1);
                break;
            }
        }

        return zipName;
    }

    private static File prepareToZip(ZipInputModel input, ZipProcessModel process, String zipName) throws ZipException, InterruptedException {
        File result = getResultZipFile(input, zipName);
        process.setOutputFile(result);
        performZip(input, process, result.getAbsolutePath());

        if (process.getProgressMonitor().isCancelAllTasks()) {
            return null;
        } else {
            return result;
        }
    }

    private static File getResultZipFile(ZipInputModel input, String zipName) {
        String fullZipName;
        File result = null;
        int count = 0;

        // if zip file with this name already exists, append a number until we get a  unique file name
        do {
            fullZipName = zipName;

            if (result != null) {
                fullZipName += "_" + count;
            }

            fullZipName += ".zip";
            result = new File(input.getOutputFolder().getAbsolutePath() + File.separator + fullZipName);
            ++count;
        } while (result == null || result.exists());

        return result;
    }

    private static void performZip(ZipInputModel input, ZipProcessModel process, String zipPath)
            throws ZipException, InterruptedException {
        ZipFile zip = getZipFile(input, zipPath);
        ProgressMonitor progressMonitor = zip.getProgressMonitor();
        process.setProgressMonitor(progressMonitor);

        while (progressMonitor.getState() == ProgressMonitor.STATE_BUSY) {
            Thread.sleep(500);
        }
    }

    private static ZipFile getZipFile(ZipInputModel input, String zipPath) throws ZipException {
        ZipFile zip = new ZipFile(zipPath);
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);

        if (input.isEncrypt()) {
            setEncryption(input, parameters);
        }

        // run in a separate thread so we can monitor progress
        zip.setRunInThread(true);
        File file = new File(input.getFileToZip().getAbsolutePath());

        if (file.isDirectory()) {
            zip.addFolder(file, parameters);
        } else {
            zip.addFile(file, parameters);
        }

        return zip;
    }

    private static void setEncryption(ZipInputModel input, ZipParameters parameters) {
        parameters.setEncryptFiles(true);
        parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
        parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
        parameters.setPassword(input.getPassword());
    }

    private static void processOuterZip(ZipInputModel input, ZipProcessModel process, File innerZipFile,
                                        String outerZipName) throws ZipException, InterruptedException {
        process.setOuter(true);
        input.setFileToZip(innerZipFile);
        File zipFile = prepareToZip(input, process, outerZipName);

        // remove inner zip from disk
        innerZipFile.delete();

        // only add reference if user chooses to and process is not cancelled
        if (zipFile != null) {
            process.setReference(new ReferenceModel(input.getTag(), input.getOriginalFile().getName(),
                    zipFile.getName()));
        }
    }
}