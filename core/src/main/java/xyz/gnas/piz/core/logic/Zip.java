package xyz.gnas.piz.core.logic;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;

import xyz.gnas.piz.core.models.Abbreviation;

public class Zip {
	public static SortedMap<Abbreviation, Abbreviation> getAbbreviationList(List<File> fileList, boolean isObfuscated) {
		SortedMap<Abbreviation, Abbreviation> abbreviationList = new TreeMap<Abbreviation, Abbreviation>();

		for (File file : fileList) {
			// remove trailing spaces and replace multiple spaces by one space
			String fileName = file.getName().trim().replaceAll(" +", " ");
			String zipFileName = isObfuscated ? getAbbreviatedFileName(fileName, file.isDirectory())
					: FilenameUtils.removeExtension(fileName);
			Abbreviation abbreviation = new Abbreviation(zipFileName);

			if (abbreviationList.containsKey(abbreviation)) {
				abbreviation = abbreviationList.get(abbreviation);
			}

			abbreviation.getFileAbbreviationMap().put(file, abbreviation.getAbbreviation());
			abbreviationList.put(abbreviation, abbreviation);
		}

		if (isObfuscated) {
			abbreviationList = generateObfuscatedAbbreviationList(abbreviationList);
		}

		return abbreviationList;
	}

	/**
	 * @description Most simple case of abbreviation
	 * @date Oct 9, 2018
	 * @param fileName name of the orinal file
	 * @return
	 */
	private static String getAbbreviatedFileName(String fileName, boolean isFolder) {
		String delimiter = " ";
		String[] split = isFolder ? fileName.split(delimiter)
				: FilenameUtils.removeExtension(fileName).split(delimiter);
		StringBuilder sb = new StringBuilder();

		// get the first character of each word in upper case and append to result
		for (String word : split) {
			sb.append(word.substring(0, 1));
		}

		return sb.toString().toUpperCase();
	}

	private static SortedMap<Abbreviation, Abbreviation> generateObfuscatedAbbreviationList(
			SortedMap<Abbreviation, Abbreviation> abbreviationList) {
		for (Abbreviation abbreviation : abbreviationList.keySet()) {
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
	 * @description Create unique abbreviations when there are multiple
	 *              files/folders with the same abbreviation under the most simple
	 *              case
	 * @date Oct 9, 2018
	 */
	private static void uniquifyAbbreviationList(SortedMap<Abbreviation, Abbreviation> abbreviationList) {
		for (Abbreviation abbreviation : abbreviationList.keySet()) {
			Map<File, String> map = abbreviation.getFileAbbreviationMap();

			if (map.size() > 1) {
				// first try to add file extension to remove duplicate
				Map<File, String> uniqueMap = uniquifyByExtension(abbreviation);

				// map that contains original files and their rebuilt names used for
				// abbreviation
				Map<File, String> fileRebuiltNameMap = new HashMap<File, String>();

				for (File file : map.keySet()) {
					fileRebuiltNameMap.put(file, file.getName());
				}

				int characterCount = 1;

				// if there are still duplicates, add a character recursively until there are no
				// duplicates
				while (uniqueMap.values().stream().distinct().count() < uniqueMap.keySet().size()
						&& characterCount < abbreviation.getLongestWordLength()) {
					uniqueMap = uniquifyByAddingCharacters(uniqueMap, characterCount, fileRebuiltNameMap);
					++characterCount;
				}

				abbreviation.setFileAbbreviationMap(uniqueMap);
			}
		}
	}

	private static Map<File, String> uniquifyByExtension(Abbreviation abbreviation) {
		Map<File, String> mapWithExtension = new HashMap<File, String>();
		Map<File, String> map = abbreviation.getFileAbbreviationMap();

		for (File file : map.keySet()) {
			String fileName = map.get(file);

			if (file.isDirectory()) {
				mapWithExtension.put(file, fileName);
			} else {
				String name = file.getName();
				String abbreviatedName = getAbbreviatedFileName(name, false);
				mapWithExtension.put(file, abbreviatedName + "_" + FilenameUtils.getExtension(name));
			}
		}

		return mapWithExtension;
	}

	private static Map<File, String> uniquifyByAddingCharacters(Map<File, String> map, int characterCount,
			Map<File, String> fileRebuiltNameMap) {
		// temporary new abbreviation list
		Map<Abbreviation, Abbreviation> newAbbreviationList = getNewAbbreviationList(map);
		updateNewAbbreviationList(newAbbreviationList, characterCount, fileRebuiltNameMap);
		Map<File, String> result = new HashMap<File, String>();

		for (Abbreviation abbreviation : newAbbreviationList.keySet()) {
			Map<File, String> fileAbbreviationMap = abbreviation.getFileAbbreviationMap();

			for (File file : fileAbbreviationMap.keySet()) {
				String newAbbreviatedName = map.get(file);

				// get abbreviation of each file in abbreviation with duplicates using their
				// rebuilt name
				if (fileAbbreviationMap.size() > 1) {
					newAbbreviatedName = getAbbreviatedFileName(fileRebuiltNameMap.get(file), file.isDirectory());
				}

				result.put(file, newAbbreviatedName);
			}
		}

		return result;
	}

	private static Map<Abbreviation, Abbreviation> getNewAbbreviationList(Map<File, String> map) {
		Map<Abbreviation, Abbreviation> newAbbreviationList = new HashMap<Abbreviation, Abbreviation>();

		for (String value : map.values()) {
			Abbreviation abbreviation = new Abbreviation(value);

			if (newAbbreviationList.containsKey(abbreviation)) {
				abbreviation = newAbbreviationList.get(abbreviation);
			}

			for (File file : map.keySet()) {
				if (map.get(file).equalsIgnoreCase(value)) {
					abbreviation.getFileAbbreviationMap().put(file, value);
				}
			}

			newAbbreviationList.put(abbreviation, abbreviation);
		}

		return newAbbreviationList;
	}

	private static void updateNewAbbreviationList(Map<Abbreviation, Abbreviation> newAbbreviationList,
			int characterCount, Map<File, String> fileRebuiltNameMap) {
		for (Abbreviation abbreviation : newAbbreviationList.keySet()) {
			Map<File, String> map = abbreviation.getFileAbbreviationMap();

			// rebuild the original file names of abbreviation with multiple files
			if (map.size() > 1) {
				for (File file : map.keySet()) {
					String[] split = FilenameUtils.removeExtension(file.getName()).split(" ");
					StringBuilder sb = new StringBuilder();

					for (String word : split) {
						// separate each consecutive character by a space
						for (int i = 0; i <= characterCount && i < word.length(); ++i) {
							if (i > 0) {
								sb.append(" ");
							}

							sb.append(word.charAt(i));
						}
					}

					fileRebuiltNameMap.put(file, sb.toString().toUpperCase());
				}
			}
		}
	}

	private static SortedMap<Abbreviation, Abbreviation> mergeDuplicateAbbreviations(
			SortedMap<Abbreviation, Abbreviation> abbreviationList) {
		SortedMap<Abbreviation, Abbreviation> newAbreviationList = new TreeMap<Abbreviation, Abbreviation>();

		for (Abbreviation abbreviation : abbreviationList.keySet()) {
			Map<File, String> map = abbreviation.getFileAbbreviationMap();

			for (File file : map.keySet()) {
				// create a new Abbreviation object for each newly generated abbreviation
				Abbreviation newAbbreviation = new Abbreviation(map.get(file));

				if (newAbreviationList.containsKey(newAbbreviation)) {
					newAbbreviation = newAbreviationList.get(newAbbreviation);
				}

				newAbbreviation.getFileAbbreviationMap().put(file, newAbbreviation.getAbbreviation());
				newAbreviationList.put(newAbbreviation, newAbbreviation);
			}
		}

		return newAbreviationList;
	}
}
