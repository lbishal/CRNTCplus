/*
 * This file is part of the CRN Toolbox Center.
 * Copyright 2010-2011 Jakob Weigert, University of Passau
 */

package de.uni_passau.fim.esl.crn_toolbox_center.readers;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
//import org.openintents.sensorsimulator.hardware.Sensor;
//import org.openintents.sensorsimulator.hardware.SensorEvent;
//import org.openintents.sensorsimulator.hardware.SensorEventListener;
//import org.openintents.sensorsimulator.hardware.SensorManagerSimulator;
import de.uni_passau.fim.esl.crn_toolbox_center.ToolboxService.ValuesDirectInputNamePair;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.Module;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.SmartphoneAccelerometer;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.SmartphoneLight;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.SmartphoneMagnet;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.SmartphoneOrientation;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.SmartphoneProximity;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.SmartphoneSensor;
/**
 * The SensorReader is responsible for getting sensor data and notifying
 * Observers with it.
 * 
 * @author Jakob Weigert <weigert@fim.uni-passau.de> (University of Passau)
 * 
 */
public //class SensorReader extends Observable {
class SensorReader extends ReaderClass {
	
    // DirectInput IDs and number of channels
    private static final String INPUT_TYPE_ACC = "SmartphoneAccelerometer";
    private static final String INPUT_TYPE_MAG = "SmartphoneMagnet";
    private static final String INPUT_TYPE_ORI = "SmartphoneOrientation";
    private static final String INPUT_TYPE_LIGHT = "SmartphoneLight";
    private static final String INPUT_TYPE_PROX = "SmartphoneProximity";
  
    public static final String[] mSupportedModules = {"SmartphoneAccelerometer", "SmartphoneMagnet",
    	"SmartphoneOrientation", "SmartphoneLight", "SmartphoneProximity"};

    private boolean mSensorListenersRegistered;
    private SensorManager mSensorManager;
    private SensorReader mSensorReader = null;
    private LinkedList<SensorEventListener> mListeners = new LinkedList<SensorEventListener>();
     /**
     * Creates a new SensorReader and register the desired sensor listeners.
     * 
     * @param sensorMgr
     *            The sensor manager got by the system.
     * @param modulesToUse
     *            The desired modules to use as ModuleClass.
     *
     */
    public SensorReader(Context context, List<Module> modulesToUse) {
    	super(context,modulesToUse);
        
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mSensorReader = SensorReader.this;
        if (!mSensorListenersRegistered) {

            Sensor sensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Sensor sensorMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            Sensor sensorOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            Sensor sensorLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            Sensor sensorProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

            //TODO: This might not work if there are diffrent type of modules!
            Iterator<Module> it = modulesToUse.iterator();
            Module module;
            
            while (it.hasNext()) {
            	module = it.next();
            	if (module.getType().equals(INPUT_TYPE_ACC)) {
            		SmartphoneAccelerometer accModule = (SmartphoneAccelerometer)module;
            		SensorEventListener sensorListenerAcc = new SensorEventListenerImp2(accModule);
            		mListeners.add(sensorListenerAcc);
            		mSensorManager.registerListener(sensorListenerAcc, sensorAcc, accModule.getReadingRate());
            	}
            	if (module.getType().equals(INPUT_TYPE_MAG)) {
            		SmartphoneMagnet magModule = (SmartphoneMagnet)module;
            		SensorEventListener sensorListenerMag = new SensorEventListenerImp2(magModule);
            		mListeners.add(sensorListenerMag);
            		mSensorManager.registerListener(sensorListenerMag, sensorMag, magModule.getReadingRate());
            	}
            	if (module.getType().equals(INPUT_TYPE_ORI)) {
            		SmartphoneOrientation oriModule = (SmartphoneOrientation)module;
            		SensorEventListener sensorListenerOri = new SensorEventListenerImp2(oriModule);
            		mListeners.add(sensorListenerOri);
            		mSensorManager.registerListener(sensorListenerOri, sensorOrientation, oriModule.getReadingRate());
            	}
            	if (module.getType().equals(INPUT_TYPE_LIGHT)) {
            		SmartphoneLight lightModule = (SmartphoneLight)module;
            		SensorEventListener sensorListenerLight = new SensorEventListenerImp2(lightModule);
            		mListeners.add(sensorListenerLight);
            		mSensorManager.registerListener(sensorListenerLight, sensorLight, lightModule.getReadingRate());
            	}
            	if (module.getType().equals(INPUT_TYPE_PROX)) {
                    SmartphoneProximity proxModule = (SmartphoneProximity)module;
            		SensorEventListener sensorListenerProx = new SensorEventListenerImp2(proxModule);
            		mListeners.add(sensorListenerProx);
            		mSensorManager.registerListener(sensorListenerProx, sensorProximity, proxModule.getReadingRate());
            	}
            	

            }
            mSensorListenersRegistered = true;

        }

    }
    
    public SensorReader getInstance(){
    	return mSensorReader;
    }

    public void shutDown() {
    	Iterator<Module> moduleIterator = mModules.iterator();
		Module module = null;
		while (moduleIterator.hasNext()) {
			module = moduleIterator.next();
			module.setReady(false);
		}
    	for (SensorEventListener listener : mListeners) {
            
        	mSensorManager.unregisterListener(listener);
        }
        mListeners.clear();
       
    }

    /**
     * An implementation of a service event listener. Notifies observers with
     * new sensor data.
     */
    class SensorEventListenerImpl implements SensorEventListener {
        String mDirectInputName;
        int mMaxNumberOfChannels;

        public SensorEventListenerImpl(String directInputName, int maxNumberOfChannels) {
            super();
            this.mDirectInputName = directInputName;
            this.mMaxNumberOfChannels = maxNumberOfChannels;
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

        public void onSensorChanged(SensorEvent sensorEvent) {
            setChanged();
            float[] values = sensorEvent.values;
            if (values.length > mMaxNumberOfChannels) {
                // Cut array to max number of channels wished
                float[] valuesNew = new float[mMaxNumberOfChannels];
                for (int i = 0; i < valuesNew.length; i++) {
                    valuesNew[i] = values[i];
                }
                values = valuesNew;
            }
            ValuesDirectInputNamePair pair = new ValuesDirectInputNamePair(mDirectInputName, values);
            notifyObservers(pair);
        }
    }
    
    /**
     * An implementation of a service event listener. Notifies observers with
     * new sensor data.
     */
    class SensorEventListenerImp2 implements SensorEventListener {
        private SmartphoneSensor mModule;
        private ValuesDirectInputNamePair mValPair;

        public SensorEventListenerImp2(SmartphoneSensor module) {
            super();
            this.mModule = module;
            mValPair = new ValuesDirectInputNamePair(module.getId(), null);
            
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

        public void onSensorChanged(SensorEvent sensorEvent) {
        	//setChanged();
            float[] sensorValues = sensorEvent.values;
            float[] values;
            values = mModule.decodeSensorData(sensorValues);
        	
            //ValuesDirectInputNamePair pair = new ValuesDirectInputNamePair(mModule.getId(), values);
            mValPair.setValues(values);
           //notifyObservers(mValPair);
            sendData(mValPair);
        }
    }

    /*
	@Override
	public ModuleClass getModuleById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getSupportedModules() {
		return mSupportedModules;
	}
     */
}