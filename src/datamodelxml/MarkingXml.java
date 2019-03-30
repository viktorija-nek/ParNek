package datamodelxml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class MarkingXml {
	@XmlElement(name = "globalStore")
    private String globalStore = new String();
	
	@XmlElementWrapper(name = "executed")
	@XmlElement(name = "event")
    private List<EventRuntimeXml> executed = new ArrayList<>();
	
	@XmlElementWrapper(name = "included")
	@XmlElement(name = "event")
    private List<EventRuntimeXml> included = new ArrayList<>();
	
	@XmlElementWrapper(name = "pendingResponses")
	@XmlElement(name = "event")
    private List<EventRuntimeXml> pending = new ArrayList<>();

	public MarkingXml() {
	}
	
	public MarkingXml(List<EventRuntimeXml> executed, List<EventRuntimeXml> included, List<EventRuntimeXml> pending) {
		this.executed = executed == null? new ArrayList<>() : executed;
		this.included = included == null? new ArrayList<>() : included;
		this.pending = pending == null? new ArrayList<>() : pending;
	}
	
	public void setGlobalStore(String globalStore) {
		this.globalStore = globalStore;
	}

	public void setExecuted(List<EventRuntimeXml> executed) {
		this.executed = executed;
	}

	public void setIncluded(List<EventRuntimeXml> included) {
		this.included = included;
	}

	public void setPending(List<EventRuntimeXml> pending) {
		this.pending = pending;
	}
}
