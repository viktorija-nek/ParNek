package datamodel;

import java.util.List;

public class Trace {
    /** This is a single trace from an event log. */
    
	private final String uid;
	private final List<String> activities;

	public Trace(List<String> activities) {
		this.uid = null;
		this.activities = activities;
	}
	
	public Trace(List<String> activities, String uid) {
		this.uid = uid;
		this.activities = activities;
	}
	
	public String getUid() {
		return uid;
	}

	public List<String> getActivities() {
		return activities;
	}
	
	@Override
	public String toString() {
		StringBuilder traceInLine = new StringBuilder("");
		
		for (int i = 0; i < activities.size(); i++) {
			traceInLine.append(activities.get(i));
			
			if (i < activities.size() - 1) {
				traceInLine.append(", ");
			}
		}

		return traceInLine.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
	    final Trace other = (Trace) obj;
	    
	    if (other == null) {
	        return false;
	    }
	    
	    if ((this.toString() == null) ? (other.toString() != null) : this.activities.size() == other.activities.size() && !this.toString().equals(other.toString())) {
	        return false;
	    }

	    return true;
	}
	
	@Override
	public int hashCode() {
	    return this.toString().hashCode();
	}
}
