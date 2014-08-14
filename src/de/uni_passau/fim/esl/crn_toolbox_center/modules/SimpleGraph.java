package de.uni_passau.fim.esl.crn_toolbox_center.modules;

import de.uni_passau.fim.esl.crn_toolbox_center.DataPacket;
import de.uni_passau.fim.esl.crn_toolbox_center.R;

public class SimpleGraph extends GUIModule{
	
	public SimpleGraph() {
		mTabNameID = R.string.simple_graph_tabname;
		mDrawableID = R.drawable.simplegraph;
		//mTabNameID = R.string.android_plot_tabname;
		//mDrawableID = R.drawable.androidplot;
		
	}
	
	@Override
	public float[] processDataToFloat(float[] data) {
		float[] values = new float[data.length+2];
		
		long timeStamp = System.currentTimeMillis();
		long timeStampSec = timeStamp/1000;
		long miliSecs = timeStamp % timeStampSec;
		long microSecs = miliSecs * 1000;
		values[0] = timeStampSec;
		values[1] = microSecs;
		for(int i = 0; i < data.length; i++) {
			values[i+2] = data[i];
		}
		return values;
	}
	
	public DataPacket processDataToDataPacket(float[] data) {
		Double [] values = new Double[data.length];		
		long timeStamp = System.currentTimeMillis();
		long timeStampSec = timeStamp/1000;
		long miliSecs = timeStamp % timeStampSec;
		long microSecs = miliSecs * 1000;
		
		for(int i = 0; i < data.length; i++) {
			values[i] = (double) data[i];
		}
		return new DataPacket(timeStampSec,microSecs,values,id);
	}

	

}
