package de.uni_passau.fim.esl.crn_toolbox_center;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.text.format.Time;



public class LogObject {
	static final int SUCCEEDED = 0;
	static final int IO_EXCEPTION = 14;
	public static final int LOG_BATTERY = 15;
	static final int LOG_ANT_CONTINOUS = 16;
	static final int LOG_ANT_STARTSTOP = 17;
	
	private String mFileName;
	private File f;
	private File fh; //help file pointer 
	private FileOutputStream out;
	private FileInputStream in;
	private int writePos = 0;
	private int fileNameCounter = 0;
	private String dateFormat = "dd/MM/yyyy";
	private String timeFormat = "HH:mm:ss:SSSS";
	SimpleDateFormat sdfDate;
	SimpleDateFormat sdfTime;
	Time timeTest;
	
	
	public LogObject(int mode, String filename) {
		String header;
		byte[] data;
		String fnCounter;
		
		switch(mode) {
		case LOG_BATTERY:
			mFileName = filename;
			header = "Date, Time, Level/Scale, Voltage, Temperature\n";
			f = new File(mFileName);
			sdfDate = new SimpleDateFormat(dateFormat);
			sdfTime = new SimpleDateFormat(timeFormat);
			
			data = new String(header).getBytes();
			if (!f.exists()) {
				try {
					//f.createNewFile();
					out = new FileOutputStream(f);
					out.write(data);
					writePos += header.length();
					out.flush();
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
			break;
		case LOG_ANT_CONTINOUS:
			mFileName = "sdcard/Epilepsy/" + filename;
			header = "Date, Time, x, y, z, voltage, temperature, count\n";
			f = new File(mFileName);
			sdfDate = new SimpleDateFormat(dateFormat);
			sdfTime = new SimpleDateFormat(timeFormat);
			data = new String(header).getBytes();
			if (!f.exists()) {
				try {
					//f.createNewFile();
					out = new FileOutputStream(f);
					out.write(data);
					writePos += header.length();
					out.flush();
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		case LOG_ANT_STARTSTOP:
			String fileHelp = "sdcard/" + filename + "counter";
			fh = new File(fileHelp);
			fnCounter = new Integer(fileNameCounter).toString();
			//Check if there is a counter file
			
			if (!fh.exists()) {	
				try {
					fnCounter = new Integer(fileNameCounter).toString();
					data = fnCounter.getBytes();
					//fh.createNewFile();
					out = new FileOutputStream(fh);
					out.write(data);
					out.flush();
					out.close();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					in = new FileInputStream(fh);
					data = new byte[in.available()];
					in.read(data);
					fnCounter = new String(data);
					fileNameCounter = new Integer(fnCounter);
				} catch (IOException e) {
					
				}
				
			}
			
			
			sdfDate = new SimpleDateFormat(dateFormat);
			sdfTime = new SimpleDateFormat(timeFormat);
			/*
			Date date = Calendar.getInstance().getTime();
			String dateStr = sdfDate.format(date);
			String timeStr = sdfTime.format(date);
			*/
			mFileName = "sdcard/" + filename + fnCounter + ".log" ;
			header = "Date, Time, x, y, z, voltage, temperature, count\n";
			f = new File(mFileName);
			
			data = new String(header).getBytes();
			if (!f.exists()) {
				try {
					//f.createNewFile();
					out = new FileOutputStream(f);
					out.write(data);
					writePos += header.length();
					out.flush();
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//Update the counter and write to filesystem
			fileNameCounter = fileNameCounter + 1;
			fnCounter = new Integer(fileNameCounter).toString();
			data = fnCounter.getBytes();
			try {
				out = new FileOutputStream(fh,false);
				out.write(data);
				out.flush();
				out.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}
	}
/*	
	public LogObject(String filename) {
		fileName = "sdcard/" + filename;
		String header = "Date, Time, x, y, z, voltage, temperature, count\n";
		f = new File(fileName);
		sdfDate = new SimpleDateFormat(dateFormat);
		sdfTime = new SimpleDateFormat(timeFormat);
		byte[] data = new String(header).getBytes();
		if (!f.exists()) {
			try {
				f.createNewFile();
				out = new FileOutputStream(f);
				out.write(data);
				writePos += header.length();
				out.flush();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
		
	}
*/	
	public int logBattery(int level, int scale, int voltage, int temperature) {
		//get current date and time
		//Date date = new Date();
		Date date = Calendar.getInstance().getTime();
		String dateStr = sdfDate.format(date);
		String timeStr = sdfTime.format(date);
		//String timeStr = date.toString();		
		String levelStr;
		String entry;
		byte[] data;
		
		levelStr = Integer.toString(level) + "/" + Integer.toString(scale);
		
		entry = dateStr + "," + timeStr + "," + levelStr + "," + Integer.toString(voltage) + "," + Integer.toString(temperature)+  "\n";
		data = entry.getBytes();
		
		try {
			out = new FileOutputStream(mFileName,true);
			out.write(data);
			writePos += entry.length();
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			
			return IO_EXCEPTION;
		}
		return SUCCEEDED;
	}
	/*
	public int logANT(ANTData buffer[]) {
		
		String dateStr; 
		String timeStr; 
		String entry;
		int bufferSize = buffer.length;
		Calendar miliSecsCal = Calendar.getInstance();
		long miliSecs;
		byte[] data;
		
		for(int i=0; i < bufferSize; i++) {
			dateStr = sdfDate.format(buffer[i].timeStamp);
			timeStr = sdfTime.format(buffer[i].timeStamp);
			miliSecsCal.setTime(buffer[i].timeStamp);
			miliSecs = miliSecsCal.getTimeInMillis();
			entry = dateStr + "," + timeStr + ","
					+ Short.toString(buffer[i].x) + "," 
					+ Short.toString(buffer[i].y) + "," 
					+ Short.toString(buffer[i].z) + "," 
					+ Short.toString(buffer[i].volt) + "," 
					+ Short.toString(buffer[i].temp) + ","
					+ Short.toString(buffer[i].count) + ","
					+ Long.toString(miliSecs) + "\n";
			
			data = entry.getBytes();
		
			try {
				out = new FileOutputStream(fileName,true);
				out.write(data);
				writePos += entry.length();
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			
				return IO_EXCEPTION;
			}
			
		}
		return SUCCEEDED;
	}
	public int addBreak() {
		String lineBreak = "\n";
		byte[] data = lineBreak.getBytes();
		try {
			out = new FileOutputStream(fileName,true);
			out.write(data);
			writePos += lineBreak.length();
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		
			return IO_EXCEPTION;
		}
		return SUCCEEDED;
	}

	public int getCounter() {
		return fileNameCounter;
	}
	
	private byte[] intToByteArray(int val) {
		return new byte[] {
				(byte)(val >>>24),
				(byte)(val >>>16),
				(byte)(val >>>8),
				(byte)val};
		}
	
	private static final int byteArrayToInt(byte [] val) {
        return (val[0] << 24)
                + ((val[1] & 0xFF) << 16)
                + ((val[2] & 0xFF) << 8)
                + (val[3] & 0xFF);
	}	
	*/
}