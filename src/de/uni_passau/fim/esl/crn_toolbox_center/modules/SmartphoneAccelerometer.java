package de.uni_passau.fim.esl.crn_toolbox_center.modules;

import android.hardware.SensorManager;

public class SmartphoneAccelerometer extends SmartphoneSensor{
	private int nChannels;
	private String readingRate;
	
	
	public float[] decodeSensorData(float[] value) {
		//float[] returnValue = {0};
		
		
		return value;
		
	}
	public SmartphoneAccelerometer() {
		super();
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
