package datamodel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Queue;

/**
 * This is an implementation of the Prefix Tree or Trie data structure, see more at https://en.wikipedia.org/wiki/Trie.
 * Consider using a radix tree instead if there are performance issues. But then we need to be careful about the count.
 */
public class PrefixTree {
	private PrefixTreeNode root = new PrefixTreeNode(null, null);
	private List<PrefixTreeNode> flattened;
	
	/** Adds the activities in the given trace to the tree.
	 * Args:
	 * - trace (List[str]): A single trace from the event log. The first activity should be the source token.
	 *   The last activity should be the sink token, unless the trace is not legal. 
	 * @throws Exception */
	public void addPath(Graph dcrGraph, List<String> trace, int count) throws Exception {
		PrefixTreeNode currentNode = root;
		String label;
		String labelTemp;
		Queue<String> traceTemp = new LinkedList<>();
		Set<String> enabledEvents;
		traceTemp.addAll(trace);

        // walk the tree adding nodes as needed and incrementing
        while (!traceTemp.isEmpty()) {
        	// take the next activity from the trace
        	label = traceTemp.remove();
        	
        	// check that all enabled events are children of current node
        	enabledEvents = dcrGraph.getEnabledEvents();
        	
        	for (String uid : enabledEvents) {
        		labelTemp = dcrGraph.uidToLabel(uid);
        		
        		if (!currentNode.hasChild(labelTemp)) {
        			currentNode.addChild(new PrefixTreeNode(uid, labelTemp, currentNode.getLevel() + 1));
        		}
        		
        		currentNode.incChildEnabled(labelTemp, count);
        	}
        	
        	// check that label from trace is possible
        	if (!currentNode.hasChild(label)) {
        		throw new Exception("PrefixTree has no such child node {" + label + "}. Trace: " + trace + ". DCR graph: " + dcrGraph.toString());
        	}
        	
        	// execute the model
        	dcrGraph.executeActivity(label);
        	
        	// update current node to the child with current label
            currentNode = currentNode.getChildrenMap().get(label);
            currentNode.incExecuted(count);
        }
	}
	
	/** Builds a prefix tree from the enabled events of a DCR graph. 
	 * @throws Exception */
	public void buildTree(Graph dcrGraph, Map<Trace, Integer> traceMap) throws Exception {
		Graph dcrGraphTemp;
		
		for (Entry<Trace, Integer> trace : traceMap.entrySet()) {
			dcrGraphTemp = dcrGraph.clone();
			addPath(dcrGraphTemp, trace.getKey().getActivities(), trace.getValue());
		}
	}
	
	public List<PrefixTreeNode> getFlattenedTree() {
		if (flattened == null) {
			this.flattened = flatten(root);
		}
		
		return this.flattened;
	}

	private List<PrefixTreeNode> flatten(PrefixTreeNode node) {
		List<PrefixTreeNode> flattenedTemp = new ArrayList<>();
		flattenedTemp.add(node);

		for (PrefixTreeNode child : node.getChildren()) {
			flattenedTemp.addAll(flatten(child));
		}
		
		return flattenedTemp;
	}
	
	@Override
	public String toString() {
		return root.toString();
	}
}
