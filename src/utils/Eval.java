package utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import datamodel.Graph;
import datamodel.PrefixTree;
import datamodel.PrefixTreeNode;
import datamodel.Trace;

public class Eval {
	private Graph graph;
	private PrefixTree prefixTree;
	private double lowerPrecisionBound = 0.0;
	private double precision = 0.0;
	private double normPrecision = 0.0;
	private double simplicityRelations = 0.0;
	private double simplicityRelationPairs = 0.0;
	
	public Eval(Graph graph, Map<Trace, Integer> traceMap, String logFilename) throws Exception {
		this.graph = graph;
		this.prefixTree = new PrefixTree();
		prefixTree.buildTree(graph, traceMap);
	}
	
	public Eval(Graph graph, Map<Trace, Integer> traceMap, String logFilename, double lowerPrecisionBound) throws Exception {
		this(graph, traceMap, logFilename);
		this.lowerPrecisionBound = lowerPrecisionBound;
	}
	
	/**
	 * Evaluates the replay fitness of a DCR graph for a given log.
	 * 
	 * In the context of declarative models, replay fitness is the ability of the model to replay the traces of the input log exactly.
	 *    fitness = #ReplayableTraces / #TotalTraces
	 * Soren Debois, Thomas T. Hildebrandt, Paw Hovsgaard Laursen, and Kenneth Ry Ulrik. 2017. Declarative process mining for DCR graphs.
	 * In Proceedings of the Symposium on Applied Computing (SAC '17). ACM, New York, NY, USA, 759-764.
	 * 
	 * Note. We only allow graphs with 100% fitness. If this fitness is not ensured, then the creation of PrefixTree fails and an error is returned from the constructor
	 */
	public double getFitness() {
		return 1.0;
	}

	/** Evaluates the precision of a DCR graph for a given log. */
	public double getPrecision() {
		if (precision == 0.0) {
			List<PrefixTreeNode> flattenedTree = prefixTree.getFlattenedTree();
			int countExecutedEvents = 0;
			double sum = 0;
			double ratio;
			
			for (PrefixTreeNode node : flattenedTree) {
				if (node.hasChildren()) {
					ratio = (double) node.getNumExecutedChildren() / node.getNumEnabledChildren();
					sum += ratio * node.getNumOfAllChildrenExecutions();
				}
				
				countExecutedEvents += node.getCountExecuted();
			}
			
			precision = (1.0 / countExecutedEvents) * sum;
		}

		return precision;
	}
	
	/** Evaluates the normalized precision of a DCR graph for a given log. */
	public double getNormalizedPrecision() {
		if (normPrecision == 0.0) {
			normPrecision = (getPrecision() - lowerPrecisionBound) / (1.0 - lowerPrecisionBound);
		}
		
		return normPrecision;
	}
	
	/**
	 * Evaluates the simplicity of a DCR graph for a given log.
	 * Simplicity = (1 - #relations / #possibleRelations) / 2 + (1 - #relationPairs / #possibleRelationPairs) / 2
	 */
	public double getSimplicity(double alpha) {
		if (simplicityRelations == 0.0) {
			int relations = graph.getConditionCount() + graph.getResponseCount() + graph.getIncludeCount() + graph.getExcludeCount();
			int activities = graph.getEvents().size();
			int possibleRelations = 4 * (int) Math.pow(activities, 2) - 3 * activities;
			int possibleRelationPairs = activities + (activities * (activities - 1)) / 2;
			Map<String, Set<String>> allRelatedPairs = new HashMap<>();
			int relationPairs = 0;
			
			for (String act : graph.getEvents().keySet()) {
				allRelatedPairs.put(act, new HashSet<>());
				
				if (graph.getConditionRelations().containsKey(act)) {
					allRelatedPairs.get(act).addAll(graph.getConditionRelations().get(act));
				}
				
				if (graph.getResponseRelations().containsKey(act)) {
					allRelatedPairs.get(act).addAll(graph.getResponseRelations().get(act));
				}
				
				if (graph.getIncludeRelations().containsKey(act)) {
					allRelatedPairs.get(act).addAll(graph.getIncludeRelations().get(act));
				}
				
				if (graph.getExcludeRelations().containsKey(act)) {
					allRelatedPairs.get(act).addAll(graph.getExcludeRelations().get(act));
				}
			}
			
			// remove duplicates, i.e., (a, b) == (b, a)
			for (String from : graph.getEvents().keySet()) {
				for (String to : allRelatedPairs.get(from)) {
					if (!from.equals(to)) {
						allRelatedPairs.get(to).remove(from);
					}
				}
			}

			relationPairs = Util.getMapSize(allRelatedPairs);
		    simplicityRelations = 1.0 - ((double) relations / possibleRelations);
		    simplicityRelationPairs = 1.0 - ((double) relationPairs / possibleRelationPairs);
		}

		return simplicityRelations * alpha + simplicityRelationPairs * (1.0 - alpha);
	}
}
