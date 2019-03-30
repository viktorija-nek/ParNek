package datamodelxml;

import javax.xml.bind.annotation.XmlAttribute;

public class LocationXml {
	@XmlAttribute(name = "xLoc")
    private int xLoc = 425;
	
	@XmlAttribute(name = "yLoc")
    private int yLoc = 50;

	public void setxLoc(int xLoc) {
		this.xLoc = xLoc;
	}

	public void setyLoc(int yLoc) {
		this.yLoc = yLoc;
	}
}
