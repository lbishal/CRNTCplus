package de.uni_passau.fim.esl.crn_toolbox_center.outputs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject.NotifyReason;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.FileWriter;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.FileWriterMean;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.Module;
import de.uni_passau.fim.esl.crn_toolbox_center.outputs.FileWriterOutput.Receiver;

public class FileWriterOutputMean extends OutputClass{

	private File f;
	private FileOutputStream output;
	private FileInputStream input;
	private FileWriterMean mFileWriterMean;
	private int mWindowSize;
	private Receiver mReceiver;
	
	
	public static final String[] mSupportedModules = {"FileWriterMean"};
	
	public FileWriterOutputMean(Context context, List<Module> modulesToUse) {
		super(context, modulesToUse);
	
		mFileWriterMean = (FileWriterMean) mModules.get(0);
		mReceiver = new Receiver();
		
		mWindowSize = mFileWriterMean.getWindowSize();
		if (mFileWriterMean.getPathName() != null) {
			f = new File(mFileWriterMean.getPathName());
			try {
				output = new FileOutputStream(f, mFileWriterMean.getAppend());
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
				output = new FileOutputStream(f, true);
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
		private List<float[]> mBuffer = new LinkedList<float[]>();
		Iterator<float[]> bufferIterator = mBuffer.iterator();
		float[] meanVal = new float[mFileWriterMean.getNChannels()];
		int counter = 0;
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
				float[] data = ReceiveFloat(mFileWriterMean.getId(), mFileWriterMean.getNChannels(), 1, 1);
				
				if (data != null) {
					//mBuffer.add(data);
					//if (mBuffer.size() == mWindowSize) {
					for (int i = 0; i < mFileWriterMean.getNChannels(); i++) {
						meanVal[i] += data[i];
					}
					counter++;
					if (counter == mWindowSize) {
						for (int i = 0; i < mFileWriterMean.getNChannels(); i++) {
							meanVal[i] /= mFileWriterMean.getWindowSize();
						}
						writeData(mFileWriterMean.processDataToString(meanVal));
						meanVal = new float[mFileWriterMean.getNChannels()];
						counter = 0;
						/*
						while (bufferIterator.hasNext()) {
							float[] value = bufferIterator.next();
							for (int i = 0; i < mFileWriterMean.getNChannels(); i++) {
								meanVal[i] += value[i];
							}
						}
						*/
						//mBuffer.clear();
					}
					//writeData(mFileWriterMean.processDataToString(meanVal));
				}
				
			}
			setChanged();
			notifyObservers(new NotifyObject(NotifyReason.OUTPUT_STOPPED, null ));
		}
			
	}

	

}
