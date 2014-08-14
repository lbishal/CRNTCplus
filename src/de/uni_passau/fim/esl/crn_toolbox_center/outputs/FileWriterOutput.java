package de.uni_passau.fim.esl.crn_toolbox_center.outputs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject.NotifyReason;

import de.uni_passau.fim.esl.crn_toolbox_center.modules.FileWriter;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.Module;


public class FileWriterOutput extends OutputClass{

	private File f;
	private FileOutputStream output;
	private FileInputStream input;
	private FileWriter mFileWriter;
	private Receiver mReceiver;
	
	public static final String[] mSupportedModules = {"FileWriter"};
	
	public FileWriterOutput(Context context, List<Module> modulesToUse) {
		super(context, modulesToUse);
		mReceiver = new Receiver();
		mFileWriter = (FileWriter) mModules.get(0);
		if (mFileWriter.getPathName() != null) {
			f = new File(mFileWriter.getPathName());
			try {
				output = new FileOutputStream(f, mFileWriter.getAppend());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void shutDown() {
		mReceiver.stopReceive();
		
	}
	
	private void writeData(String data) {
		byte[] byteData = data.getBytes();
		//if (f.exists()) {
			try {
				//output = new FileOutputStream(f, mFileWriter.getAppend());
				output.write(byteData);
				output.flush();
				output.close();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//}
	}
	
	class Receiver implements Runnable {
		boolean mRunning = false;
		private transient volatile Thread receiver;
		
		public Receiver() {
			
		    receiver = new Thread(this);
		    receiver.setDaemon(true);
		    mRunning = true;
		    receiver.start();
		}
		public void stopReceive() {
			mRunning = false;
			receiver = null;
		} 
		 
		public void run() {
			while (mRunning) {
				float[] data = ReceiveFloat(mFileWriter.getId(), mFileWriter.getNChannels(), 1, 1);
				if (data != null) {
					writeData(mFileWriter.processDataToString(data));
				}
				
			}
			setChanged();
			notifyObservers(new NotifyObject(NotifyReason.OUTPUT_STOPPED, null ));
		}
			
	}

}
