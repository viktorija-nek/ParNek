package datamodel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** This is a node in a Prefix Tree Node data structure */
public class PrefixTreeNode {
	private String uid;
	private String label;
	private Map<String, PrefixTreeNode> children = new HashMap<>();	// Key: Activity Label; Value: PrefixTreeNode
	private int countEnabled = 0;	// Number of times this event node is enabled during replay
	private int countExecuted = 0;	// Number of times this event node is executed during replay
	private int level;	// Level of the node in the tree, used primarily for pretty printing

	public PrefixTreeNode(String uid, String label, int level) {
		this.uid = uid;
		this.label = label;
		this.level = level;
	}
	
	public PrefixTreeNode(String uid, String label) {
		this(uid, label, 1);
	}

	public String getLabel() {
		return label;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getUid() {
		return uid;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}

	public Map<String, PrefixTreeNode> getChildrenMap() {
		return children;
	}
	
	public Collection<PrefixTreeNode> getChildren() {
		return children.values();
	}

	public int getNumEnabledChildren() {
		return children.size();
	}
	
	public int getNumExecutedChildren() {
		int count = 0;
		
		for (PrefixTreeNode child : children.values()) {
			if (child.getCountExecuted() > 0) {
				count++;
			}
		}
		
		return count;
	}
	
	public int getNumOfAllChildrenExecutions() {
		int count = 0;
		
		for (PrefixTreeNode child : children.values()) {
			count += child.getCountExecuted();
		}
		
		return count;
	}

	public int getCountEnabled() {
		return countEnabled;
	}

	public int getCountExecuted() {
		return countExecuted;
	}
	
	public boolean hasChildren() {
		return children.size() > 0;
	}
		
	public boolean hasChild(String label) {
		return children.containsKey(label);
	}
	
	public void incEnabled(int n) {
		countEnabled += n;
	}
	
	public void incEnabled() {
		incEnabled(1);
	}

	public void incExecuted(int n) {
		countExecuted += n;
	}
	
	public void incExecuted() {
		incExecuted(1);
	}
	
	public void incChildEnabled(String label, int n) {
		children.get(label).incEnabled(n);
	}
	
	public void incChildEnabled(String label) {
		children.get(label).incEnabled();
	}

	public void incChildExecuted(String label, int n) {
		children.get(label).incExecuted(n);
	}
	
	public void incChildExecuted(String label) {
		children.get(label).incExecuted();
	}
	
	public void addChild(PrefixTreeNode childNode) throws Exception {
		if (!children.containsKey(childNode.getLabel())) {
			children.put(childNode.getLabel(), childNode);
		} else {
			throw new Exception("Model is non-deterministic.");
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("(" + label + ", enabled=" + countEnabled + ", executed=" + countExecuted + ", children=");
		
		if (children.isEmpty()) {
			sb.append("None");
		} else {
			String indent;
			
			for (PrefixTreeNode node : children.values()) {
				indent = new String(new char[node.getLevel()]).replace("\0", "\t");
				sb.append("\n" + indent + node.toString());
			}
		}
		
		sb.append(")");
		
		return sb.toString();
	}
}
