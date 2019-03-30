package datamodelxml;

import javax.xml.bind.annotation.XmlAttribute;

public class LabelMappingXml {
    @XmlAttribute(name = "eventId")
    private String eventId;
    
    @XmlAttribute(name = "labelId")
    private String labelId;
    
    public LabelMappingXml() {
    }
    
    public LabelMappingXml(String eventId) { 
    	this.eventId = eventId;
    	this.labelId = eventId;
    }
    
    public LabelMappingXml(String eventId, String labelId) { 
    	this.eventId = eventId;
    	this.labelId = labelId;
    }

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public void setLabelId(String labelId) {
		this.labelId = labelId;
	}
}
