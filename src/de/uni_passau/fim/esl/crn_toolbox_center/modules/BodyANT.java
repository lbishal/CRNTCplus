package de.uni_passau.fim.esl.crn_toolbox_center.modules;

import android.util.Log;


public class BodyANT extends ANTModule{

	transient private short oldCount = -1;
	transient private short newCount = 0;
	
	
	public BodyANT() {
		channelPeriod = 32; //32Hz
		deviceType = 0xBA;
		networkNr = 0x00; //Public Network
		rfFrequency = 2461; 
	}
	
	/**
	 * Return order: 
	 * 0: x accelerometer
	 * 1: y accelerometer
	 * 2: z accelerometer
	 * 3: BodyANT voltage
	 * 4: BodyANT temperature
	 * 5: BodyANT counter
	 */
	@Override
	public float[] decodeSensorData(byte[] value) {
		short msgBuf[] = new short[10];
    	short x;
    	short y;
        short z;
        short temp;
    	short volt;
    	short count;
    	byte length;
    	
    	float[] data = null;
    	//byte rxChannelNumber = (byte)(value[AntMesg.MESG_DATA_OFFSET] & AntDefine.CHANNEL_NUMBER_MASK);
    	
    	
    		length = value[0];
    		length -= 2;
    		
    		//Copy message to short values
    		msgBuf[1] = (short)(value[1]);
    		msgBuf[2] = (short)(value[2]);
    		msgBuf[3] = (short)(value[3]);
    		msgBuf[4] = (short)(value[4]);
    		msgBuf[5] = (short)(value[5]);
    		msgBuf[6] = (short)(value[6]);
    		msgBuf[7] = (short)(value[7]);
    		msgBuf[8] = (short)(value[8]);
    		msgBuf[9] = (short)(value[9]);
    		
    		//Remove sign bits
    		msgBuf[1] = (short)(msgBuf[1] & 0x00FF);
    		msgBuf[2] = (short)(msgBuf[2] & 0x00FF);
    		msgBuf[3] = (short)(msgBuf[3] & 0x00FF);
    		msgBuf[4] = (short)(msgBuf[4] & 0x00FF);
    		msgBuf[5] = (short)(msgBuf[5] & 0x00FF);
    		msgBuf[6] = (short)(msgBuf[6] & 0x00FF);
    		msgBuf[7] = (short)(msgBuf[7] & 0x00FF);
    		msgBuf[8] = (short)(msgBuf[8] & 0x00FF);
    		msgBuf[9] = (short)(msgBuf[9] & 0x00FF);
    		
    		//CRNT Toolbox code:
    		
    		x = (short)((msgBuf[3] << 2) | (msgBuf[4] >> 6));
    		y = (short)(((msgBuf[4] & 0x3F) << 4) | (msgBuf[5] >> 4));
    		z = (short)(((msgBuf[5] & 0x0F) << 6) | (msgBuf[6] >> 2));
    		
    		count = (short)(msgBuf[7]);
    		temp = (short)((msgBuf[8]) / 2-30);
    		volt = (short)((msgBuf[9])*300/157);
    		x = convertToShort(x);
    		y = convertToShort(y);
    		z = convertToShort(z);
    		newCount = count;
    		if (oldCount != newCount) {
    			data = new float[6];
    			data[0] = (float)x;
    			data[1] = (float)y;
    			data[2] = (float)z;
    			data[3] = (float)volt;
    			data[4] = (float)temp;
    			data[5] = (float)count;
    		    oldCount = newCount;
    		    //Log.d("ANT", msg);
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

		if ((val & 0x0200) > 0) {

		ret = (short) (val | 0xFFFFFC00);

		}

		else {

		ret = val;

		}

		return ret;
    }

	@Override
	public void shutDown() {
		oldCount = -1;
		newCount = 0;
		
	}
	

}
