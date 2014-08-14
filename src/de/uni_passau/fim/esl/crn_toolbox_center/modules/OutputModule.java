package de.uni_passau.fim.esl.crn_toolbox_center.modules;

import de.uni_passau.fim.esl.crn_toolbox_center.DataPacket;

abstract public class OutputModule extends Module {
	protected int nChannels;	//Number of data channels that should be read.
	
	abstract public float[] processDataToFloat(float[] data);
	abstract public DataPacket processDataToDataPacket(float[] data);
	//abstract public Long[] processDataToLong(float[] data);
	//abstract public Double[] processDataToDouble(float[] data);
		
	public int getNChannels() {
		return nChannels;
	}

}
