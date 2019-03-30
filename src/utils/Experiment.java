package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import datamodel.Graph;
import datamodel.Log;
import datamodel.Trace;
import miner.Flower;
import miner.Miner;
import miner.ParNek;
import utils.Const;
import utils.Const.MeasureName;
import utils.Const.MinerName;
import utils.Eval;
import utils.Stats;

public class Experiment {
	public static void runExperiment(Log log, String logPath, List<MinerName> minerNames, boolean toXml) throws FileNotFoundException {
		if (log == null) {
			return;
		}

		PrintWriter pwResult = new PrintWriter(new File(Const.OUTPUT_RESULT));
		double lowerPrec = getLowerPrec(log, logPath);
		Map<MeasureName, Double> measureMap;
		int maxCount = minerNames.size();
		int count = 1;
		Miner miner;

		pwResult.write("MINER");
		for (MeasureName measureName : Const.MEASURE_ORDER) {
			pwResult.write(Const.FILE_DELIMITER);
			pwResult.write(Const.MEASURE_MAP.get(measureName));
		}
		pwResult.write('\n');

		for (MinerName minerName : minerNames) {
			System.out.println("MINER (" + count + "\\" + maxCount + "):\t" + minerName.toString());
			count++;

			try {
				miner = getMiner(minerName, log);
				measureMap = getMeasures(miner, log.getTraces(), logPath, lowerPrec, toXml? minerName.toString() : null);
				
				pwResult.write(minerName.toString());
				for (MeasureName measureName : Const.MEASURE_ORDER) {
					pwResult.write(Const.FILE_DELIMITER);

					if (measureName == MeasureName.SIZE) {
						pwResult.write(Const.FORMAT_SIZE.format(measureMap.get(measureName)));
					} else {
						pwResult.write(Const.FORMAT_DOUBLE.format(measureMap.get(measureName)));
					}
				}
				pwResult.write('\n');
			} catch (Exception e) {
				PrintWriter pwError = new PrintWriter(new File(Const.OUTPUT_ERROR_LOG));
				pwError.write(e.getMessage());
				pwError.write("\n");
				pwError.write(e.getStackTrace().toString());
				pwError.close();
				pwResult.close();
				return;
			}
		}
		
		pwResult.close();
	}
	
	public static void runExperiment(Log log, String logPath, ParNek miner, boolean toXml) throws FileNotFoundException {
		if (log == null) {
			return;
		}

		PrintWriter pwResult = new PrintWriter(new File(Const.OUTPUT_RESULT));
		double lowerPrec = getLowerPrec(log, logPath);
		Map<MeasureName, Double> measureMap;
		String minerName = "PARNEK";

		pwResult.write("MINER");
		for (MeasureName measureName : Const.MEASURE_ORDER) {
			pwResult.write(Const.FILE_DELIMITER);
			pwResult.write(Const.MEASURE_MAP.get(measureName));
		}
		pwResult.write('\n');

		try {
			measureMap = getMeasures(miner, log.getTraces(), logPath, lowerPrec, toXml? minerName : null);
			
			pwResult.write(minerName.toString());
			for (MeasureName measureName : Const.MEASURE_ORDER) {
				pwResult.write(Const.FILE_DELIMITER);
				
				if (measureName == MeasureName.SIZE) {
					pwResult.write(Const.FORMAT_SIZE.format(measureMap.get(measureName)));
				} else {
					pwResult.write(Const.FORMAT_DOUBLE.format(measureMap.get(measureName)));
				}
			}
			pwResult.write('\n');
		} catch (Exception e) {
			PrintWriter pwError = new PrintWriter(new File(Const.OUTPUT_ERROR_LOG));
			pwError.write(e.getMessage());
			pwError.write("\n");
			pwError.write(e.getStackTrace().toString());
			pwError.close();
			pwResult.close();
			return;
		}
		
		pwResult.close();
	}
	
	private static double getLowerPrec(Log log, String logPath) throws FileNotFoundException {
		double lowerPrecisionBound = 0.0;
		Eval evaluation;
		Graph graph;
		Miner miner;

		try {
			miner = new Flower(log);
			graph = miner.mine();
			evaluation = new Eval(graph, log.getTraces(), logPath);
			lowerPrecisionBound = evaluation.getPrecision();
		} catch (Exception e) {
			System.out.println("WARNING: Errors occured while calculating a lower precision bound (LPB). LPB was automatically set to 0.00.");
		}
		
		return lowerPrecisionBound;
	}

	private static Map<MeasureName, Double> getMeasures(Miner miner, Map<Trace, Integer> traceMap, String logPath, double lowerPrecisionBound, String xmlFileName) throws Exception {
		Map<MeasureName, Double> measures = new HashMap<>();
		Graph graph;
		Eval evaluation;
		int size;
		double precision, normPrecision;

		graph = miner.mine();
		size = graph.getRelationCount();
		evaluation = new Eval(graph, traceMap, logPath, lowerPrecisionBound);		
		precision = evaluation.getPrecision();
		normPrecision = evaluation.getNormalizedPrecision();
		
		measures.put(MeasureName.FITNESS, roundDouble(evaluation.getFitness()));
		measures.put(MeasureName.PRECISION, roundDouble(precision));
		measures.put(MeasureName.NORM_PREC, roundDouble(normPrecision));
		measures.put(MeasureName.SIZE, 1.0 * size);
		measures.put(MeasureName.LOWER_PREC_BOUND, roundDouble(lowerPrecisionBound));

		if (xmlFileName != null) {
			graph.toXml(Const.OUTPUT_DIR + "/" + xmlFileName.toLowerCase() + ".xml");
		}

		return measures;
	}

	private static double roundDouble(double d) {
		return Math.round(d * Math.pow(10, Const.ROUND)) / Math.pow(10, Const.ROUND);
	}

	private static Miner getMiner(MinerName minerName, Log log) throws Exception {
		Miner miner = null;
		int actCount;

		switch (minerName) {
			case FLOWER:
				miner = new Flower(log);
				break;
			case PARNEK:
				miner = new ParNek(log, false, false);
				break;
			case PARNEK_FOLLOW_1:
				miner = new ParNek(log, 1, false, true, false);
				break;
			case PARNEK_FOLLOW_1_IMP:
				miner = new ParNek(log, 1, true, true, false);
				break;
			case PARNEK_FOLLOW_1_NO_ONCE:
				miner = new ParNek(log, 1, false, false, false);
				break;
			case PARNEK_FOLLOW_1_NO_ONCE_IMP:
				miner = new ParNek(log, 1, true, false, false);
				break;
			case PARNEK_FOLLOW_ALL:
				actCount = (new Stats(log.getTraces().keySet())).getActivities().size();
				miner = new ParNek(log, actCount, false, true, false);
				break;
			case PARNEK_FOLLOW_ALL_IMP:
				actCount = (new Stats(log.getTraces().keySet())).getActivities().size();
				miner = new ParNek(log, actCount, true, true, false);
				break;
			case PARNEK_FOLLOW_ALL_NO_ONCE:
				actCount = (new Stats(log.getTraces().keySet())).getActivities().size();
				miner = new ParNek(log, actCount, false, false, false);
				break;
			case PARNEK_FOLLOW_ALL_NO_ONCE_IMP:
				actCount = (new Stats(log.getTraces().keySet())).getActivities().size();
				miner = new ParNek(log, actCount, true, false, false);
				break;
			case PARNEK_COND:
				miner = new ParNek(log, false, true);
				break;
			case PARNEK_FOLLOW_1_COND:
				miner = new ParNek(log, 1, false, true, true);
				break;
			case PARNEK_FOLLOW_1_IMP_COND:
				miner = new ParNek(log, 1, true, true, true);
				break;
			case PARNEK_FOLLOW_1_NO_ONCE_COND:
				miner = new ParNek(log, 1, false, false, true);
				break;
			case PARNEK_FOLLOW_1_NO_ONCE_IMP_COND:
				miner = new ParNek(log, 1, true, false, true);
				break;
			case PARNEK_FOLLOW_ALL_COND:
				actCount = (new Stats(log.getTraces().keySet())).getActivities().size();
				miner = new ParNek(log, actCount, false, true, true);
				break;
			case PARNEK_FOLLOW_ALL_IMP_COND:
				actCount = (new Stats(log.getTraces().keySet())).getActivities().size();
				miner = new ParNek(log, actCount, true, true, true);
				break;
			case PARNEK_FOLLOW_ALL_NO_ONCE_COND:
				actCount = (new Stats(log.getTraces().keySet())).getActivities().size();
				miner = new ParNek(log, actCount, false, false, true);
				break;
			case PARNEK_FOLLOW_ALL_NO_ONCE_IMP_COND:
				actCount = (new Stats(log.getTraces().keySet())).getActivities().size();
				miner = new ParNek(log, actCount, true, false, true);
				break;
			default:
				throw new Error("WARNING: No support for '" + minerName + "' miner.");
		}
	
		return miner;
	}
}
