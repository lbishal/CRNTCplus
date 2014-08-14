package de.uni_passau.fim.esl.crn_toolbox_center.modules;

import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject.NotifyReason;
import de.uni_passau.fim.esl.crn_toolbox_center.ToolboxService.ValuesDirectInputNamePair;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;


import zephyr.android.HxMBT.*;
import zephyr.android.HxMBT.ConnectListenerImpl.HRSpeedDistPacketInfo;

public class ZephyrHxM extends BluetoothModule{
	
	private transient String[] mSensorNames;
	private transient float previousDistance;
	private transient float previousStride;
	private transient float[] mDataValues;
	private transient ValuesDirectInputNamePair pair;
	private transient ZephyrProtocol protocol;
	private transient BTClient client;
	
	private int HR_SPD_DIST_PACKET =0x26;
	
	
	public ZephyrHxM() {
		//heart rate, speed, distance
		mDataValues = null;
		mSensorNames = new String[4];
		
		mSensorNames[0] = "Heart rate";
		mSensorNames[1] = "Speed";
		mSensorNames[2] = "Distance";
		mSensorNames[3] = "Strides";
		
		pair = new ValuesDirectInputNamePair();		
	}
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getType() {
		return type;
	}
	
		
	@Override
	public boolean initConnection(Context context) {
		boolean succes = false;
				
		succes=true;
		
		return succes;
	}
	
	@Override
	public boolean connect(BluetoothDevice device) {
		boolean success = false;
		
		client = new BTClient(BluetoothAdapter.getDefaultAdapter(), device.getAddress());
		client.addConnectedEventListener(connectedListener);
		
		success=client.IsConnected();
		
		return success;
	}

	@Override
	public int getState() {
		int state = 0;
		
		if(client == null)
			state = STATE_NONE;
		else
		{
			if( client.IsConnected())
				state = STATE_CONNECTED;
			else
				state= STATE_CONNECTING;
		}
		
		return state;
	}

	@Override
	public void startStreaming() {
		client.start();		
	}

	@Override
	public void shutDown() {
		client.removeConnectedEventListener(connectedListener);
		client.Close();
		
		client = null;
		protocol = null;
		
		mDataValues = null;
		
		moduleReady = false;
	}
	
	private class MessageParser extends ConnectListenerImpl
	{

		public MessageParser() {
			super(null, null);
			// TODO Auto-generated constructor stub
		}
		
		public final HRSpeedDistPacketInfo messageParser = new HRSpeedDistPacketInfo();
	}
	
	private ConnectedListener<BTClient> connectedListener = new ConnectedListener<BTClient>() {
		
		//HRSpeedDistPacketInfo HRSpeedDistPacket = connectedListener.new HRSpeedDistPacketInfo();// new HRSpeedDistPacketInfo();
				
		//Connection established event
		public void Connected(ConnectedEvent<BTClient> eventArgs) {
					
			//Creates a new ZephyrProtocol object and passes it the BTComms object
			protocol = new ZephyrProtocol( eventArgs.getSource().getComms());
			
			protocol.addZephyrPacketEventListener(new ZephyrPacketListener() {
				public void ReceivedPacket(ZephyrPacketEvent eventArgs) {
					ZephyrPacketArgs msg = eventArgs.getPacket();
					byte CRCFailStatus;
					float distance, stride, temp;
					
														
					CRCFailStatus = msg.getCRCStatus();
					
					//Stride and Heart rate are bytes, the cast to float is not working,
					//because Java assumes they are signed 8-bit integers. A value above 127 
					//will be considered as a negative number
					if( HR_SPD_DIST_PACKET == msg.getMsgID() && (CRCFailStatus==0))
					{
						byte [] DataArray = msg.getBytes();
						
						HRSpeedDistPacketInfo parser = new MessageParser().messageParser;
						stride = (float)parser.GetStrides(DataArray);
						distance = (float)parser.GetDistance(DataArray);
						
						if( mDataValues != null)
						{
							mDataValues[0] = (float)parser.GetHeartRate(DataArray);
							mDataValues[1] = (float)parser.GetInstantSpeed(DataArray);
													
							if( previousDistance != distance)
							{
								if( previousDistance < distance)
									temp = (distance - previousDistance);
								else
								{
									//Distance rolls over at 256 meters
									temp = (float) (256.0-previousDistance);
									temp += distance;
								}
								
								//distance from sensor is in 1/16 meters
								mDataValues[2] += temp;
								previousDistance = distance;							
							}
															
							if( previousStride != stride)
							{
								if( previousStride < stride)
									temp = (stride - previousStride);
								else
								{
									//Stride rolls over at 128 steps
									temp = (float) (128.0 - previousStride);
									temp += stride;
								}
								
								mDataValues[3] += temp;
								previousStride = stride;
							}							
							
							pair.setDirectInputName(id);
							pair.setValues(mDataValues);
			             	   
		             	    setChanged();
		             	    notifyObservers(new NotifyObject(NotifyReason.BT_SENSOR_DATA, pair));
						}
						else
						{
							mDataValues = new float[4];
							
							for( int i=0; i < 4; i++)
								mDataValues[i]=0;
							
							previousStride = stride;
							previousDistance = distance;							
						}
					}				
					
				}
			});
		}
	};
	
}
