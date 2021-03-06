package de.uni_passau.fim.esl.crn_toolbox_center.modules;

import android.hardware.SensorManager;

public class SmartphoneLight extends SmartphoneSensor{

	String readingRate;
	int nChannels;
	
	public SmartphoneLight() {
		super();
	}

	
	public float[] decodeSensorData(float[] value) {
		
		return value;
	}

	@Override
	public String getId() {
		
		return id;
	}
	
	public int getNChannels() {
		return nChannels;
	}
	public int getReadingRate() {
		int returnVal = SensorManager.SENSOR_DELAY_NORMAL;
		if (readingRate.equals("NORMAL")) {
			returnVal = SensorManager.SENSOR_DELAY_NORMAL;
        } else if (readingRate.equals("UI")) {
            returnVal = SensorManager.SENSOR_DELAY_UI;
        } else if (readingRate.equals("GAME")) {
            returnVal = SensorManager.SENSOR_DELAY_GAME;
        } else if (readingRate.equals("FASTEST")) {
            returnVal = SensorManager.SENSOR_DELAY_FASTEST;
        }
		return returnVal;
	}

	@Override
	public String getType() {
		return type;
	}
}
