package datamodelxml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class ConstraintsXml {
	@XmlElementWrapper(name = "conditions")
	@XmlElement(name = "condition")
    private List<RelationXml> conditions;
	
	@XmlElementWrapper(name = "responses")
	@XmlElement(name = "response")
    private List<RelationXml> responses;
	
	@XmlElementWrapper(name = "includes")
	@XmlElement(name = "include")
    private List<RelationXml> includes;
	
	@XmlElementWrapper(name = "excludes")
	@XmlElement(name = "exclude")
    private List<RelationXml> excludes;

    public ConstraintsXml() {
    }
    
    public ConstraintsXml(List<RelationXml> conditions, List<RelationXml> responses, List<RelationXml> includes, List<RelationXml> excludes) {
    	this.conditions = conditions;
    	this.responses = responses;
    	this.includes = includes;
    	this.excludes = excludes;
    }
	
	public void setConditions(List<RelationXml> conditions) {
		this.conditions = conditions;
	}

	public void setResponses(List<RelationXml> responses) {
		this.responses = responses;
	}

	public void setIncludes(List<RelationXml> includes) {
		this.includes = includes;
	}

	public void setExcludes(List<RelationXml> excludes) {
		this.excludes = excludes;
	}
}
