package datamodelxml;

import javax.xml.bind.annotation.XmlElement;

public class CustomXml {
	@XmlElement(name = "visualization")
    private VisualizationXml visualization = new VisualizationXml();
	
	@XmlElement(name = "eventScope")
    private String eventScope = "private";
	
	@XmlElement(name = "level")
    private int level = 1;
	
	@XmlElement(name = "sequence")
    private int sequence = 1;
	
	@XmlElement(name = "costs")
    private int costs = 0;

	public void setVisualization(VisualizationXml visualization) {
		this.visualization = visualization;
	}

	public void setEventScope(String eventScope) {
		this.eventScope = eventScope;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public void setCosts(int costs) {
		this.costs = costs;
	}
}
