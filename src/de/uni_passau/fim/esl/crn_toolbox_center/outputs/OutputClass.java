package de.uni_passau.fim.esl.crn_toolbox_center.outputs;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.util.List;
import java.util.Observable;

import de.uni_passau.fim.esl.crn_toolbox_center.DataPacket;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject.NotifyReason;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.Module;

abstract public class OutputClass extends Observable {
	protected List<Module> mModules;
	protected Context mContext;
	
	protected native float[] ReceiveFloat(String name, int nchannels, int nrows, int timeout);
	
	public OutputClass(Context context, List<Module> modulesToUse) {
		mModules = modulesToUse;
		mContext = context;
	}
	
	abstract public void shutDown();
	
	

}
