package de.uni_passau.fim.esl.crn_toolbox_center.modules;

public class SuuntoBelt extends ANTModule{

	transient private short batteryStatus;
	transient private short activeTime;
	transient private short count;
	transient private byte[] networkKeyMsg = {0x11,0x76,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
	@Override
	public float[] decodeSensorData(byte[] value) {
		short msgBuf[] = new short[11];
		short tval1;
		short tval2;
		short tval3;
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
		msgBuf[10] = (short)(value[10]);
		
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
		msgBuf[10] = (short)(msgBuf[10] & 0x00FF);
		
		short mesgType = msgBuf[3];
		switch(mesgType) {
		case 0x02: 
			batteryStatus = msgBuf[5];
			activeTime = (short) (msgBuf[7] << 8 | msgBuf[6]);
			break;
		case 0x01: 
			count = msgBuf[4];
			tval1 = (short) (msgBuf[6] << 8 | msgBuf[5]);
			tval2 = (short) (msgBuf[8] << 8 | msgBuf[7]);
			tval3 = (short) (msgBuf[10] << 8 | msgBuf[9]);
			break;
			
		default: 
			break;
		}
		
		return null;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getType() {
		return type;
	}
	
	public byte[] getNetworkKeyMsg() {
		return networkKeyMsg;
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}

}
