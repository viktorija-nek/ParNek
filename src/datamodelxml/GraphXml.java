package datamodelxml;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "dcrgraph")
public class GraphXml {
    @XmlAttribute(name = "dataTypesStatus")
    private String dataTypesStatus = "hide";
    
    @XmlAttribute(name = "filterLevel")
    private int filterLevel = -1;
    
    @XmlAttribute(name = "formGroupStyle")
    private String formGroupStyle = "Normal";
    
    @XmlAttribute(name = "formLayoutStyle")
    private String formLayoutStyle = "Horizontal";
    
    @XmlAttribute(name = "graphBG")
    private String graphBG = "#EBEBEB";
    
    @XmlAttribute(name = "title")
    private String title = "DCR";    
    
    @XmlAttribute(name = "zoomLevel")
    private int zoomLevel = 0;    
 
    @XmlElement(name = "specification")
    private SpecificationXml specification;

    @XmlElement(name = "runtime")
    private RuntimeXml runtime;
    
    public GraphXml() {
    	this.specification = new SpecificationXml();
    }
    
    public GraphXml(List<EventXml> events, List<LabelXml> labels, List<LabelMappingXml> labelMappings) {
    	this.specification = new SpecificationXml(events, labels, labelMappings);
    }
    
    public GraphXml(List<EventXml> events, List<LabelXml> labels, List<LabelMappingXml> labelMappings,
    		List<RelationXml> conditions, List<RelationXml> responses, List<RelationXml> includes, List<RelationXml> excludes,
    		List<EventRuntimeXml> executed, List<EventRuntimeXml> included, List<EventRuntimeXml> pending) {
    	this.specification = new SpecificationXml(events, labels, labelMappings, conditions, responses, includes, excludes);
    	this.runtime = new RuntimeXml(executed, included, pending);
    }

	public void setDataTypesStatus(String dataTypesStatus) {
		this.dataTypesStatus = dataTypesStatus;
	}

	public void setFilterLevel(int filterLevel) {
		this.filterLevel = filterLevel;
	}

	public void setFormGroupStyle(String formGroupStyle) {
		this.formGroupStyle = formGroupStyle;
	}

	public void setFormLayoutStyle(String formLayoutStyle) {
		this.formLayoutStyle = formLayoutStyle;
	}

	public void setGraphBG(String graphBG) {
		this.graphBG = graphBG;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setZoomLevel(int zoomLevel) {
		this.zoomLevel = zoomLevel;
	}

	public void setResources(SpecificationXml specification) {
		this.specification = specification;
	}
}
