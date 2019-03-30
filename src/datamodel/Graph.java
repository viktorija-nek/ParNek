package datamodel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import datamodelxml.RelationXml;
import datamodelxml.EventRuntimeXml;
import datamodelxml.EventXml;
import datamodelxml.GraphXml;
import datamodelxml.LabelMappingXml;
import datamodelxml.LabelXml;
import utils.Const.RelationType;
import utils.Util;

/**
 * This is the data structure for a DCR Graph
 */
public class Graph  {
	private static int nextUid = 0;
	private Map<String, Event> events = new HashMap<>();
	private Map<String, Set<String>> conditionRelations = new HashMap<>();
	private Map<String, Set<String>> responseRelations = new HashMap<>();
	private Map<String, Set<String>> includeRelations = new HashMap<>();
	private Map<String, Set<String>> excludeRelations = new HashMap<>();
	private Set<String> enabledEvents = new HashSet<>();
	private Set<String> includedEvents = new HashSet<>();
	private Set<String> pendingEvents = new HashSet<>();
	private Set<String> executedEvents = new HashSet<>();
	
	public Graph() {
		updateEnabledEvents();
	}
	
	public Map<String, Event> getEvents() {
		return events;
	}

	public Map<String, Set<String>> getConditionRelations() {
		return conditionRelations;
	}

	public Map<String, Set<String>> getResponseRelations() {
		return responseRelations;
	}

	public Map<String, Set<String>> getIncludeRelations() {
		return includeRelations;
	}

	public Map<String, Set<String>> getExcludeRelations() {
		return excludeRelations;
	}

	public Set<String> getEnabledEvents() {
		return enabledEvents;
	}

	public Set<String> getIncludedEvents() {
		return includedEvents;
	}

	public Set<String> getPendingEvents() {
		return pendingEvents;
	}

	public Set<String> getExecutedEvents() {
		return executedEvents;
	}

	/** Adds a single event to the DCR graph. */
    public String addEvent() {
    	return addEvent(null, "", true, false, false);
    }
    
	/** Adds a single event to the DCR graph. */
    public String addEvent(String uid, String label, boolean included, boolean pending, boolean executed) {
    	if (uid == null || uid.isEmpty()) {
    		uid = String.valueOf(nextUid);
    		nextUid++;
    	}
    	
    	events.put(uid, new Event(uid, label, included, pending, executed));
    	updateEnabledEvents();
    	
    	return uid;
    }
    
    /** Adds a single relation between two events in the DCR graph. 
     * @throws Exception */
    public void addRelation(String from, String to, RelationType relType) throws Exception {
    	if (relType == RelationType.EXCLUDE) {
    		if (!excludeRelations.containsKey(from)) {
    			excludeRelations.put(from, new HashSet<>());
    		}

    		excludeRelations.get(from).add(to);
    	} else if (!from.equals(to)) {  // Disallow self-condition, self-response, self-includes and self-milestone relations
    		switch (relType) {
    			case CONDITION:
    				if (!conditionRelations.containsKey(from)) {
    					conditionRelations.put(from, new HashSet<>());
    	    		}

    				conditionRelations.get(from).add(to);
    				break;
    			case RESPONSE:
					if (!responseRelations.containsKey(from)) {
						responseRelations.put(from, new HashSet<>());
		    		}

					responseRelations.get(from).add(to);
					break;
				case INCLUDE:
					if (!includeRelations.containsKey(from)) {
						includeRelations.put(from, new HashSet<>());
		    		}

					includeRelations.get(from).add(to);
					break;
				default:
					throw new Error("WARNING: No support for " + relType + " relations.");
	    		}
    	} else {
    		// System.out.println("WARNING: Ignored invalid relation:(" + from + ", " + to + ", " + relType + ")");
    	}
    	
    	updateEnabledEvents();
    }

    
    /** Removes a single relation between two events in the DCR graph. 
     * @throws Exception */
    public void removeRelation(String from, String to, RelationType relType) throws Exception {
    	switch (relType) {
			case CONDITION:
				if (conditionRelations.containsKey(from)) {
					conditionRelations.get(from).remove(to);
	    		}
				break;
			case RESPONSE:
				if (responseRelations.containsKey(from)) {
					responseRelations.get(from).remove(to);
	    		}
				break;
			case INCLUDE:
				if (includeRelations.containsKey(from)) {
					includeRelations.get(from).remove(to);
	    		}
				break;
			case EXCLUDE:
				if (excludeRelations.containsKey(from)) {
					excludeRelations.get(from).remove(to);
	    		}
				break;
			default:
				throw new Error("WARNING: No support for " + relType + " relations.");
    		}
    	
    	updateEnabledEvents();
    }

    public Set<String> getConditionsToUid(String uid) {
    	Set<String> conditionsToUid = new HashSet<>();
    	
    	for (Entry<String, Set<String>> entry : getConditionRelations().entrySet()) {
    		if (entry.getValue().contains(uid)) {
    			conditionsToUid.add(entry.getKey());
    		}
    	}
    	
    	return conditionsToUid;
    }
    
    public Set<String> getConditionsFromUid(String uid) {
    	return getConditionRelations().get(uid) == null? new HashSet<>() : getConditionRelations().get(uid);
    }
    
    public Set<String> getResponsesFromUid(String uid) {
    	return getResponseRelations().get(uid) == null? new HashSet<>() : getResponseRelations().get(uid);
    }
    
    public Set<String> getExclusionsFromUid(String uid) {
    	return getExcludeRelations().get(uid) == null? new HashSet<>() : getExcludeRelations().get(uid);
    }
    
    public Set<String> getInclusionsFromUid(String uid) {
    	return getIncludeRelations().get(uid) == null? new HashSet<>() : getIncludeRelations().get(uid);
    }
    
    public int getResponseCount() {
    	return Util.getMapSize(getResponseRelations());
    }
    
    public int getConditionCount() {
    	return Util.getMapSize(getConditionRelations());
    }
    
    public int getIncludeCount() {
    	return Util.getMapSize(getIncludeRelations());
    }
    
    public int getExcludeCount() {
    	return Util.getMapSize(getExcludeRelations());
    }
    
    public int getRelationCount() {
    	return getResponseCount() + getConditionCount() + getIncludeCount() + getExcludeCount();
    }

    public void executeActivity(String label) throws Exception {
    	String uid = labelToUid(label);
    	//System.out.println(uid);
    	// Now we need to update the marking, i.e., executed, pending and included events.
    	// This means we need to update the sets the graph maintains and the flags each event maintains.
    	Event e = events.get(uid);
    	e.setExecuted(true);
    	e.setPending(false);
    	
    	// send responses
    	for (String from : responseRelations.keySet()) {
			for (String to : responseRelations.get(from)) {
				if (from.equals(uid)) {
	    			events.get(to).setPending(true);
	    		}
			}
		}

    	// perform any dynamic includes
    	for (String from : includeRelations.keySet()) {
			for (String to : includeRelations.get(from)) {
				if (from.equals(uid)) {
	    			events.get(to).setIncluded(true);
	    		}
			}
		}
    	
    	// perform any dynamic excludes
    	for (String from : excludeRelations.keySet()) {
			for (String to : excludeRelations.get(from)) {
				if (from.equals(uid)) {
	    			events.get(to).setIncluded(false);
	    		}
			}
		}
    	
    	// finally, update the state of the graph
        updateEnabledEvents();
    }
    
    public void executeTrace(List<String> trace) throws Exception {
    	for (String label : trace) {
    		executeActivity(label);
    	}
    }
    
    /** Finds the enabled DCR graph events by their activity label and returns their uids. If the deterministic flag is set,
        then an exception will be raised if more than one event is found with the same activity label. 
     * @throws Exception */
    public String labelToUid(String label) throws Exception {
    	Stack<String> uids = new Stack<>();
    	
    	for (Event e : events.values()) {
    		if (label.equals(e.getLabel()) && (enabledEvents.contains(e.getUid()))) {
    			uids.push(e.getUid());
    		}
    	}
    	
    	if (uids.size() == 1) {
    		return uids.pop();
    	} else {
    		throw new Exception(uids.size() < 1 ? "No enabled event with label {" + label + "}. Enabled events: " + enabledEvents + ".": "Model is non-deterministic.");
    	}
    }
    
    /** Returns the label of an event given its UID */
    public String uidToLabel(String uid) throws Exception {
    	return events.get(uid).getLabel();
    }
    
    public boolean isAccepting() {
    	return pendingEvents.isEmpty();
    }

	/** Analyzes a DCR Graph's 'marking' to to find all included, pending and executed events. */
	private void updateMarking() {
		includedEvents.clear();
		pendingEvents.clear();
		executedEvents.clear();
		
		for (Event e : events.values()) {
            if (e.isIncluded()) {
            	includedEvents.add(e.getUid());
            }
            
            if (e.isPending()) {
            	pendingEvents.add(e.getUid());
            }
            
            if (e.isExecuted()) {
            	executedEvents.add(e.getUid());
            }
		}
	}

	/** Analyzes a DCR Graph's 'marking' to determine which elements of the graph are currently enabled. */
	public void updateEnabledEvents() {
		updateMarking();
		Set<String> candidates = new HashSet<>(includedEvents);
		
		for (String from : conditionRelations.keySet()) {
			for (String to : conditionRelations.get(from)) {
				if (includedEvents.contains(from) && !executedEvents.contains(from)) {
					candidates.remove(to);
				}
			}
		}

		enabledEvents = new HashSet<>(candidates);
	}
	
	public void toXml(String filename) throws JAXBException {
		List<EventXml> eventsXml = new ArrayList<>();
		List<LabelXml> labelsXml = new ArrayList<>();
		List<LabelMappingXml> labelMappingsXml = new ArrayList<>();
		List<EventRuntimeXml> executedXml = new ArrayList<>();
		List<EventRuntimeXml> includedXml = new ArrayList<>();
		List<EventRuntimeXml> pendingXml = new ArrayList<>();
		List<RelationXml> conditionsXml = new ArrayList<>();
		List<RelationXml> responsesXml = new ArrayList<>();
		List<RelationXml> inclusionsXml = new ArrayList<>();
		List<RelationXml> exclusionseXml = new ArrayList<>();
		GraphXml grapghXml;
		JAXBContext jaxbContext = JAXBContext.newInstance(GraphXml.class);
	    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		for (String uid : events.keySet()) {
			eventsXml.add(new EventXml(uid));
			labelsXml.add(new LabelXml(uid));
			labelMappingsXml.add(new LabelMappingXml(uid));
		}
		
		for (String uid : executedEvents) {
			executedXml.add(new EventRuntimeXml(uid));
		}
		
		for (String uid : includedEvents) {
			includedXml.add(new EventRuntimeXml(uid));
		}
		
		for (String uid : pendingEvents) {
			pendingXml.add(new EventRuntimeXml(uid));
		}
		
		for (String from : conditionRelations.keySet()) {
			for (String to : conditionRelations.get(from)) {
				conditionsXml.add(new RelationXml(from, to));
			}
		}
		
		for (String from : responseRelations.keySet()) {
			for (String to : responseRelations.get(from)) {
				responsesXml.add(new RelationXml(from, to));
			}
		}
		
		for (String from : includeRelations.keySet()) {
			for (String to : includeRelations.get(from)) {
				inclusionsXml.add(new RelationXml(from, to));
			}
		}
		
		for (String from : excludeRelations.keySet()) {
			for (String to : excludeRelations.get(from)) {
				exclusionseXml.add(new RelationXml(from, to));
			}
		}
		
		grapghXml = new GraphXml(eventsXml, labelsXml, labelMappingsXml, conditionsXml, responsesXml, inclusionsXml, exclusionseXml, executedXml, includedXml, pendingXml);
	     
	    // Marshalling
	    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	    jaxbMarshaller.marshal(grapghXml, new File(filename));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("__DCR GRAPH__");
		sb.append("\nEvents:\t\t" + events.values());
		sb.append("\nIncludes:\t" + includeRelations);
		sb.append("\nExcludes:\t" + excludeRelations);
		sb.append("\nConditions:\t" + conditionRelations);
		sb.append("\nResponses:\t" + responseRelations);
		sb.append("\nEnabled:\t" + enabledEvents);
		sb.append("\nExecuted:\t" + executedEvents);
		sb.append("\nPending:\t" + pendingEvents);
		sb.append("\nIncluded:\t" + includedEvents);
		
		return sb.toString();
	}
	
	@Override
	public Graph clone() {
		Graph copy = new Graph();

		copy.events = new HashMap<>();
		for (Entry<String, Event> entry : this.events.entrySet()) {
			copy.events.put(entry.getKey(), new Event(entry.getValue()));
		}
		
		copy.conditionRelations = mapDeepCopy(this.conditionRelations);
		copy.responseRelations = mapDeepCopy(this.responseRelations);
		copy.includeRelations = mapDeepCopy(this.includeRelations);
		copy.excludeRelations = mapDeepCopy(this.excludeRelations);
		copy.enabledEvents = new HashSet<>(this.enabledEvents);
		copy.includedEvents = new HashSet<>(this.includedEvents);
		copy.pendingEvents = new HashSet<>(this.pendingEvents);
		copy.executedEvents = new HashSet<>(this.executedEvents);

		return copy;
	}
	
    private static Map<String, Set<String>> mapDeepCopy(Map<String, Set<String>> original) {
    	Map<String, Set<String>> copy = new HashMap<String, Set<String>>();
    	
	    for (Entry<String, Set<String>> entry : original.entrySet()) {
	    	copy.put(entry.getKey(), new HashSet<String>(entry.getValue()));
	    }
    	   
	    return copy;
    }
}
