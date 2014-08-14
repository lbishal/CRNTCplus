package de.uni_passau.fim.esl.crn_toolbox_center.modules;

public class RandomGenerator extends Module{
	private int frequency;
	private int offset;
	
	public int getFrequency() {
		return frequency;
	}
	
	public float getPeriodInMs() {
		float val = (1.0f/frequency)*1000;
		return val;
	}
	
	public int getOffSet() {
		return offset;
	}
}
