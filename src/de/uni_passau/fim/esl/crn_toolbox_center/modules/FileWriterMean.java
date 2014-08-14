package de.uni_passau.fim.esl.crn_toolbox_center.modules;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FileWriterMean extends FileWriter{
	private int windowSize;
	private float upperThreshold;
	private float lowerThreshold;
	private float upperValue;
	private float lowerValue;
	private float betweenValue;
	
	public int getWindowSize() {
		return windowSize;
	}
	public float getUpperValue() {
		return upperValue;
	}
	public float getLowerValue() {
		return lowerValue;
	}
	public float getBetweenValue() {
		return betweenValue;
	}
	
	public String processDataToString(float[] data) {
		for (int i = 0; i < getNChannels(); i++) {
			if (data[i] >= upperThreshold) {
				data[i] = upperValue;
			}
			if (data[i] <= lowerThreshold) {
				data[i] = lowerValue;
			}
			if (data[i] < upperThreshold && data[i] > lowerThreshold) {
				data[i] = betweenValue;
			}
		}
		return super.processDataToString(data);
	
	}
}
