package utils;

import java.io.File;
import java.util.List;

import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.model.XLog;

public class XesLoader {
	
	public static List<XLog> load(String path) throws Exception {
		File file = read(path);
		List<XLog> xLogs = null;
		XesXmlParser xesXmlParser = new XesXmlParser();
		XesXmlGZIPParser xesXmlGZIPParser = new XesXmlGZIPParser();
		if (xesXmlParser.canParse(file)) {
			xLogs = parse(file, xesXmlParser);
		} else if (xesXmlGZIPParser.canParse(file)) {
			xLogs = parse(file, xesXmlGZIPParser);
		} else {
			System.out.println("File format can not be parsed.");
		}
		return xLogs;
	}
	
	public static boolean canParse(String path) {
		File file = read(path);
		XesXmlParser xesXmlParser = new XesXmlParser();
		XesXmlGZIPParser xesXmlGZIPParser = new XesXmlGZIPParser();
		if (xesXmlParser.canParse(file)) {
			return true;
		} else if (xesXmlGZIPParser.canParse(file)) {
			return true;
		} else {
			return false;
		}
	}

	private static File read(String path) throws NullPointerException {
		File file = null;
		try {
			file = new File(path);
		} catch (NullPointerException e) {
			System.out.println("Problem loading file: " + path);
			e.printStackTrace();
			throw e;
		}
		return file;
	}
	
	private static List<XLog> parse(File file, XesXmlParser parser) throws Exception {
		List<XLog> xLogs = null;
		try {
			xLogs = parser.parse(file);
		} catch (Exception e) {
			System.out.println("Unexpected problem parsing file");
			e.printStackTrace();
			throw e;
		}
		return xLogs;
	}
	
	private static List<XLog> parse(File file, XesXmlGZIPParser parser) throws Exception {
		List<XLog> xLogs = null;
		try {
			xLogs = parser.parse(file);
		} catch (Exception e) {
			System.out.println("Unexpected problem parsing file");
			e.printStackTrace();
			throw e;
		}
		return xLogs;
	}
}
