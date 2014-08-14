package de.uni_passau.fim.esl.crn_toolbox_center.view;

import java.util.Observer;
import com.google.gson.Gson;
import de.uni_passau.fim.esl.crn_toolbox_center.CRNToolboxCenter;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.GUIModule;
import android.app.Activity;
import android.content.Intent;

public abstract class MyTabActivity extends Activity implements Observer{
	//protected String[] mSupportedModules;
	//protected List<ModuleClass> mModules;
	public static final String TAB_MODULE_ID = "tabModuleID";
	protected String mSupportedModule;
	protected String mGSONString;
	protected GUIModule mModule;
	protected int mTabNameID;
	protected int mDrawableID;
	protected String mTabName;
	protected Gson mGson; //Needed for deserialization/serialization
	protected Intent mIntent;
	
	/**
	 * Called when the toolbox is being shutdown.
	 */
	abstract public void shutDown();
	/**
	 * Initializes the TabActivity. Always call this in OnCreate()!
	 * @param gsonClass A class definition of the module that should be supported by the TabActivity
	 * @return An instance of the supported module if the initialization was successful, null otherwise.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected GUIModule initTabActivity(Class gsonClass) {
		
		
		//Standard MyTabActivity initialization 
		CRNToolboxCenter.getInstance().addObserver(this);
	    mIntent = getIntent();
	    mGSONString = mIntent.getStringExtra(TAB_MODULE_ID);
	    if (mGSONString != null && gsonClass != null) {
	    	mGson = new Gson();
	    	mModule = mGson.fromJson(mGSONString, gsonClass);
	    	if (mModule != null) {
	    		return mModule;
	    	} else {
	    		return null;
	    	}
	    } else {
	    	return null;
	    }
	}
	
}
