package datamodelxml;

import javax.xml.bind.annotation.XmlAttribute;

public class ColorsXml {
	@XmlAttribute(name = "bg")
    private String bg = "#f9f7ed";
	
	@XmlAttribute(name = "stroke")
    private String stroke = "#cccccc";
	
	@XmlAttribute(name = "textStroke")
    private String textStroke = "#000000";

	public void setBg(String bg) {
		this.bg = bg;
	}

	public void setStroke(String stroke) {
		this.stroke = stroke;
	}

	public void setTextStroke(String textStroke) {
		this.textStroke = textStroke;
	}
}
