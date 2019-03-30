package datamodelxml;

import javax.xml.bind.annotation.XmlElement;

public class VisualizationXml {
	@XmlElement(name = "location")
    private LocationXml location = new LocationXml();
	
	@XmlElement(name = "colors")
    private ColorsXml colors = new ColorsXml();
}
