package datamodel;

/**
 * This is a DCR Graph event.
 */
public class Event {
	private String uid;
	private String label;
	private boolean included;
	private boolean pending;
	private boolean executed;
	
	public Event(String uid, String label, boolean included, boolean pending, boolean executed) {
		this.uid = uid;
		this.label = label;
		this.included = included;
		this.pending = pending;
		this.executed = executed;
	}
	
	public Event(Event e) {
		this.uid = e.getUid();
		this.label = e.getLabel();
		this.included = e.isIncluded();
		this.pending = e.isPending();
		this.executed = e.isExecuted();
	}

	public boolean isIncluded() {
		return included;
	}

	public void setIncluded(boolean included) {
		this.included = included;
	}

	public boolean isPending() {
		return pending;
	}

	public void setPending(boolean pending) {
		this.pending = pending;
	}

	public boolean isExecuted() {
		return executed;
	}

	public void setExecuted(boolean executed) {
		this.executed = executed;
	}

	public String getUid() {
		return uid;
	}

	public String getLabel() {
		return label;
	}
	
	@Override
	public String toString() {
		return "EVENT (uid=" + uid + ", label=" + label + ", included=" + included + ", pending=" + pending + ", executed=" + executed + ")";
	}
}
