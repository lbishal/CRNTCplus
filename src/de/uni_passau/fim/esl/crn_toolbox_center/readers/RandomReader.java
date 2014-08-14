package de.uni_passau.fim.esl.crn_toolbox_center.readers;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import android.content.Context;
import de.uni_passau.fim.esl.crn_toolbox_center.ToolboxService.ValuesDirectInputNamePair;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.Module;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.RandomGenerator;

public class RandomReader extends ReaderClass{
	

	public static final String[] mSupportedModules = {"RandomGenerator"};
	private LinkedList<Generator> mGenerators;
	
	
	public RandomReader(Context context, List<Module> modulesToUse) {
		super(context, modulesToUse);
		
		mGenerators = new LinkedList<Generator>();
		Iterator<Module> it = mModules.iterator();
		RandomGenerator module = null;
		while (it.hasNext()) {
			module = (RandomGenerator) it.next();
			Generator generator = new Generator(module);
			mGenerators.add(generator);
		}
	}
	/*
	@Override
	public ModuleClass getModuleById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getSupportedModules() {
		// TODO Auto-generated method stub
		return null;
	}
	 */
	@Override
	public void shutDown() {
		Iterator<Generator> it = mGenerators.iterator();
		Generator generator = null;
		while (it.hasNext()) {
			generator = it.next();
			generator.shutdown();
		}
		Iterator<Module> moduleIterator = mModules.iterator();
		Module module = null;
		while (moduleIterator.hasNext()) {
			module = moduleIterator.next();
			module.setReady(false);
		}
		mGenerators.clear();
		mModules.clear();
		
	}
	
		
	public class Generator implements Runnable {
		private Random mRandomGenerator;
		RandomGenerator mModule;
		private transient volatile Thread sender;
		private boolean mRunning;
		
		public Generator (RandomGenerator module) {
			mModule = module;
			mRandomGenerator = new Random();
			mRunning = true;
			sender = new Thread(this);
			sender.setDaemon(true);
			sender.start();
			
		}
		
		public void run() {
			while (mRunning) {
				float[] values = new float[3];
				values[0] = mRandomGenerator.nextFloat() + mModule.getOffSet();
				values[1] = mRandomGenerator.nextFloat() + mModule.getOffSet();
				values[2] = mRandomGenerator.nextFloat() + mModule.getOffSet();
				ValuesDirectInputNamePair pair = new ValuesDirectInputNamePair(mModule.getId(), values);
				//setChanged();
		        //notifyObservers(inputVal);
				sendData(pair);
		        try {
					Thread.sleep((long) mModule.getPeriodInMs());
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}
			}
			
		}
		
		public void shutdown() {
			mRunning = false;
		}
		
	}

}
