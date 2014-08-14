package de.uni_passau.fim.esl.crn_toolbox_center.modules;

import java.util.Collection;

import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;

import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject.NotifyReason;
import de.uni_passau.fim.esl.crn_toolbox_center.ToolboxService.ValuesDirectInputNamePair;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class Shimmer extends BluetoothModule{
	
	private transient com.shimmerresearch.driver.Shimmer mShimmer = null;
	private transient String[] mSensorNames;
	private transient float[] mDataValues;
	private transient int mLength = 0;
	
	//Shimmer configuration file values
	private int samplingrate;			//OPTIONAL: Sampling rate of the Shimmer: 0,10,50,100,125,166,200,250,500 or 1000Hz Default: 0
	private boolean accelerometer;		//OPTIONAL: Activate accelerometer. Default: false
	private boolean gyroscope;			//OPTIONAL: Activate gyroscope. Default: false
	private boolean magnetometer;		//OPTIONAL: Activate magnetometer. Default: false
	private boolean ecg;				//OPTIONAL: Activate ECG. Default: false
	private boolean emg;				//OPTIONAL: Activate EMG. Default: false
	private boolean gsr;				//OPTIONAL: Activate GSR. Default: false
	private boolean anex_a7;			//OPTIONAL: Activate anex a7. Default: false
	private boolean anex_a0;			//OPTIONAL: Activate anex a0. Default: false
	private boolean heart;				//OPTIONAL: Activate heart. Default: false
	private boolean strain;				//OPTIONAL: Activate strain. Default: false
	private boolean calibrated;			//OPTIONAL: Use calibrated data
	private int accRange;				//OPTIONAL: 0: (+-1,5G), 1: (+- 2G) , 2: (+- 4G) or 3: (+- 6G)
	private int gsrRange;				//OPTIONAL: 0: (10-56 kOhm) ,1: (56-220 kOhm), 2: (220-680 kOhm)
										//			3: (680-4700 kOhm ) or 4: (auto range);
	
	public Shimmer() {
		
				
	}
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getType() {
		return type;
	}
	
	public String getCalibratedString() {
		if (calibrated) {
			return "Calibrated";
		} else {
			return "Uncalibrated";
		}
	}
	
	private double setSamplingRate() {
		int setSamplingRate = 0;
		
		switch (samplingrate) {
		case 0: 
			setSamplingRate = (byte) 255;
			break;
		case 10:
			setSamplingRate = 100;
			break;
		case 50:
			setSamplingRate = 20;
			break;
		case 100:
			setSamplingRate = 10;
			break;
		case 125:
			setSamplingRate = 8;
			break;
		case 166:
			setSamplingRate = 6;
			break;
		case 200:
			setSamplingRate = 5;
			break;
		case 250:
			setSamplingRate = 4;
			break;
		case 500:
			setSamplingRate = 2;
			break;
		case 1000:
			setSamplingRate = 1;
			break;
		default:
			setSamplingRate = (byte) 255;
			break;
		}
		
			return setSamplingRate;
	}
	
	private int setSensors() {
		
		int setSensorMsg = 0;
		if (accelerometer) {
			setSensorMsg = (byte) (setSensorMsg | com.shimmerresearch.driver.Shimmer.SENSOR_ACCEL);
			mSensorNames[mLength] = "AccelerometerX";
			mLength++;
			mSensorNames[mLength] = "AccelerometerY";
			mLength++;
			mSensorNames[mLength] = "AccelerometerZ";
			mLength++;
		}
		if (gyroscope) {
			setSensorMsg = (byte) (setSensorMsg | com.shimmerresearch.driver.Shimmer.SENSOR_GYRO);
			mSensorNames[mLength] = "GyroscopeX";
			mLength++;
			mSensorNames[mLength] = "GyroscopeY";
			mLength++;
			mSensorNames[mLength] = "GyroscopeZ";
			mLength++;
		}
		if (magnetometer) {
			setSensorMsg = (byte) (setSensorMsg | com.shimmerresearch.driver.Shimmer.SENSOR_MAG);
			mSensorNames[mLength] = "MagnetometerX";
			mLength++;
			mSensorNames[mLength] = "MagnetometerY";
			mLength++;
			mSensorNames[mLength] = "MagnetometerZ";
			mLength++;
		}
		if (ecg) {
			setSensorMsg = (byte) (setSensorMsg | com.shimmerresearch.driver.Shimmer.SENSOR_ECG);
			mSensorNames[mLength] = "ECGRALL";
			mLength++;
			mSensorNames[mLength] = "ECGLALL";
			mLength++;
		}
		if (emg) {
			setSensorMsg = (byte) (setSensorMsg | com.shimmerresearch.driver.Shimmer.SENSOR_EMG);
			mSensorNames[mLength] = "EMG";
			mLength++;
		}
		if (gsr) {
			setSensorMsg = (byte) (setSensorMsg | com.shimmerresearch.driver.Shimmer.SENSOR_GSR);
			mSensorNames[mLength] = "GSR";
			mLength++;
		}
		if (anex_a7) {
			setSensorMsg = (byte) (setSensorMsg | com.shimmerresearch.driver.Shimmer.SENSOR_EXP_BOARD_A7);
			mSensorNames[mLength] = "ExpBoardA7";
			mLength++;
		}
		if (anex_a0) {
			setSensorMsg = (byte) (setSensorMsg | com.shimmerresearch.driver.Shimmer.SENSOR_EXP_BOARD_A0);
			mSensorNames[mLength] = "ExpBoardA0";
			mLength++;
		}
		if (heart) {
			setSensorMsg = (byte) (setSensorMsg | com.shimmerresearch.driver.Shimmer.SENSOR_HEART);
			mSensorNames[mLength] = "Heart Rate";
			mLength++;
		}
		if (strain) {
			setSensorMsg = (byte) (setSensorMsg | com.shimmerresearch.driver.Shimmer.SENSOR_STRAIN);
			mSensorNames[mLength] = "Strain Gauge High";
			mLength++;
			mSensorNames[mLength] = "Strain Gauge Low";
			mLength++;
		}
		
		mSensorNames[mLength] = "TimeStamp";
		mLength++;
		
		mDataValues = new float[mLength];
		return setSensorMsg;
		
	}
	
	// The Handler that gets information back from the BluetoothChatService
    private Handler mHandler = new Handler() {
        @Override
        
        public void handleMessage(Message msg) {
        	
        	
        	switch (msg.what) {
        	case com.shimmerresearch.driver.Shimmer.MESSAGE_STATE_CHANGE:
        		break;
        	case com.shimmerresearch.driver.Shimmer.MESSAGE_READ:
        		 if ((msg.obj instanceof ObjectCluster)){
             	    ObjectCluster objectCluster =  (ObjectCluster) msg.obj; 
             	    
             	    for (int i = 0; i < mLength; i++) {
             	    	Collection<FormatCluster> ofFormats = objectCluster.mPropertyCluster.get(mSensorNames[i]);
             	    	mDataValues[i] = (float) ((FormatCluster)objectCluster.returnFormatCluster(ofFormats,getCalibratedString())).mData; 
             	    	
             	    }
             	    ValuesDirectInputNamePair pair = new ValuesDirectInputNamePair(id, mDataValues);
             	   
             	    setChanged();
             	    notifyObservers(new NotifyObject(NotifyReason.BT_SENSOR_DATA, pair));
             	   
        		 }
        		break;
        	case com.shimmerresearch.driver.Shimmer.MESSAGE_ACK_RECEIVED:
             	break;
        	case com.shimmerresearch.driver.Shimmer.MESSAGE_TOAST:
        		break;
        		
        	}
        }
    };
	
	
	@Override
	public boolean initConnection(Context context) {
		boolean succes = false;
		mSensorNames = new String[19];
		
		mDataValues = new float[19];
		mShimmer = new com.shimmerresearch.driver.Shimmer(context, mHandler, id, samplingrate, 0, 0, setSensors(), true);
		if (mShimmer != null) {
			succes = true;
		} else {
			succes = false;
		}
		return succes;
	}
	
	@Override
	public boolean connect(BluetoothDevice device) {
		if (mShimmer != null) {
			mShimmer.connect(device);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int getState() {
		int state = 0;
		switch (mShimmer.getState()) {
		case com.shimmerresearch.driver.Shimmer.STATE_NONE:
			state = STATE_NONE;
			break;
		case com.shimmerresearch.driver.Shimmer.STATE_CONNECTING:
			state = STATE_CONNECTING;
			break;
		case com.shimmerresearch.driver.Shimmer.STATE_CONNECTED:
			state = STATE_CONNECTED;
			break;
		}
		return state;
	}

	@Override
	public void startStreaming() {
		mShimmer.startStreaming();
		
	}

	@Override
	public void shutDown() {
		mShimmer.stopStreaming();
		mShimmer.stop();
		mHandler.removeMessages(com.shimmerresearch.driver.Shimmer.MESSAGE_READ);
		mShimmer = null;
		mSensorNames = null;
		mDataValues = null;
		mLength = 0;
		moduleReady = false;
	}


	

	
}
