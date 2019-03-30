package datamodelxml;

import javax.xml.bind.annotation.XmlAttribute;

public class RelationXml {
    @XmlAttribute(name = "description")
    private String description = "";
    
    @XmlAttribute(name = "filterLevel")
    private int filterLevel = 0;
    
    @XmlAttribute(name = "groups")
    private String groups = "";
    
    @XmlAttribute(name = "time")
    private String time = "";   
    
    @XmlAttribute(name = "sourceId")
    private String sourceId;
    
    @XmlAttribute(name = "targetId")
    private String targetId;

    public RelationXml() {
    }
    
    public RelationXml(String sourceId, String targetId) { 
    	this.sourceId = sourceId;
    	this.targetId = targetId;
    }
    
	public void setDescription(String description) {
		this.description = description;
	}

	public void setFilterLevel(int filterLevel) {
		this.filterLevel = filterLevel;
	}

	public void setGroups(String groups) {
		this.groups = groups;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}
}
