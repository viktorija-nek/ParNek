import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import datamodel.Log;
import miner.ParNek;
import utils.Util;
import utils.Const;
import utils.Const.MinerName;
import utils.Const.MinerType;
import utils.Experiment;

public class Main {
	
	
	public static void main(String[] args) throws Exception {
		Set<String> flags = new HashSet<>(Arrays.asList(args));
		
		if (args.length != 0) {
			// print HELP information
			for (String help : Const.FLAG_HELP) {
				if (flags.contains(help)) {
					
					System.out.println("Copyright 2018");
					System.out.println("All rights reserved.");
					System.out.println("");
					System.out.println("*************************************");
					System.out.println("*  _____           _   _      _     *");
					System.out.println("* |  __ \\         | \\ | |    | |    *");
					System.out.println("* | |__) |_ _ _ __|  \\| | ___| | __ *");
					System.out.println("* |  ___/ _` | '__| . ` |/ _ \\ |/ / *");
					System.out.println("* | |  | (_| | |  | |\\  |  __/   <  *");
					System.out.println("* |_|   \\__,_|_|  |_| \\_|\\___|_|\\_\\ *");
					System.out.println("*                                   *");
					System.out.println("*************************************");                       
					System.out.println("");
					System.out.println("version 1.0.0");
					System.out.println("");
					System.out.println("Developed by:");
					System.out.println("");
					System.out.println("  Viktorija Nekrasaite, viktorijanekrasaite3@gmail.com");
					System.out.println("  Andrew Parli, drewparli@gmail.com");
					System.out.println("");
					System.out.println("");
					System.out.println("");
					System.out.println("Usage: java -jar parnek.jar <xesfile> [args...]");
					System.out.println("");
					System.out.println("Arguments include:");
					System.out.println("   -all      run all the possible miners");
					System.out.println("   -f1       run ParNek's extension where excluded activity can be included by only one activity");
					System.out.println("   -fa       run ParNek's extension where excluded activity can be included by all the activities");
					System.out.println("   -cond     mine additionl conditions");
					System.out.println("   -opt      optimize exclusion relations (only combined with -f1 or -fa)");
					System.out.println("   -check    check all the activities while searching for possible exclusions (only combined with -f1 or -fa)");
					System.out.println("   -xml      save a graph to XML file");
					System.out.println("");
					System.out.println("   -help     print this help message");
					System.out.println("   -h        print this help message");
					System.out.println("   -?        print this help message");

					return;
				}
			}
		}

		if (new File(Const.OUTPUT_DIR).exists()) {
			System.out.println("ERROR: 'output' directory already exists. Please delete/rename 'output' directory and rerun again.");
			return;
		} else {
			if (args.length == 0) {
				System.out.println("ERROR: no file. Please provide XES log file.");
				return;
			} else {
				String logFilePath = args[0];

				if (!Util.isXes(logFilePath)) {
					System.out.println("ERROR: illegal file. Please provide XES log file.");
					return;
				}

				flags.remove(logFilePath);
				MinerType minerType = Util.checkFlagsAndReturnMiner(flags);
				
				if (minerType.equals(MinerType.ERROR)) {
					return;
				}
				
				Log log = new Log(logFilePath);

				try {
					log.parse();
				} catch (Exception e) {
					try {
						PrintWriter pwError = new PrintWriter(new File(Const.OUTPUT_ERROR_LOG));
						pwError.write(e.getMessage());
						pwError.write("\n");
						pwError.write(e.getStackTrace().toString());
						pwError.close();
						return;
					} catch (FileNotFoundException ex) {
						System.out.println("ERROR: File not found exception.");
						ex.printStackTrace();
						return;
					}
				}

				new File(Const.OUTPUT_DIR).mkdirs();
				
				try {
					switch (minerType) {
						case ALL:
							Experiment.runExperiment(log, logFilePath, new ArrayList<MinerName>(Arrays.asList(MinerName.values())), flags.contains(Const.FLAG_XML));
							break;
						case PARNEK:
							Experiment.runExperiment(log, logFilePath, new ParNek(log, false, flags.contains(Const.FLAG_COND)), flags.contains(Const.FLAG_XML));
							break;
						case F1:
							Experiment.runExperiment(log, logFilePath, new ParNek(log, 1, flags.contains(Const.FLAG_OPT), flags.contains(Const.FLAG_CHECK), flags.contains(Const.FLAG_COND)), flags.contains(Const.FLAG_XML));
							break;
						case FA:
							Experiment.runExperiment(log, logFilePath, new ParNek(log, Integer.MAX_VALUE, flags.contains(Const.FLAG_OPT), flags.contains(Const.FLAG_CHECK), flags.contains(Const.FLAG_COND)), flags.contains(Const.FLAG_XML));
							break;
						default:
							return;
					}
				} catch (FileNotFoundException ex) {
					System.out.println("ERROR: File not found exception.");
					ex.printStackTrace();
				}	
			}
		}
		
		System.out.println("");
		System.out.println("INFORMATION: see program's output in " + Const.OUTPUT_DIR + ".");
	}
}