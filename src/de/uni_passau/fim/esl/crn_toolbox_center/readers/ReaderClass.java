package de.uni_passau.fim.esl.crn_toolbox_center.readers;

import java.util.Iterator;
import java.util.List;
import java.util.Observable;

import android.content.Context;
import android.util.Log;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject.NotifyReason;
import de.uni_passau.fim.esl.crn_toolbox_center.ToolboxService.ValuesDirectInputNamePair;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.Module;

abstract public class ReaderClass extends Observable {
	
	protected String[] mSupportedModules;
	protected List<Module> mModules;
	protected Context mContext;
	protected boolean allModulesReady = false; //Used to synchronize all inputs.
	private int mReadyCount = 0;
	
	public ReaderClass(Context context, List<Module> modulesToUse) {
		mModules = modulesToUse;
		mContext = context;
		
	}
	
	abstract public void shutDown();
	
	protected void sendData(ValuesDirectInputNamePair pair) {
		if(!allModulesReady) {
			Iterator<Module> it = mModules.iterator();
			Module module = null;
			while(it.hasNext()) {
				module = it.next();
				if (module.getId().equals(pair.getDirectInputName()) && !module.isReady()) {
					//mModules.remove(module);
					module.setReady(true);
					Log.d("Module ready",module.getId());
					mReadyCount++;
					if (mReadyCount == mModules.size()) {
						allModulesReady = true;
						setChanged();
						notifyObservers(new NotifyObject(NotifyReason.READER_READY, allModulesReady));
					}
					//mModules.add(module);
				}
			}
		} else {
			setChanged();
			notifyObservers(pair);
		}
	}
	
	public synchronized boolean isReady() {
		return allModulesReady;
	}
	
}
