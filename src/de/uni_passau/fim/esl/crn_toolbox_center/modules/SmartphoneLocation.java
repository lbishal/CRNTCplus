package de.uni_passau.fim.esl.crn_toolbox_center.modules;

public class SmartphoneLocation extends SmartphoneSensor{
	private int minTimeBetweenUpdates = 0;
	private int minDistanceBetweenUpdates = 0;
	
	public SmartphoneLocation() {
		super();
	}

	
	public float[] decodeSensorData(float[] value) {
		return value;
	}

	@Override
	public String getId() {
		return id;
		
	}
	
	public int getMinTime() {
		return minTimeBetweenUpdates;
	}
	
	public int getMinDistance() {
		return minDistanceBetweenUpdates;
	}

	@Override
	public String getType() {
		return type;
	}
}
