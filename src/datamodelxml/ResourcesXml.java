package datamodelxml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class ResourcesXml {
	@XmlElementWrapper(name = "events")
	@XmlElement(name = "event")
    private List<EventXml> events;
	
	@XmlElementWrapper(name = "labels")
	@XmlElement(name = "label")
    private List<LabelXml> labels;
	
	@XmlElementWrapper(name = "labelMappings")
	@XmlElement(name = "labelMapping")
    private List<LabelMappingXml> labelMappings;

    public ResourcesXml() {
    	this.events = new ArrayList<>();
    }
    
    public ResourcesXml(List<EventXml> events, List<LabelXml> labels, List<LabelMappingXml> labelMappings) {
    	this.events = events;
    	this.labels = labels;
    	this.labelMappings = labelMappings;
    }

	public void setEvents(List<EventXml> events) {
		this.events = events;
	}

	public void setLabels(List<LabelXml> labels) {
		this.labels = labels;
	}

	public void setLabelMappings(List<LabelMappingXml> labelMappings) {
		this.labelMappings = labelMappings;
	}
}
