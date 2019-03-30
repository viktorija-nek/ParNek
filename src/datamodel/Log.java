package datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import utils.XesLoader;

public class Log {
    /** This is an event log. It can be parsed, loaded and saved. */

    private String filename;
    private Map<Trace, Integer> traces = new HashMap<>();
    private Set<Trace> tracesWithoutSelfRepetition = new HashSet<>();
    private Set<String> selfRepeatedActivities = new HashSet<>();
    private Set<String> uniqueActivities = new HashSet<>();
    private List<Integer> lengths = new ArrayList<>();
    private int activityCount = 0;
    private int traceCount = 0;
    private String format = "xes";
    private static final String XES_LABEL_KEY = "concept:name";

    public Log(String filename) {
        this.filename = filename;
    }

    public Log(String filename, String format) {
        this.filename = filename;
        this.format = format;
    }

    public String getFilename() {
        return filename;
    }

    public Map<Trace, Integer> getTraces() {
        return traces;
    }

    public String getFormat() {
        return format;
    }

    public void parse() throws Exception {
        /**
         * Parses the given XES formated event log associated with the Log
         * object and stores unique traces with their frequency in the log.
         */
        List<XLog> xLogs = XesLoader.load(filename);
        XLog log = xLogs.get(0); // Despite the fact that XesLoader return a list of XLogs, there should only be one log.
        List<String> activities;
        uniqueActivities = new HashSet<>();
        activityCount = 0;
        traceCount = 0;

        // Create a map with trace as a key and frequency as a value
        // process trace by trace
        for (XTrace xTrace : log) {
            activities = new ArrayList<>();

            for (XEvent xAct : xTrace) {
                if (xAct.getAttributes().containsKey(XES_LABEL_KEY)) {
                    activities.add(xAct.getAttributes().get(XES_LABEL_KEY).toString());
                    uniqueActivities.add(xAct.getAttributes().get(XES_LABEL_KEY).toString());
                    activityCount++;
                }
            }

            Trace trace = new Trace(activities);
            lengths.add(activities.size());

            if (traces.containsKey(trace)) {
                traces.put(trace, traces.get(trace) + 1);
            } else {
                traces.put(trace, 1);
            }
            
            traceCount++;
        }
    }

    public Set<Trace> getTracesWithoutSelfRepetition() throws Exception {
        if (tracesWithoutSelfRepetition.size() < 1) {
            List<String> activitiesWithoutSelfRepetition;
            List<String> activities;

            if (traces.size() < 1) {
                parse();
            }

            for (Trace trace : traces.keySet()) {
                activitiesWithoutSelfRepetition = new ArrayList<>();
                activities = trace.getActivities();

                if (activities.size() > 0) {
                    activitiesWithoutSelfRepetition.add(activities.get(0));
                }

                for (int i = 1; i < activities.size(); i++) {
                    if (!activities.get(i-1).equals(activities.get(i))) {
                        activitiesWithoutSelfRepetition.add(activities.get(i));
                    } else {
                        selfRepeatedActivities.add(activities.get(i));
                    }
                }

                tracesWithoutSelfRepetition.add(new Trace(activitiesWithoutSelfRepetition));
            }
        }

        return tracesWithoutSelfRepetition;
    }

    public Set<String> getSelfRepeatedActivities() throws Exception {
        if (selfRepeatedActivities.size() < 1) {
            getTracesWithoutSelfRepetition();
        }

        return selfRepeatedActivities;
    }

    public void printMostCommonTraces() {
        int size = getTraces().size();
        List<Entry<Trace, Integer>> mostCommonTraces = new ArrayList<>();

        for (Entry<Trace, Integer> trace : getTraces().entrySet()) {
            if (size < 5 || trace.getValue() > 100) {
                mostCommonTraces.add(trace);
            }
        }
        //	Collections.sort(mostCommonTraces, Comparator.comparing(s -> s.getValue()));

        System.out.println("------------ TRACES ------------");
        System.out.println("--------------------------------");
        System.out.println("UNIQUE TRACES COUNT:\t" + size);
        System.out.println("MOST COMMON TRACES:");
        for (Entry<Trace, Integer> trace : mostCommonTraces) {
            System.out.println(trace.getValue() + ":\t " + trace.getKey());
        }
        System.out.println("--------------------------------");
    }
    
    public int getUniqueActivitiesCount() {
    	return uniqueActivities.size();
    }
    
    public int getActivityCount() {
    	return activityCount;
    }
    
    public int getUniqueTraceCount() {
    	return traces.size();
    }
    
    public int getTraceCount() {
    	return traceCount;
    }
    
    public List<Integer> getStats() {
    	List<Integer> stats = new ArrayList<>();
    	stats.add(getUniqueActivitiesCount());
    	stats.add(getActivityCount());
    	stats.add(getUniqueTraceCount());
    	stats.add(getTraceCount());
    	
    	return stats;
    }
    
    public List<Double> getMinMedMax() {
    	List<Double> stats = new ArrayList<>();
    	int size = lengths.size();
    	double median;
    	
    	if (size > 0) {
    		Collections.sort(lengths);

    		if (size % 2 == 0) {
    			median = ((double) lengths.get(size /2 - 1) + (double) lengths.get(size /2)) / 2;
    		} else {
    			median = (double) lengths.get(size /2);
    		}
    		   
    		stats.add((double) lengths.get(0));
    		stats.add(median);
    		stats.add((double) lengths.get(size - 1));
    		    
    	}

    	return stats;
    }

    @Override
    public String toString() {
        StringBuilder traceList = new StringBuilder("");

        for (Trace trace : traces.keySet()) {
            traceList.append(trace.toString());
            traceList.append("\n");
        }

        return "filename = " + this.filename + "\ntraces:\n" + traceList.toString();
    }
}
