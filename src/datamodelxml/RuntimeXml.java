package datamodelxml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class RuntimeXml {
	@XmlElement(name = "marking")
    private MarkingXml marking = new MarkingXml();
	
	public RuntimeXml() {
	}
	
	public RuntimeXml(List<EventRuntimeXml> executed, List<EventRuntimeXml> included, List<EventRuntimeXml> pending) {
		this.marking = new MarkingXml(executed, included, pending);
	}
}
