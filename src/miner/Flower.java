package miner;

import java.util.HashSet;
import java.util.Set;

import datamodel.Graph;
import datamodel.Log;
import datamodel.Trace;

public class Flower implements Miner {
    private Graph graph;
    private Set<Trace> traces;

    public Flower(Log log) throws Exception {
        this.traces = log.getTraces().keySet();
    }

    @Override
    public Graph mine() {
        Set<String> activities = new HashSet<>();
        graph = new Graph();

        for (Trace trace : traces) {
            activities.addAll(new HashSet<>(trace.getActivities()));
        }

        for (String uid : activities) {
            graph.addEvent(uid, uid, true, false, false);
        }

        return graph;
    }

}
