package de.uni_passau.fim.esl.crn_toolbox_center.modules;

abstract public class ANTPlusModule extends ANTModule{
	

	
	public ANTPlusModule() {
		networkNr = 0x01;	//ANT+ network
		rfFrequency = 57; //2457Mhz (ANT+ frequency)
		
	}
	abstract public float[] decodeSensorData(byte[] value);
	
	public short getDeviceNr() {
		//Convert endianess.
		short val = (short)((deviceNr >>> 8) | (deviceNr << 8));
		return val;
	}
	
	public byte getChannelNr() {
		return (byte) ((byte)channelNr & 0xFF);
	}
	
	public byte getNetworkNr() {
		return (byte) ((byte)networkNr & 0xFF);
	}
	
	public byte getDeviceType() {
		return (byte) ((byte)deviceType & 0xFF);
	}
	
	public byte getTransmissionType() {
		return (byte) ((byte)transmissionType & 0xFF);
	}
	
	public short getChannelPeriod() {
		return (short)channelPeriod;
	}
	
	public byte getRfFrequency() {
		return (byte) ((byte)rfFrequency & 0xFF);
	}
	
	public byte getProximityTreshold() {
		return (byte) ((byte)proximityTreshold & 0xFF); 
	}


}
