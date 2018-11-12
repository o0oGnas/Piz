package xyz.gnas.piz.core.logic;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import xyz.gnas.piz.core.models.ReferenceModel;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ReferenceLogic {
	public static List<ReferenceModel> loadReferences(String filePath)
			throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		File file = new File(filePath);

		if (file.exists()) {
			ReferenceModel[] zipArray = mapper.readValue(file, ReferenceModel[].class);
			return List.of(zipArray);
		} else {
			return new LinkedList<>();
		}
	}

	public static void saveReferences(List<ReferenceModel> referencesList, String filePath)
			throws IOException {
		File file = new File(filePath);
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
		prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
		mapper.writeValue(file, referencesList.toArray());
	}
}
