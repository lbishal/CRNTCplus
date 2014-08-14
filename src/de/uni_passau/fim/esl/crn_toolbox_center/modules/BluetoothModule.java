package de.uni_passau.fim.esl.crn_toolbox_center.modules;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

abstract public class BluetoothModule extends Module {
	public transient static final int STATE_NONE = 0;
	public transient static final int STATE_CONNECTING = 1;
	public transient static final int STATE_CONNECTED = 2;
	
	protected String address;					//REQUIRED: MAC address of the Bluetooth device
	protected String name;						//OPTIONAL: Name of the Bluetooth device
	protected transient String uuid;
	protected transient BluetoothDevice mDevice;
	protected transient int mDataSize;
	protected transient boolean mServer;
	
	public String getAddress() {
		return address;
	}
	
	public String getName() {
		return name;
	}
	
	public BluetoothDevice getDevice() {
		return mDevice;
	}
	
	public void setDevice(BluetoothDevice device) {
		mDevice = device;
	}
	public String getUUID() {
		return uuid;
	}
	
	abstract public boolean connect(BluetoothDevice device);
	
	abstract public int getState();
	
	abstract public boolean initConnection(Context context);
	abstract public void startStreaming();
	abstract public void shutDown();
	
}
