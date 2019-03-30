package datamodelxml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class EventXml {
	@XmlAttribute(name = "id")
    private String id;
	
	@XmlElement(name = "custom")
    private CustomXml custom = new CustomXml();

	public EventXml() {
		this.id = null;
	}
	
	public EventXml(String id) {
		this.id = id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setCustom(CustomXml custom) {
		this.custom = custom;
	}
}
