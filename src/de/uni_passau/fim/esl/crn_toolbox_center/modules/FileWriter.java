package de.uni_passau.fim.esl.crn_toolbox_center.modules;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.uni_passau.fim.esl.crn_toolbox_center.DataPacket;

public class FileWriter extends OutputModule{
	private String pathname;	//REQUIRED: Full name of the file and the path. Should always start with: sdcard/
	private String dateFormat;	//OPTIONAL: Specify the date format in a String.
	private String timeFormat;	//OPTIONAL: Specify the time format in a String.
	private String seperator;	//OPTIONAL: Specify the separator character. Default: ,
	private String header;		//OPTIONAL: Specify a header for the columns. Default: null
	private boolean UnixTime;	//OPTIONAL: Log date and time in Unix epoch notation: Default: true
	private boolean append;		//OPTIONAL: Append data to the same file. Default: false
	
	public String getPathName() {
		return pathname;
	}

	@Override
	public float[] processDataToFloat(float[] data) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public String processDataToString(float[] data) {
		String entry = null;
		if (seperator == null) {
			seperator = new String();
			seperator = ",";
		}
		if (!UnixTime && (dateFormat != null) && (timeFormat != null)) {
			SimpleDateFormat sdfDate;
			SimpleDateFormat sdfTime;
			sdfDate = new SimpleDateFormat(dateFormat);
			sdfTime = new SimpleDateFormat(timeFormat);
			Date date = Calendar.getInstance().getTime();
			String dateStr = sdfDate.format(date);
			String timeStr = sdfTime.format(date);
			entry = dateStr + seperator + timeStr + seperator;
			for (int i = 0; i < data.length; i++) {
				entry = entry + Float.toString(data[i]) + seperator;
			}
			
			entry = entry + "\n";
		} else {
			long timeStamp = System.currentTimeMillis();
			long timeStampSec = timeStamp/1000;
			long miliSecs = timeStamp % timeStampSec;
			long microSecs = miliSecs * 1000;
			//double timeStampMSec = timeStamp - timeStampSec;
			entry = Long.toString(timeStampSec) + seperator + Long.toString(microSecs) + seperator;
			for (int i = 0; i < data.length; i++) {
				entry = entry + Float.toString(data[i]) + seperator;
			}
			entry = entry + "\n";
		}
		return entry;
	}
	
	public boolean getAppend() {
		return append;
	}
	
	public String getHeader() {
		return header;
	}

	@Override
	public DataPacket processDataToDataPacket(float[] data) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
