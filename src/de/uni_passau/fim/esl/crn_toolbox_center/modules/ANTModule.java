package de.uni_passau.fim.esl.crn_toolbox_center.modules;

abstract public class ANTModule extends Module {
	protected int deviceNr;				//REQUIRED: Unique 16 bit device number. 0 for wildcard search
	protected int channelNr;			//REQUIRED: Unique arbitrary channel number
	protected int proximityTreshold;	//REQUIRED: Number between 1 and 7 to specify the search area
	protected int transmissionType;		//REQUIRED: 8 bit field that defines the transmission. 0 for wildcard search
	
  	protected transient int channelPeriod;
  	protected transient int rfFrequency;
  	protected transient int networkNr;
	protected transient int deviceType;
  	
  	
	abstract public float[] decodeSensorData(byte[] value);
	
	public short getDeviceNr() {
		//Convert endianess.
		short val = (short)((deviceNr >>> 8) | (deviceNr << 8));
		return val;
	}
	
	public void setDeviceNr(short deviceNr) {
		short val = (short) ((deviceNr >>> 8) | (deviceNr << 8));
	
		this.deviceNr = val & 0xFFFF;
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
		
		short channelPeriodVal = (short) (32768/channelPeriod);
		//short channelPeriodVal = (short)(channelPeriod);
		return channelPeriodVal;
	}
	
	public byte getRfFrequency() {
		byte rfFrequencyVal = (byte)((rfFrequency - 2400)/1);
		
		return rfFrequencyVal;
	}
	
	public byte getProximityTreshold() {
		return (byte) ((byte)proximityTreshold & 0xFF); 
	}
	abstract public void shutDown();
}
