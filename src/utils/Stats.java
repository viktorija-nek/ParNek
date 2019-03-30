package utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import datamodel.Trace;

public class Stats {

	public enum RelationType {
		RESPONSE, PRECEDENCE, PREDECESSOR, SUCCESSOR
	}

	private Set<Trace> traces;
	private Set<String> activities; // set of activity labels in the event log

    private Set<String> atMostOnce; // a set of activities which occurs at most once in each trace in the log
    private Set<String> selfRepeated;

    private Map<String, Set<String>> response;
    private Map<String, Set<String>> precedence;
    private Map<String, Set<String>> alternatePrecedence;
    private Map<String, Set<String>> chainPrecedenceMap;
    private Map<String, String> chainPrecedence;

    private Map<String, Set<String>> predecessor;
    private Map<String, Set<String>> predecessorFirst;
    private Map<String, Set<String>> successor;

    private Map<Boolean, Map<String, Set<String>>> possibleOneExcludesAnother = new HashMap<>();

	public Stats(Set<Trace> traces) {
		this.traces = traces;
	}

	public void calculateStats()  {
		List<String> traceActivities;
		Set<String> notAtMostOnce = new HashSet<>();
		Set<String> alternatePrecedenceTrace = new HashSet<>();
		Map<String, Set<String>> notChainPrecedence = new HashMap<>();
		Map<RelationType, Map<String, Set<String>>> traceRelations;
		String key;

		activities = new HashSet<>();
		precedence = new HashMap<>();
		alternatePrecedence = new HashMap<>();
		predecessor = new HashMap<>();
		successor = new HashMap<>();

		for (Trace trace : traces) {
			traceActivities = trace.getActivities();
			activities.addAll(new HashSet<>(traceActivities));

			for (String act : new HashSet<>(traceActivities)) {
				if (!notAtMostOnce.contains(act)) {
					if (countOccurrences(traceActivities, act) > 1) {
						notAtMostOnce.add(act);
					}
				}
			}

			traceRelations = getTraceRelations(traceActivities);

			// calculate an intersection of all the responses
			if (response == null) {
				response = traceRelations.get(RelationType.RESPONSE);
			} else {
				for (Entry<String, Set<String>> entry : traceRelations.get(RelationType.RESPONSE).entrySet()) {
        			key = entry.getKey();

					if (!response.containsKey(key)) {
        				response.put(key, entry.getValue());
        			} else {
        				response.get(key).retainAll(entry.getValue());
        			}
        		}
			}

			// calculate an intersection of all the precedences
			if (precedence == null) {
				precedence = traceRelations.get(RelationType.PRECEDENCE);
			} else {
				for (Entry<String, Set<String>> entry : traceRelations.get(RelationType.PRECEDENCE).entrySet()) {
					key = entry.getKey();

					if (!precedence.containsKey(key)) {
        				precedence.put(key, entry.getValue());
        			} else {
        				precedence.get(key).retainAll(entry.getValue());
        			}

					alternatePrecedenceTrace = new HashSet<>(precedence.get(key));

		            for (String act : precedence.get(key)) {
		            	if (!inEverySet(splitTraceBeforeActivity(traceActivities, key), act)) {
		            		alternatePrecedenceTrace.remove(act);
		            	}
		            }

					if (!alternatePrecedence.containsKey(key)) {
						alternatePrecedence.put(key, alternatePrecedenceTrace);
        			} else {
        				alternatePrecedence.get(key).retainAll(alternatePrecedenceTrace);
        			}

					for (String act : entry.getValue()) {
						if (!aPrecededByB(traceActivities, key, act)) {
		            		if (!notChainPrecedence.containsKey(key)) {
		            			notChainPrecedence.put(key, new HashSet<>());
		            		}

		            		notChainPrecedence.get(key).add(act);
		            	}
					}
        		}
			}

			// calculate a union of all the predecessors
			if (predecessor == null) {
				predecessor = traceRelations.get(RelationType.PREDECESSOR);
			} else {
				for (Entry<String, Set<String>> entry : traceRelations.get(RelationType.PREDECESSOR).entrySet()) {
					key = entry.getKey();

					if (!predecessor.containsKey(key)) {
						predecessor.put(key, entry.getValue());
        			} else {
        				predecessor.get(key).addAll(entry.getValue());
        			}
        		}
			}
			
			// calculate a union of all the predecessors
			if (predecessorFirst == null) {
				predecessorFirst = traceRelations.get(RelationType.PREDECESSOR);
			} else {
				for (Entry<String, Set<String>> entry : traceRelations.get(RelationType.PREDECESSOR).entrySet()) {
					key = entry.getKey();

					if (!predecessorFirst.containsKey(key)) {
						predecessorFirst.put(key, entry.getValue());
        			} else {
        				predecessorFirst.get(key).addAll(entry.getValue());
        			}
        		}
			}

			// calculate a union of all the successors
			if (successor == null) {
				successor = traceRelations.get(RelationType.SUCCESSOR);
			} else {
				for (Entry<String, Set<String>> entry : traceRelations.get(RelationType.SUCCESSOR).entrySet()) {
					key = entry.getKey();

					if (!successor.containsKey(key)) {
						successor.put(key, entry.getValue());
        			} else {
        				successor.get(key).addAll(entry.getValue());
        			}
        		}
			}
		}

		removeEmptySetsFromMap(response);
		removeEmptySetsFromMap(precedence);
		removeEmptySetsFromMap(alternatePrecedence);
		removeEmptySetsFromMap(predecessor);
		removeEmptySetsFromMap(successor);

		atMostOnce = new HashSet<>(activities);
		chainPrecedenceMap = mapDeepCopy(precedence);

		for (String act : notAtMostOnce) {
			atMostOnce.remove(act);
		}

		for (String act : notChainPrecedence.keySet()) {
			if (chainPrecedenceMap.containsKey(act)) {
				chainPrecedenceMap.get(act).removeAll(notChainPrecedence.get(act));
			}
		}

		removeEmptySetsFromMap(chainPrecedenceMap);
	}

	public Map<String, Set<String>> getPossibleOneExcludesAnother(boolean checkAll) {
		if (!possibleOneExcludesAnother.containsKey(checkAll)) {
			possibleOneExcludesAnother.put(checkAll, new HashMap<>());
			boolean foundAtLeastOnceAB;
			Set<String> toSet;
			
			if (checkAll) {
				toSet = new HashSet<>(activities);
			} else {
				Set<String> notAtMostOnce = new HashSet<>(activities);
				notAtMostOnce.removeAll(atMostOnce);
				toSet = new HashSet<>(notAtMostOnce);
			}

			for (String from : activities) {
				for (String to : toSet) {
					foundAtLeastOnceAB = false;

					for (Trace trace : traces) {
						if (atLeastOnceAB(trace.getActivities(), from, to)) {
							foundAtLeastOnceAB = true;
							break;
						}
					}

					if (!foundAtLeastOnceAB) {
						if (!possibleOneExcludesAnother.get(checkAll).containsKey(from)) {
							possibleOneExcludesAnother.get(checkAll).put(from, new HashSet<>());
						}

						possibleOneExcludesAnother.get(checkAll).get(from).add(to);
					}
				}
			}
		}

		return possibleOneExcludesAnother.get(checkAll);
	}
	
	public Map<String, Set<Integer>> getRepetitionMap(Set<Trace> traces) {
		Map<String, Set<Integer>> repetitionMap = new HashMap<>();
		List<String> activities;
		int count = 1;
		
		for (String activity : getActivities()) {
			repetitionMap.put(activity, new HashSet<>());
		}
		
		for (Trace trace: traces) {
			activities = trace.getActivities();

            for (int i = 1; i < activities.size(); i++) {
                if (!activities.get(i-1).equals(activities.get(i))) {
                	repetitionMap.get(activities.get(i-1)).add(count);
                	count = 1;
                } else {
                	count++;
                }
            }
            
            repetitionMap.get(activities.get(activities.size() - 1)).add(count);
		}

		return repetitionMap;
	}

	/** Returns the set of activity labels in the event log. */
	public Set<String> getActivities() {
		if (activities == null) {
			calculateStats();
		}

		return activities;
	}

	/** at_most_one - */
	public Set<String> getAtMostOnce() {
		if (atMostOnce == null) {
			calculateStats();
		}

		return atMostOnce;
	}

	/** response - a dictionary (key, set) where key is followed by all the activities in the set */
	public Map<String, Set<String>> getResponse() {
		if (response == null) {
			calculateStats();
		}

		return response;
	}

	/** precedence - a dictionary (key, set) where key is preceded by all the activities in the set */
	public Map<String, Set<String>> getPrecedence() {
		if (precedence == null) {
			calculateStats();
		}

		return precedence;
	}

	public Map<String, Set<String>> getAlternatePrecedence() {
		if (alternatePrecedence == null) {
			calculateStats();
		}

		return alternatePrecedence;
	}

	/** precedence - a dictionary (key, act) where key is immediately preceded by act in the log
	 * @throws Exception */
	public Map<String, String> getChainPrecedence() throws Exception {
		if (chainPrecedence == null) {
			chainPrecedence = new HashMap<>();

			if (chainPrecedenceMap == null) {
				calculateStats();
			}

			for (String act : chainPrecedenceMap.keySet()) {
				if (chainPrecedenceMap.get(act).size() > 1) {
					throw new Exception("Activity " + act + " CANNOT be immediately preceded by several activities: " + chainPrecedenceMap.get(act));
				}
				chainPrecedence.put(act, chainPrecedenceMap.get(act).iterator().next());
			}
		}

		return chainPrecedence;
	}

	/** predecessor - a dictionary (key, set) with set of activities that comes before the last key in the log */
	public Map<String, Set<String>> getPredecessor() {
		if (predecessor == null) {
			calculateStats();
		}

		return predecessor;
	}
	
	/** predecessor - a dictionary (key, set) with set of activities that comes before the last key in the log */
	public Map<String, Set<String>> getPredecessorFirst() {
		if (predecessorFirst == null) {
			calculateStats();
		}

		return predecessorFirst;
	}

	/** successor - a dictionary (key, set) with set of activities that comes after the first key in the log */
	public Map<String, Set<String>> getSuccessor() {
		if (successor == null) {
			calculateStats();
		}

		return successor;
	}

	/**
	 * response - a dictionary (key, set) where key is followed by all the activities in the set
	 * precedence - a dictionary (key, set) where key is preceded by all the activities in the set
	 * predecessor - a dictionary (key, set) with set of activities that comes before the last key in the log
	 * successor - a dictionary (key, set) with set of activities that comes after the first key in the log
	 */
	private Map<RelationType, Map<String, Set<String>>> getTraceRelations(List<String> trace) {
		Map<RelationType, Map<String, Set<String>>> result = new HashMap<>();
		Map<String, Set<String>> responses = new HashMap<>();
		Map<String, Set<String>> precedences = new HashMap<>();
		Map<String, Set<String>> predecessors = new HashMap<>();
		Map<String, Set<String>> successors = new HashMap<>();
		Set<String> uniqueActivities = new HashSet<>(trace);
		int foundAt = trace.size();
		String act;

		// create responses(key, set) which shows what activities follows after the last occurrence of key in the trace
		// create predecessors(key, set) which shows what activities comes before the last occurrence of key in the trace
		while (!uniqueActivities.isEmpty()) {
			act = uniqueActivities.iterator().next();

			for (int i = trace.size() - 1; i >= 0; i--) {
				if (trace.get(i).equals(act)) {
					foundAt = i;
					break;
				}
			}

			responses.put(act, new HashSet<>(trace.subList(foundAt + 1, trace.size())));
			predecessors.put(act, new HashSet<>(trace.subList(0, foundAt)));
			uniqueActivities.remove(act);
		}

		uniqueActivities = new HashSet<>(trace);

		// create precedences(key, set) which shows what activities comes before the first occurrence of key in the trace
		// create successors(key, set) which shows what activities follows after the first occurrence of key in the trace
		while (!uniqueActivities.isEmpty()) {
			act = uniqueActivities.iterator().next();

			for (int i = 0; i < trace.size(); i++) {
				if (trace.get(i).equals(act)) {
					foundAt = i;
					break;
				}
			}

			precedences.put(act, new HashSet<>(trace.subList(0, foundAt)));
			successors.put(act, new HashSet<>(trace.subList(foundAt + 1, trace.size())));
			uniqueActivities.remove(act);
		}

		result.put(RelationType.RESPONSE, responses);
		result.put(RelationType.PREDECESSOR, predecessors);
		result.put(RelationType.PRECEDENCE, precedences);
		result.put(RelationType.SUCCESSOR, successors);

		return result;
	}
	
	public Map<String, Set<String>> getIncludedBy(Set<Trace> traces) {
		selfRepeated = new HashSet<>();
		Map<String, Set<String>> includedBy = new HashMap<>();
		List<String> traceActs;

		for (Trace trace : traces) {
			traceActs = trace.getActivities();
			
			for (int i = 1; i < traceActs.size(); i++) {
				if (traceActs.get(i).equals(traceActs.get(i - 1))) {
					selfRepeated.add(traceActs.get(i));
				}
				
				if (!includedBy.containsKey(traceActs.get(i))) {
					includedBy.put(traceActs.get(i), new HashSet<>());
				}
				
				includedBy.get(traceActs.get(i)).add(traceActs.get(i - 1));
			}
		}
		
		return includedBy;
	}
	
	public Set<String> getSelfRepeated(Set<Trace> traces) {
		if (selfRepeated == null) {
			getIncludedBy(traces);
		}
		
		return selfRepeated;
	}

	public void printStatistics() throws Exception {
		System.out.println("---------- STATISTICS ----------");
		System.out.println("--------------------------------");
		System.out.println("ACTIVITIES:\t\t" + getActivities().size());
		System.out.println("AT MOST ONCE:\t\t" + getAtMostOnce().size());
		System.out.println("RESPONSE:\t\t" + getResponse().size());
		System.out.println("PRECEDENCE:\t\t" + getPrecedence().size());
		System.out.println("CHAIN PRECEDENCE:\t" + getChainPrecedence().size());
		System.out.println("PREDECESSOR:\t\t" + getPredecessor());
		System.out.println("SUCCESSOR:\t\t" + getSuccessor());
		System.out.println("--------------------------------");
	}

	// HELPERS
	/** Returns number of times target occurs in a list */
	private int countOccurrences(List<String> list, String target) {
		int count = 0;

		for (String item : list) {
			if (item.equals(target)) {
				count++;
			}
		}

		return count;
	}

    private boolean aPrecededByB(List<String> trace, String a, String b) {
        for (int i = 0; i < trace.size() - 1; i++) {
            if (trace.get(i+1).equals(a) && !trace.get(i).equals(b)) {
                return false;
            }
        }

    	return true;
	}

    /* returns if B is directly followed by A at least once in the trace, e.g. ...AB...A...C...B... */
    private boolean atLeastOnceAB(List<String> trace, String a, String b) {
        for (int i = 0; i < trace.size() - 1; i++) {
        	if (trace.get(i).equals(a) && trace.get(i+1).equals(b)) {
        		return true;
        	}
        }

    	return false;
	}

    private Set<Set<String>> splitTraceBeforeActivity(List<String> trace, String activity) {
    	Set<Set<String>> splits = new HashSet<>();
    	Set<String> split = new HashSet<>();

    	for (String act : trace) {
    		if (act.equals(activity)) {
    			splits.add(split);
                split = new HashSet<>();
    		} else {
    			split.add(act);
    		}
    	}

        return splits;
    }

    private boolean inEverySet(Set<Set<String>> sets, String elem) {
    	for (Set<String> s : sets) {
    		if (!s.contains(elem)) {
    			return false;
    		}
    	}

        return true;
    }

    public static Map<String, Set<String>> mapDeepCopy(Map<String, Set<String>> original) {
        Map<String, Set<String>> copy = new HashMap<String, Set<String>>();

        for (Entry<String, Set<String>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new HashSet<String>(entry.getValue()));
        }

        return copy;
    }

    public void removeEmptySetsFromMap(Map<String, Set<String>> map) {
        Set<String> keysWithEmptySets = new HashSet<>();

        for (String key : map.keySet()) {
            if (map.get(key).isEmpty()) {
                keysWithEmptySets.add(key);
            }
        }

        for (String key : keysWithEmptySets) {
            map.remove(key);
        }
    }

}
