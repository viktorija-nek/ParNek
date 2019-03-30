package miner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import datamodel.Graph;
import datamodel.Log;
import datamodel.Trace;
import utils.ComparatorSize;
import utils.Const;
import utils.Const.RelationType;
import utils.Stats;
import utils.Util;

/** Class for generating DCR graphs (model) from a given event log based on different mining algorithms */
public class ParNek implements Miner {
	private Log log;
	private Graph graph;
	private Stats statistics;
	private boolean inex = false;
	private boolean cond = false;
	private boolean improve = false;
	private int maxFollowCount = 0;
	private boolean optimized = false;
	private boolean checkAll = true;

	public ParNek(Log log, boolean inex, boolean cond) {
		this.log = log;
		this.statistics = new Stats(this.log.getTraces().keySet());
		this.inex = inex;
		this.cond = cond;
	}

	public ParNek(Log log, int maxFollowCount, boolean optimized, boolean checkAll, boolean cond) {
		this.log = log;
		this.statistics = new Stats(this.log.getTraces().keySet());
		this.improve = true;
		this.maxFollowCount = maxFollowCount;
		this.optimized = optimized;
		this.checkAll = checkAll;
		this.cond = cond;
	}

	/** creates a model with condition and response relations. The relations are optimized in a sense that "if B depends on A" and "C depends on A, B" then we get that B depends on A and C only depends on B (transitive relation)
	 * @throws Exception */
	@Override
	public Graph mine() throws Exception {
		graph = new Graph();
		Set<String> selfExclusions = statistics.getAtMostOnce();
		Map<String, String> chainPrecedence = statistics.getChainPrecedence();
		Map<String, Set<String>> conditions = new HashMap<>();
		String act;

		for (String uid : statistics.getActivities()) {
			graph.addEvent(uid, uid, true, false, false);
		}

		for (String uid : selfExclusions) {
			graph.addRelation(uid, uid, RelationType.EXCLUDE);
		}

		for (Entry<String, Set<String>> entry : optimizeRelations(statistics.getResponse()).entrySet()) {
			act = entry.getKey();

			for (String to : entry.getValue()) {
				graph.addRelation(act, to, RelationType.RESPONSE);
			}
		}

		if (inex) {
			Map<String, Set<String>> includedByMap = statistics.getIncludedBy(log.getTraces().keySet());
			Set<String> selfRepeated = statistics.getSelfRepeated(log.getTraces().keySet());
			Set<String> excludedBy;
			Set<String> includedBy;
			
			for (String to : includedByMap.keySet()) {
	        	excludedBy = new HashSet<>(statistics.getActivities());
	        	includedBy = new HashSet<>(includedByMap.get(to));
	        	includedBy.remove(to);
	        	excludedBy.removeAll(includedBy);
	        	
	        	if (selfRepeated.contains(to)) {
	        		excludedBy.remove(to);
	        	} else {
	        		excludedBy.add(to);
	        	}

	        	for (String from : includedBy) {
	        		graph.addRelation(from, to, RelationType.INCLUDE);
	        	}

	        	for (String from : excludedBy) {
	        		graph.addRelation(from, to, RelationType.EXCLUDE);
	        	}
	        }
		} else {
			for (String uid : chainPrecedence.keySet()) {
				graph.addRelation(chainPrecedence.get(uid), uid, Const.RelationType.INCLUDE);
				graph.addRelation(uid, uid, Const.RelationType.EXCLUDE);
				selfExclusions.add(uid);
			}

			Map<String, Set<String>> exclusionMap = createExclusionMap(selfExclusions);
			for (String uid : exclusionMap.keySet()) {
				for (String actTo : exclusionMap.get(uid)) {
					graph.addRelation(uid, actTo, Const.RelationType.EXCLUDE);
				}
			}
	
			if (improve) {
				tryToImproveGraph();
			}			
		}

		
		conditions = this.cond? combineConditions() : statistics.getPrecedence();

		for (Entry<String, Set<String>> entry : optimizeRelations(conditions).entrySet()) {
			act = entry.getKey();
	
			for (String from : entry.getValue()) {
				graph.addRelation(from, act, Const.RelationType.CONDITION);
			}
		}

//		System.out.println(graph);
//		graph.toXml("C:/Users/Viktorija/Desktop/graph.xml");

		return graph;
	}

	private void tryToImproveGraph() throws Exception {
		Map<String, Set<String>> alternatePrecedence = statistics.getAlternatePrecedence();
		Map<String, Set<String>> excludeKeyTo = new HashMap<>();
		Map<String, Set<String>> excludeKeyToOptimized = new HashMap<>();
		Set<String> allPreceded;

		for (String from : statistics.getPossibleOneExcludesAnother(checkAll).keySet()) {
			for (String to : statistics.getPossibleOneExcludesAnother(checkAll).get(from)) {
				allPreceded = new HashSet<>();

				for (Trace trace : log.getTraces().keySet()) {
					if (trace.getActivities().contains(from) && trace.getActivities().contains(to)) {
						allPreceded.addAll(beforeInBetween(trace.getActivities(), from, to));
					}
				}

				allPreceded.remove(from);
				allPreceded.remove(to);

				if (allPreceded.size() <= maxFollowCount) {
					if (optimized) {
						if (!excludeKeyTo.containsKey(to)) {
							excludeKeyTo.put(to, new HashSet<>());
						}

						excludeKeyTo.get(to).add(from);
					} else {
						graph.addRelation(from, to, Const.RelationType.EXCLUDE);
					}

					for (String includeFrom : allPreceded) {
						graph.addRelation(includeFrom, to, Const.RelationType.INCLUDE);
					}
				}
			}
		}

		if (optimized) {
			excludeKeyToOptimized = Util.mapDeepCopy(excludeKeyTo);

			for (String to : excludeKeyTo.keySet()) {
				for (String from : excludeKeyTo.get(to)) {
					for (String fromRedundant : excludeKeyTo.get(to)) {
						if (!from.equals(fromRedundant) && !to.equals(fromRedundant)) {
							if (alternatePrecedence.containsKey(fromRedundant) && alternatePrecedence.get(fromRedundant).contains(from)) {
								// System.out.println("from=" + fromRedundant + ", to=" + to);
								excludeKeyToOptimized.get(to).remove(fromRedundant);
							}
						}
					}
				}
			}

			for (String to : excludeKeyToOptimized.keySet()) {
				for (String from : excludeKeyToOptimized.get(to)) {
					graph.addRelation(from, to, Const.RelationType.EXCLUDE);
				}
			}
		}
	}

	// HELPERS
	/**
	 * optimizes the set of pairs
	 * example: {(A, B), (A, C), (B, C)} --> {(A, B), (B, C)} since C depends on B which already depends on A
	 * :param pairs: set of pairs, for example, {(A, B), (A, C), (B, C)}
	 * :return: optimized set of pairs, for example, {(A, B), (B, C)}
	 */
	private Map<String, Set<String>> optimizeRelations(Map<String, Set<String>> relationMap) {
		Map<String, Set<String>> optimizedRelations = new HashMap<>();
		List<Entry<String, Set<String>>> sortedDependencies =  new ArrayList<>(relationMap.entrySet());
		Collections.sort(sortedDependencies, new ComparatorSize());
		int listSize = sortedDependencies.size();
		Set<String> actSet;
		Set<String> nextActSet;
		int j;

		for (int i = listSize - 1; i >= 0; i--) {
			actSet = sortedDependencies.get(i).getValue();

			// if set of dependent activities is less than 2 that means the activities in the set cannot be changed
			if (actSet.size() < 2) {
				break;
			} else {
				j = i - 1;

				while (j >= 0) {
					nextActSet = new HashSet<>(sortedDependencies.get(j).getValue());
					nextActSet.add(sortedDependencies.get(j).getKey());

					if (nextActSet.size() < 2) {
						break;
					} else {
						if (actSet.containsAll(nextActSet)) {
							// discard all the elements except key
							for (String elem : sortedDependencies.get(j).getValue()) {
								actSet.remove(elem);
							}
						}
					}

					j--;
				}
			}
		}

		for (Entry<String, Set<String>> entry : sortedDependencies) {
			optimizedRelations.put(entry.getKey(), entry.getValue());
		}

		return optimizedRelations;
	}

	private Map<String, Set<String>> createExclusionMap(Set<String> selfExclusions) {
		Map<String, Set<String>> exclusionMap = new HashMap<>();
		Map<String, Set<String>> exclusionMapOptimized = new HashMap<>();
		Map<String, Set<String>> predecessorMap = statistics.getPredecessor();
		Map<String, Set<String>> successorMap = statistics.getSuccessor();
		Set<String> actExclusions;
		boolean excluded;

		for (String act : statistics.getActivities()) {
			if (predecessorMap.containsKey(act)) {
				exclusionMap.put(act, new HashSet<>());
				actExclusions = new HashSet<>(statistics.getActivities());

				if (successorMap.containsKey(act)) {
					actExclusions.removeAll(successorMap.get(act));
				}

				for (String actTo : actExclusions) {
					if (!(predecessorMap.containsKey(act) && predecessorMap.get(act).contains(actTo))) {
						exclusionMap.get(act).add(actTo); // remove all which can not coexist with act
					} else {
						if (!selfExclusions.contains(actTo)) {
							exclusionMap.get(act).add(actTo); // remove all which comes before and does not have self-exclusion
						}
					}
				}

				exclusionMap.get(act).remove(act); // remove self exclusions
			}
		}

		// optimization: go through all the predecessors and if they already exclude that activity there is no need to exclude it again
		for (String act : exclusionMap.keySet()) {
			for (String actTo : exclusionMap.get(act)) {
				excluded = false;

				if (predecessorMap.containsKey(act)) {
					for (String predecessor : predecessorMap.get(act)) {
						if (!predecessor.equals(act) && exclusionMap.containsKey(predecessor) && exclusionMap.get(predecessor).contains(actTo)) {
							excluded = true;
							break;
						}
					}

					if (!excluded) {
						if (!exclusionMapOptimized.containsKey(act)) {
							exclusionMapOptimized.put(act, new HashSet<>());
						}

						exclusionMapOptimized.get(act).add(actTo);
					}
				}
			}
		}

		return exclusionMapOptimized;
	}

	private Set<String> beforeInBetween(List<String> trace, String start, String end) {
		Set<String> before = new HashSet<>();
		boolean reset = true;
		int foundStartAt = 0;

		while (foundStartAt < trace.size() && !trace.get(foundStartAt).equals(start)) {
			foundStartAt++;
		}

		if (foundStartAt == trace.size()) {
			return before;
		} else {
			reset = false;

			for (int i = foundStartAt + 1; i < trace.size(); i++) {
				if (reset) {
					if (trace.get(i).equals(start)) {
						reset = false;
					}
				} else if (trace.get(i).equals(end)) {
					before.add(trace.get(i-1));
					
					if (!start.equals(end)) {
						reset = true;
					}
				}
			}

			return before;
		}
	}
	
	private Map<String, Set<String>> combineConditions() {
		Map<String, Set<String>> conditions = new HashMap<>();
		Map<String, Set<String>> conditionsPrecedence = statistics.getPrecedence();
		Map<String, Set<String>> conditionsAdditional = findConditions();

		for (String uid : statistics.getActivities()) {
			if (conditionsPrecedence.containsKey(uid)) {
				if (!conditions.containsKey(uid)) {
					conditions.put(uid, new HashSet<>());
				}
				
				conditions.get(uid).addAll(conditionsPrecedence.get(uid));
			}
			
			if (conditionsAdditional.containsKey(uid)) {
				if (!conditions.containsKey(uid)) {
					conditions.put(uid, new HashSet<>());
				}
				
				conditions.get(uid).addAll(conditionsAdditional.get(uid));
			}
		}
		
		return conditions;
	}
	
	private Map<String, Set<String>> findConditions() {
		Map<String, Set<String>> conditions = statistics.getPredecessorFirst();
		List<String> traceActs;
		Set<String> conditionsValid;
		Set<String> conditionsToCheck;
		Set<String> conditionKeys;
		int foundFirstAt, foundLastAt;
		boolean out, executed;
		
		for (String act : conditions.keySet()) {			
			conditions.get(act).removeAll(graph.getConditionsToUid(act));
			conditions.get(act).remove(act);
		}

		for (Trace trace : this.log.getTraces().keySet()) {
			traceActs = trace.getActivities();
			conditionKeys = new HashSet<>(conditions.keySet());

			for (String act : conditionKeys) {
				foundFirstAt = -1; // act NOT found in the trace
				foundLastAt = -1;
				
				for (int i = 0; i < traceActs.size(); i++) {
					if (traceActs.get(i).equals(act)) {
						if (foundFirstAt == -1) {
							foundFirstAt = i;
							foundLastAt = i;
						} else {
							foundLastAt = i;
						}
					}
				}
				
				if (foundFirstAt != -1) {
					conditionsValid = new HashSet<>(traceActs.subList(0, foundFirstAt));
					conditionsValid.retainAll(conditions.get(act));
					conditionsToCheck = new HashSet<>(conditions.get(act));
					conditionsToCheck.removeAll(conditionsValid);

					for (String condFrom : conditionsToCheck) {
						out = false; // all the activities are included in the beginning
						executed = false; // all the activities are not executed in the beginning
						
						// we are interested if we can do all the act if we have condFrom -> * act 
						for (int j = 0; j < foundLastAt; j++) {
							if (traceActs.get(j).equals(condFrom)) {
								executed = true;
								break;
							}
							
							if (traceActs.get(j).equals(act)) {
								if (!out) {
									break; // we would not be able to do act if condFrom was in the graph and it was not executed
								}
							}
							
							if (graph.getExclusionsFromUid(traceActs.get(j)).contains(condFrom)) {
								out = true; // after performing j activity in the trace, condFrom was excluded
							}
							
							if (graph.getInclusionsFromUid(traceActs.get(j)).contains(condFrom)) {
								out = false; // after performing j activity in the trace, condFrom was included
							}
						}
						
						// if we end up that condFrom is excluded, it means that we can have condFrom ->* act since condFrom will be excluded (it does not violate the relation)
						if (out || executed) {
							conditionsValid.add(condFrom);
						}
					}

					conditionsValid.remove(act); // no self-conditions allowed
					if (conditionsValid.isEmpty()) {
						conditions.remove(act);
					} else {
						conditions.put(act, conditionsValid);
					}
				}
			}
		}

		return conditions;
	}
}