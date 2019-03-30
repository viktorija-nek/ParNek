package utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import utils.Const.MinerType;

public class Util {
	public static int getMapSize(Map<String, Set<String>> map) {
		int size = 0;
		
		for (String key : map.keySet()) {
			size += map.get(key).size();
		}
		
		return size;
	}
	
    public static Map<String, Set<String>> mapDeepCopy(Map<String, Set<String>> original) {
        Map<String, Set<String>> copy = new HashMap<String, Set<String>>();

        for (Entry<String, Set<String>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new HashSet<String>(entry.getValue()));
        }

        return copy;
    }
    
    public static boolean isXes(String fileName) {
		if (fileName == null) {
			return false;
		} else {
			int length = fileName.length();
			
			if (length < 5) {
				return false;
			} else {
				return fileName.substring(length - 4, length).equals(".xes");
			}
		}
	}
    
    public static MinerType checkFlagsAndReturnMiner(Set<String> flags) {
    	MinerType miner = MinerType.PARNEK;
    	
    	if (!flags.isEmpty()) {
			Set<String> legalFlags = new HashSet<>(Arrays.asList(Const.FLAG_ALL, Const.FLAG_F1, Const.FLAG_FA, Const.FLAG_COND, Const.FLAG_OPT, Const.FLAG_CHECK, Const.FLAG_XML));
			legalFlags.addAll(Const.FLAG_HELP);
			Set<String> illegalFlags = new HashSet<>(flags);
			illegalFlags.removeAll(legalFlags);
			
			// illegal flags
			if (!illegalFlags.isEmpty()) {
				int count = 1;
				int illegalCount = illegalFlags.size();
				
				System.out.print("ERROR: illegal arguments: ");
				for (String s : illegalFlags) {
					System.out.print(s);
					
					if (count < illegalCount) {
						System.out.print(", ");
					} else {
						System.out.println(".");
					}
					
					count++;
				}

				return MinerType.ERROR;
			}
			
			// print HELP information
			for (String help : Const.FLAG_HELP) {
				if (flags.contains(help)) {
					System.out.println("Usage: java -jar parnek.jar <xesfile> [args...]");
					System.out.println("");
					System.out.println("Arguments include:");
					System.out.println("   -all      run all the possible miners");
					System.out.println("   -inex     run ParNek's extension Inex which mines inclusions and exclusions differently than the core algorithm");
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

					return MinerType.ERROR;
				}
			}
			
			// there should be only one type of mining
			int countMinerOptions = 0;

			for (String flag : flags) {
				if (flag.equals(Const.FLAG_ALL) || flag.equals(Const.FLAG_F1) || flag.equals(Const.FLAG_FA)) {
					countMinerOptions++;
	
					if (countMinerOptions > 1) {
						System.out.println("ERROR: You have to choose only one type of miner: " + Const.FLAG_ALL + ", " + Const.FLAG_F1 + " or " + Const.FLAG_FA + ".");
						return MinerType.ERROR;
					}
					
					switch (flag) {
						case Const.FLAG_ALL:
							if (flags.contains(Const.FLAG_COND) || flags.contains(Const.FLAG_OPT) || flags.contains(Const.FLAG_CHECK)) {
								System.out.println("ERROR: " + Const.FLAG_ALL + " cannot be combined with arguments: " + Const.FLAG_COND + ", " + Const.FLAG_OPT + ", and " + Const.FLAG_CHECK + ".");
								return MinerType.ERROR;
							}
							
							miner = MinerType.ALL;
							break;
						case Const.FLAG_F1:
							miner = MinerType.F1;
							break;
						case Const.FLAG_FA:
							miner = MinerType.FA;
							break;
					}
				}
			}
    	}
    	
    	return miner;
    }
}
