package de.uni_passau.fim.esl.crn_toolbox_center.modules;

public class ETHOS extends ANTModule {
	transient private short oldCount = -1;
	transient private short gyroX;
	transient private short gyroY;
    transient private short gyroZ;
    transient private short accX;
	transient private short accY;
	transient private short accZ;
	transient private short magnetX;
	transient private short magnetY;
	transient private short magnetZ;
	transient private short oldMagX = 0;
	transient private short oldMagY = 0;
	transient private short oldMagZ = 0;
	transient private byte axisCounter = 0;
	
	
	public ETHOS() {
		channelPeriod = 256; 
		deviceType = 0x74;
		networkNr = 0x00; //Public Network
		rfFrequency = 2474; 
		//rfFrequency = 2485; //New frequency
	}

	/**
	 * Return order:
	 * 0: x gyroscope
	 * 1: y gyroscope
	 * 2: z gyroscope
	 * 3: x accelerometer
	 * 4: y accelerometer
	 * 5: z accelerometer
	 * 6: x magnetometer
	 * 7: y magnetometer
	 * 8: z magnetometer
	 */
	@Override
	public float[] decodeSensorData(byte[] value) {
		 
		short msgBuf[] = new short[11];
    	short count;
    	byte length;
    	
    	float[] data = null;
    	//byte rxChannelNumber = (byte)(value[AntMesg.MESG_DATA_OFFSET] & AntDefine.CHANNEL_NUMBER_MASK);
    	
    	
    		length = value[0];
    		length -= 2;
    		
    		
	    		//Copy message to short values
	    		//msgBuf[0] = (short)(value[0]);
	    		//msgBuf[1] = (short)(value[1]);
	    		//msgBuf[2] = (short)(value[2]);
	    	msgBuf[3] = (short)(value[3]);
	    	msgBuf[3] = (short)(msgBuf[3] & 0x00FF);
	    	count = (short)msgBuf[3];
	    	if (oldCount != count) {	
	    		msgBuf[4] = (short)(value[4]);
	    		msgBuf[5] = (short)(value[5]);
	    		msgBuf[6] = (short)(value[6]);
	    		msgBuf[7] = (short)(value[7]);
	    		msgBuf[8] = (short)(value[8]);
	    		msgBuf[9] = (short)(value[9]);
	    		msgBuf[10] = (short)(value[10]);
    		
	    		//Remove sign bits
	    		
	    		msgBuf[4] = (short)(msgBuf[4] & 0x00FF);
	    		msgBuf[5] = (short)(msgBuf[5] & 0x00FF);
	    		msgBuf[6] = (short)(msgBuf[6] & 0x00FF);
	    		msgBuf[7] = (short)(msgBuf[7] & 0x00FF);
	    		msgBuf[8] = (short)(msgBuf[8] & 0x00FF);
	    		msgBuf[9] = (short)(msgBuf[9] & 0x00FF);
	    		msgBuf[10] = (short)(msgBuf[10] & 0x00FF);
	    		
	    		axisCounter = (byte)(msgBuf[10] & 0x003);
	    		//CRNT Toolbox code:
	    		
	    		
	    		if ((count % 2) != 0 ) {

		    		gyroX = (short)((msgBuf[4] << 8) | (msgBuf[5]));
		    		gyroY = (short)((msgBuf[6] << 8) | (msgBuf[7]));
		    		gyroZ = (short)((msgBuf[8] << 8) | (msgBuf[9]));
		    		
		    		//gyroX = convertToShort(gyroX);
		    		//gyroY = convertToShort(gyroY);
		    		//gyroZ = convertToShort(gyroZ);
		    		
		    		switch(axisCounter) {
		    			case 1:
		    				magnetX = (short)(magnetX | (msgBuf[10] >>> 2));
		    				//magnetX = (short)(magnetX << 6);
		    				break;
		    			case 2:
		    				magnetY = (short)(magnetY | (msgBuf[10] >>> 2));
		    				//magnetY = (short)(magnetY << 6);
		    				break;
		    			case 3: 
		    				magnetZ = (short)(magnetZ | (msgBuf[10] >>> 2));
		    				//magnetZ = (short)(magnetZ << 6);
		    				break;
		    			default: 
		    				break;
		    		}

	    		} else {
	    			accX = (short)((msgBuf[4] << 8) | (msgBuf[5]));
	    			accY = (short)((msgBuf[6] << 8) | (msgBuf[7]));
	    			accZ = (short)((msgBuf[8] << 8) | (msgBuf[9]));
	    			
	    			//accX = convertToShort(accX);
	    			//accY = convertToShort(accY);
	    			//accZ = convertToShort(accZ);
	    			
	    			switch(axisCounter) {
	    			case 1:
	    				magnetX = (short)(msgBuf[10] >>> 2);
	    				magnetX = (short)(magnetX << 6);
	    				break;
	    			case 2:
	    				magnetY = (short)(msgBuf[10] >>> 2);
	    				magnetY = (short)(magnetY << 6);
	    				break;
	    			case 3: 
	    				magnetZ = (short)(msgBuf[10] >>> 2);
	    				magnetZ = (short)(magnetZ << 6);
	    				break;
	    			default: 
	    				break;
	    		}
	    		}
	    		
	    		if ((axisCounter == 3) && ((count % 2) != 0)) {
	    			oldMagX = magnetX;
	    			oldMagY = magnetY;
	    			oldMagZ = magnetZ;
	    		}
	    		
	    		data = new float[9];
    			data[0] = (float)gyroX;
    			data[1] = (float)gyroY;
    			data[2] = (float)gyroZ;
    			data[3] = (float)accX;
    			data[4] = (float)accY;
    			data[5] = (float)accZ;
    			data[6] = (float)oldMagX;
    			data[7] = (float)oldMagY;
    			data[8] = (float)oldMagZ;
    			
    			oldCount = count;
    			return data;
	    		
    		
    		} else {
    			return null;
    		}
			
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getType() {
		return type;
	}
	
	private short convertToShort(short val) {
    	short ret=val;
    	short temp = (short)(val & 0x01FF);
    	if ((val & 0x0200) > 0) {
    		temp = (short)~temp;
    		temp &= 0x01FF;
    		temp += 1;
    		ret = (short)-temp;
    		
     	} else {
     		ret = val;
     	}
    	return ret;
    }

	@Override
	public void shutDown() {
		oldCount = -1;
		gyroX = 0;
		gyroY = 0;
	    gyroZ = 0;
	    accX = 0;
		accY= 0;
		accZ = 0;
		magnetX = 0;
		magnetY = 0;
		magnetZ = 0;
		oldMagX = 0;
		oldMagY = 0;
		oldMagZ = 0;
		axisCounter = 0;
		
	}

}
