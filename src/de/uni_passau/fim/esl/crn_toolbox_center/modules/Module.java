package de.uni_passau.fim.esl.crn_toolbox_center.modules;

import java.util.Observable;


/**
 * Class to store the modules (crnt tasks) into.
 * @author Frank
 * 
 */
abstract public class Module extends Observable{
	
	protected String type;	//REQUIRED: Type of the module
	protected String id;	//REQUIRED: Unique id of the module
	protected transient boolean moduleReady;
	
	public Module() {
		
	}
		
	/** Returns a string of the Id of the module
	 * 
	 * @return
	 */
	public String getId() {
		return id;
	}
	public String getType() {
		return type;
	}
	
	public boolean isReady() {
		return moduleReady;
	}
	
	public void setReady(boolean ready) {
		moduleReady = ready;
	}
	

}
