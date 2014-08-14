package de.uni_passau.fim.esl.crn_toolbox_center.modules;

public class BatteryStatus extends Module{
	transient private int mLevel;
	transient private int mScale;
	transient private int mTemperature;
	transient private int mVoltage;
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getType() {
		return type;
	}
	
	public float[] decodeSensorData(int level, int scale, int temperature, int voltage) { 
	
		float[] values = new float[4];
		mLevel = level;
		mScale = scale;
		mTemperature = temperature;
		mVoltage = voltage;
		
		values[0] = mLevel;
		values[1] = mScale;
		values[2] = mTemperature;
		values[3] = mVoltage;
		
		return values;
		
	}

}
