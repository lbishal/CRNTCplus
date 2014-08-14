package de.uni_passau.fim.esl.crn_toolbox_center.readers;

import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject.NotifyReason;
import de.uni_passau.fim.esl.crn_toolbox_center.ToolboxService;
import de.uni_passau.fim.esl.crn_toolbox_center.ToolboxService.ValuesDirectInputNamePair;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.BluetoothModule;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.Module;

public class BluetoothReader extends ReaderClass implements Observer{

	public static final int CONNECTING = 0;
	public static final int CONNECTED = 1;
	public static final int ERROR = 3;
	public static final int STOPPED = 4;
	public static final int STREAMING = 5;
	
	private final BluetoothAdapter mBluetoothAdapter;
	Set<BluetoothDevice> mPairedDevices;
	public static final String[] mSupportedModules = {"Shimmer", "ZephyrHxM"};
	
	
	public BluetoothReader(Context context, List<Module> modulesToUse) {
		//TODO Still unfinished. 
		super(context, modulesToUse);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		Iterator<Module> it = mModules.iterator();
		BluetoothModule module = null;
		
		
		if (mBluetoothAdapter == null) {
    		setChanged();
 			notifyObservers(new NotifyObject(NotifyReason.READER_ERROR, ToolboxService.BLUETOOTH_NOT_ENABLED));
		}
		if (mBluetoothAdapter.isEnabled()) {
		   
			//ModuleClass[] modules = mModules.toArray(new ModuleClass[0]);
		    it = mModules.iterator();
		    while (it.hasNext()) {
		    //for (int i =0; i < modules.length; i++) {
		    	//module = (BluetoothModule)modules[i];
		    	module = (BluetoothModule)it.next();
		    	BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(module.getAddress());
		    	if (device.getAddress().equals(module.getAddress())){
		    		
		    		if (module.initConnection(context)) {
		    			module.addObserver(this);
		    			module.connect(device);
		    			while (module.getState() == BluetoothModule.STATE_CONNECTING) {}
		    		}

		    	}
		    }
		    it = mModules.iterator();
			module = null;
			while (it.hasNext()) {
				module = (BluetoothModule) it.next(); 
				module.startStreaming();
			}

		} else {
			setChanged();
 			notifyObservers(new NotifyObject(NotifyReason.READER_ERROR, ToolboxService.BLUETOOTH_NOT_ENABLED));
		}
		
		
	}


	public void update(Observable observable, Object data) {
		
		if (data instanceof NotifyObject) {
			NotifyObject object = (NotifyObject) data;
			switch (object.getNotifyReason()) {
			case BT_SENSOR_DATA:
				if (object.getData() instanceof ValuesDirectInputNamePair) {
					ValuesDirectInputNamePair pair = (ValuesDirectInputNamePair) object.getData();
					sendData(pair);
				}
			}
		}
		
	}


	@Override
	public void shutDown() {
		Iterator<Module> it = mModules.iterator();
		BluetoothModule module = null;
		while (it.hasNext()) {
			module = (BluetoothModule) it.next();
			module.setReady(false);
			module.shutDown();
		}
		
	}
	
	
	

}
