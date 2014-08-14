/*
 * This file is part of the CRN Toolbox Center.
 * Copyright 2010-2011 Jakob Weigert, University of Passau
 */

package de.uni_passau.fim.esl.crn_toolbox_center.readers;

import java.util.Iterator;
import java.util.List;
//import java.util.Observable;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import de.uni_passau.fim.esl.crn_toolbox_center.ToolboxService.ValuesDirectInputNamePair;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.Module;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.SmartphoneSensor;
//import de.uni_passau.fim.esl.crn_toolbox_center.modules.SmartphoneAccelerometer;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.SmartphoneLocation;

/**
 * The LocationReader is responsible for getting location sensor data and
 * notifying Observers with it.
 * 
 * @author Jakob Weigert <weigert@fim.uni-passau.de> (University of Passau)
 * 
 */
public class LocationReader extends ReaderClass {

    public static final String INPUT_ID_GPS = "GPSInput";
    public static final String[] mSupportedModules = {"SmartphoneLocation"};

    private LocationManager mLocationManager;
    private LocationListener mListener;
    private boolean mLocationListenersRegistered;

    public LocationReader(Context context, List<Module> modulesToUse) {
    	super(context, modulesToUse);
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (!mLocationListenersRegistered) {
        	Iterator<Module> it = modulesToUse.iterator();
            Module module;
            while(it.hasNext()) {
            	module = it.next();
            	if (module.getId().equals(INPUT_ID_GPS)) {
            		SmartphoneLocation locationModule = (SmartphoneLocation)module;
            		mListener = new LocationListenerImpl();
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    		locationModule.getMinTime(), locationModule.getMinDistance(), mListener);
            	}
            }
        	
        }

    }

    /**
     * Unregisters location listeners from the location manager.
     */
    public void shutDown() {
    	Iterator<Module> moduleIterator = mModules.iterator();
		Module module = null;
		while (moduleIterator.hasNext()) {
			module = moduleIterator.next();
			module.setReady(false);
		}
    }

    /**
     * An implementation of a location listener. Notifies observers with new
     * sensor data.
     */
    class LocationListenerImpl implements LocationListener {

        // @Override
        public void onLocationChanged(Location loc) {

            // Toolbox only accepts floats, no doubles
            float latitude = (float) loc.getLatitude();
            float longitude = (float) loc.getLongitude();
            float altitude = (float) loc.getAltitude();
            float bearing = (float) loc.getBearing();
            float speed = (float) loc.getSpeed();
            float accuracy = (float) loc.getAccuracy();

            //setChanged();
            float[] values = { latitude, longitude, altitude, bearing, speed, accuracy };

            ValuesDirectInputNamePair pair = new ValuesDirectInputNamePair(INPUT_ID_GPS, values);
            //notifyObservers(pair);
            sendData(pair);

        }

        // @Override
        public void onProviderDisabled(String provider) {}

        // @Override
        public void onProviderEnabled(String provider) {}

        // @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

    }
    
    /**
     * An implementation of a location listener. Notifies observers with new
     * sensor data.
     */
    class LocationListenerImp2 implements LocationListener {
    	private SmartphoneSensor mModule;
    	
    	public LocationListenerImp2(SmartphoneSensor module) {
    		super();
    		mModule = module;
    	}
        // @Override
        public void onLocationChanged(Location loc) {

            // Toolbox only accepts floats, no doubles
            float latitude = (float) loc.getLatitude();
            float longitude = (float) loc.getLongitude();
            float altitude = (float) loc.getAltitude();
            float bearing = (float) loc.getBearing();
            float speed = (float) loc.getSpeed();
            float accuracy = (float) loc.getAccuracy();

            setChanged();
            float[] locValues = { latitude, longitude, altitude, bearing, speed, accuracy };
            float[] values = mModule.decodeSensorData(locValues);
            
            ValuesDirectInputNamePair pair = new ValuesDirectInputNamePair(INPUT_ID_GPS, values);
            notifyObservers(pair);

        }

        // @Override
        public void onProviderDisabled(String provider) {}

        // @Override
        public void onProviderEnabled(String provider) {}

        // @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

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
