package datamodelxml;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;

public class SpecificationXml {
	@XmlElement(name = "resources")
    private ResourcesXml resources;
	
	@XmlElement(name = "constraints")
    private ConstraintsXml constraints;
	
	public SpecificationXml() {
    	this.resources = new ResourcesXml();
    	this.constraints = new ConstraintsXml();
    }
	
    public SpecificationXml(List<EventXml> events, List<LabelXml> labels, List<LabelMappingXml> labelMappings) {
    	this.resources = new ResourcesXml(events, labels, labelMappings);
    }
    
    public SpecificationXml(List<EventXml> events, List<LabelXml> labels, List<LabelMappingXml> labelMappings, List<RelationXml> conditions, List<RelationXml> responses, List<RelationXml> includes, List<RelationXml> excludes) {
    	this.resources = new ResourcesXml(events, labels, labelMappings);
    	this.constraints = new ConstraintsXml(conditions, responses, includes, excludes);
    }

	public void setResources(ResourcesXml resources) {
		this.resources = resources;
	}
	
	public void setConstraints(ConstraintsXml constraints) {
		this.constraints = constraints;
	}
}