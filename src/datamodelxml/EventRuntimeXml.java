package datamodelxml;

import javax.xml.bind.annotation.XmlAttribute;

public class EventRuntimeXml {
    @XmlAttribute(name = "id")
    private String id;
    
    public EventRuntimeXml() {
    }
    
    public EventRuntimeXml(String id) { 
    	this.id = id;
    }
    
	public void setId(String id) {
		this.id = id;
	}
}
