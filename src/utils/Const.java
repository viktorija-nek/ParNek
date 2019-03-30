package utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Const {
	// -----------------------------------------------
	// -------------------- ENUMs --------------------
	// -----------------------------------------------
	public enum MinerType {
		ERROR, ALL, PARNEK, F1, FA
	}

	public enum RelationType {
		CONDITION, RESPONSE, EXCLUDE, INCLUDE
	}

	public enum MinerName {
		FLOWER, PARNEK, 
		PARNEK_FOLLOW_1, PARNEK_FOLLOW_1_IMP, PARNEK_FOLLOW_1_NO_ONCE, PARNEK_FOLLOW_1_NO_ONCE_IMP,
		PARNEK_FOLLOW_ALL, PARNEK_FOLLOW_ALL_IMP, PARNEK_FOLLOW_ALL_NO_ONCE, PARNEK_FOLLOW_ALL_NO_ONCE_IMP, PARNEK_COND,
		PARNEK_FOLLOW_1_COND, PARNEK_FOLLOW_1_IMP_COND, PARNEK_FOLLOW_1_NO_ONCE_COND, PARNEK_FOLLOW_1_NO_ONCE_IMP_COND,
		PARNEK_FOLLOW_ALL_COND, PARNEK_FOLLOW_ALL_IMP_COND, PARNEK_FOLLOW_ALL_NO_ONCE_COND, PARNEK_FOLLOW_ALL_NO_ONCE_IMP_COND
	}
	
	public enum MeasureName {
		FITNESS, PRECISION, LOWER_PREC_BOUND, NORM_PREC, SIZE
	}

	// ------------------------------------------------
	// -------------------- CONSTs --------------------
	// ------------------------------------------------
	public static final double ROUND = 4;
	public static final char FILE_DELIMITER = ',';
	public static final DecimalFormat FORMAT_DOUBLE = new DecimalFormat("0.0000");
	public static final DecimalFormat FORMAT_SIZE = new DecimalFormat("0");
	public static final String OUTPUT_DIR = "./output";
	public static final String OUTPUT_RESULT = OUTPUT_DIR + "/results.csv";
	public static final String OUTPUT_ERROR_LOG = OUTPUT_DIR + "/log.txt";
	public static final String ERROR_EXPERIMENT = "ERROR. Find more details in " + OUTPUT_ERROR_LOG;
	
	public static final String FLAG_ALL = "-all";
	public static final String FLAG_F1 = "-f1";
	public static final String FLAG_FA = "-fa";
	public static final String FLAG_COND = "-cond";
	public static final String FLAG_OPT = "-opt";
	public static final String FLAG_CHECK = "-check";
	public static final String FLAG_XML = "-xml";
	public static final Set<String> FLAG_HELP = new HashSet<>(Arrays.asList("-help", "-h", "-?"));
	
	public static final List<MeasureName> MEASURE_ORDER = new ArrayList<>(Arrays.asList(
		MeasureName.PRECISION,
		MeasureName.NORM_PREC,
		MeasureName.SIZE,
		MeasureName.FITNESS,
		MeasureName.LOWER_PREC_BOUND
	));

	public static final Map<MeasureName, String> MEASURE_MAP;
	static
    {
		MEASURE_MAP = new HashMap<>();
		MEASURE_MAP.put(MeasureName.PRECISION, "PRECISION");
		MEASURE_MAP.put(MeasureName.NORM_PREC, "NORM. PREC.");
		MEASURE_MAP.put(MeasureName.SIZE, "SIZE");
		MEASURE_MAP.put(MeasureName.FITNESS, "FITNESS");
		MEASURE_MAP.put(MeasureName.LOWER_PREC_BOUND, "LOWER PREC. BOUND");
    }
}
