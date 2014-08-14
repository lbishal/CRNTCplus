package de.uni_passau.fim.esl.crn_toolbox_center.outputs;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject.NotifyReason;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.GUIModule;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.Module;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.SimpleGraph;
import de.uni_passau.fim.esl.crn_toolbox_center.DataPacket;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject;

public class GraphOutput extends OutputClass {

	public static final String[] mSupportedModules = {"SimpleGraph","AndroidPlot"};
	LinkedList<Receiver> mReceivers = new LinkedList<Receiver>();
	
	
	public GraphOutput(Context context, List<Module> modulesToUse) {
		super(context, modulesToUse);
		Iterator<Module> it = mModules.iterator();
		GUIModule module = null;
		while (it.hasNext()) {
			module = (GUIModule)it.next();
			Receiver receiver = new Receiver(module);
			mReceivers.add(receiver);
			
		}
	}

	@Override
	public void shutDown() {
		Iterator<Receiver> it = mReceivers.iterator();
		Receiver receiver = null;
		while (it.hasNext()) {
			receiver = it.next();
			receiver.stopReceive();
		}
		mReceivers.clear();
		mModules.clear();
		
	}
	
	class Receiver implements Runnable {
		private boolean mRunning = false;
		private transient volatile Thread receiver;
		private GUIModule mModule;
		private float[] oldData = null;
		private long mOldTime;
		
		//private native float[] ReceiveFloat(String name, int nchannels, int nrows, int timeout);
		public Receiver(GUIModule module) {
			
			mModule = module;
		    receiver = new Thread(this);
		    
		    receiver.setDaemon(true);
		    mRunning = true;
		    mOldTime = System.currentTimeMillis();
		    receiver.start();
		}
		public void stopReceive() {
			mRunning = false;
			receiver = null;
		} 
		 
		public void run() {
			while (mRunning) {
				float[] data = ReceiveFloat(mModule.getId(), mModule.getNChannels(), 1, 1);
				
				if (data != null) {
					DataPacket packet = mModule.processDataToDataPacket(data);
					oldData = data;
					long currentTime = System.currentTimeMillis();
					long timeDif = currentTime - mOldTime;
					if (timeDif >= 4) {
						//mHandler.post(new GUISender(packet));
						setChanged();
						notifyObservers(packet);
   	 					mOldTime = currentTime;
					}
   	 				
   	 				
				} else {
					if (oldData != null) {
						DataPacket packet = mModule.processDataToDataPacket(oldData);
						//mHandler.post(new GUISender(packet));
						setChanged();
						notifyObservers(packet);
					}
				}
   	 			
			}
			setChanged();
			notifyObservers(new NotifyObject(NotifyReason.OUTPUT_STOPPED, null ));
		}
			
	}
	class GUISender implements Runnable {
		DataPacket data;
		
		public GUISender(DataPacket packet) {
			data = packet;
		}
		
		public void run() {
			setChanged();
			notifyObservers(data);
			
		}
	}

}
