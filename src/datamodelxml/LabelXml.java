package datamodelxml;

import javax.xml.bind.annotation.XmlAttribute;

public class LabelXml {
    @XmlAttribute(name = "id")
    private String id;
    
    public LabelXml() {
    }
    
    public LabelXml(String id) { 
    	this.id = id;
    }
    
	public void setId(String id) {
		this.id = id;
	}
}
