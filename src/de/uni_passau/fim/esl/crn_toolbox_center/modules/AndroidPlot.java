package de.uni_passau.fim.esl.crn_toolbox_center.modules;

import de.uni_passau.fim.esl.crn_toolbox_center.DataPacket;
import de.uni_passau.fim.esl.crn_toolbox_center.R;

public class AndroidPlot extends GUIModule{

	private Channel channels[]; 	//OPTIONAL: List of Channels
	private int maxRange;			//OPTIONAL: Max range of the Y-axis of the graph
	private int minRange;			//OPTIONAL: Min range of the Y-axis of the graph
	private int nPoints;			//OPTIONAL: Number of points on the X-axis
	private String xName;			//OPTIONAL: Label for the X-axis
	private String yName;			//OPTIONAL: Label for the Y-axis
	
	public class Channel {
		int channelNr;
		String channelName;
		String color;
	}
	
	public int getMaxRange()  {
		return maxRange;
	}
	
	public int getMinRange() {
		return minRange;
	}
	
	public int getNPoints() {
		return nPoints;
	}
	
	public String getXName() {
		return xName;
	}
	
	public String getYName() {
		return yName;
	}
	
	public AndroidPlot() {
		mTabNameID = R.string.android_plot_tabname;
		mDrawableID = R.drawable.androidplot;
	}
	
	public Channel[] getChannels() {
		return channels;
	}
	
	@Override
	public float[] processDataToFloat(float[] data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
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
