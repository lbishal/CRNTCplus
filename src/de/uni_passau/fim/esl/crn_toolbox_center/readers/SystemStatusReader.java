package de.uni_passau.fim.esl.crn_toolbox_center.readers;

import java.util.Iterator;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.BatteryStatus;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.Module;

public class SystemStatusReader extends ReaderClass{

	private BatteryStatus mBatteryModule;
	
	public static final String[] mSupportedModules = {"BatteryStatus","CPUusage"};
	
	public SystemStatusReader(Context context, List<Module> modules) {
		super(context, modules);
		mBatteryModule = (BatteryStatus) getModuleByType("BatteryStatus");
		if (mBatteryModule != null) {
			IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
			context.registerReceiver(batteryReceiver, filter);
		}
        
		
	}
	/*
	@Override
	public ModuleClass getModuleById(String id) {
		Iterator<ModuleClass> it = mModules.iterator();
		ModuleClass module = null;
		while (it.hasNext()) {
			module = it.next();
			if (module.getId().equals(id)) {
				return module;
			}
		}
		return null;
	}
	*/
	private Module getModuleByType(String type) {
		Iterator<Module> it = mModules.iterator();
		Module module = null;
		while (it.hasNext()) {
			module = it.next();
			if (module.getType().equals(type)) {
				return module;
			}
		}
		return null;
	}
	/*
	@Override
	public String[] getSupportedModules() {
		return mSupportedModules;
	}
	*/
	/** Receive smart-phone battery information. */
    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        
        //String text;
        
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
            int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
            
            mBatteryModule.decodeSensorData(level, scale, temperature, voltage);
            Log.i("BatteryManager", "level is "+level+"/"+scale+", temp is "+temperature+", voltage is "+voltage);
        
        };
    };

	@Override
	public void shutDown() {
		Iterator<Module> moduleIterator = mModules.iterator();
		Module module = null;
		while (moduleIterator.hasNext()) {
			module = moduleIterator.next();
			module.setReady(false);
		}
		mContext.unregisterReceiver(batteryReceiver);
		
	}
}
