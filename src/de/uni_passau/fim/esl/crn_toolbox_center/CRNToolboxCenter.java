/*
 * This file is part of the CRN Toolbox Center.
 * Copyright 2010-2011 Jakob Weigert, University of Passau
 */

package de.uni_passau.fim.esl.crn_toolbox_center; 

import java.util.List;
import java.util.Observable;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject.NotifyReason;
import de.uni_passau.fim.esl.crn_toolbox_center.ToolboxService.ValuesDirectOutputNamePair;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.Module;
import de.uni_passau.fim.esl.crn_toolbox_center.view.MainActivity;
import de.uni_passau.fim.esl.crn_toolbox_center.view.TabConfigFile;
import de.uni_passau.fim.esl.crn_toolbox_center.view.TabControlPort;
import de.uni_passau.fim.esl.crn_toolbox_center.view.TabStatus;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject;

/** 
 * The main class of the CRN Toolbox Center model, which performs all actions
 * through the ToolboxService to the Toolbox, holds all current information
 * about the app status and is observed by the view. This class implements the
 * Singleton pattern. It also saves and restores the preferences of this app and
 * holds a reference to the MainActivity to call Android Activity methods.
 * 
 * @author Jakob Weigert <weigert@fim.uni-passau.de> (University of Passau)
 * 
 */
public class CRNToolboxCenter extends Observable {

	public static final int ACTIVITY_REQUEST = 0;
	public static final int SUCCEEDED = 0;
	public static final int SD_READONLY = 48;
	public static final int SD_NOREAD_NOWRITE = 49;
	public static final int INPUT_ERROR = 1;
	public static final int OUTPUT_ERROR = 2;
	public static final int NO_SERVICE_ERROR = 3;
	public static final int READER_ERROR = 4;
	public static final int DATAPACKET = 5;
	public static final int TOAST_MESSAGE = 6;
	
	
    // DirectInput IDs
    // The ToolboxService has the same IDs itself because it may run without the
    // application running.
    public static final String[] DIRECT_INPUT_IDs = { "AccInput", "MagInput", "OriInput",
            "LightInput", "ProxInput", "GPSInput" };
    
    private static final String KEY_SERVICE_CREATE = "serviceCreateReason";
    private static final String VAL_SERVICE_CREATE = "createdWhileAppStart";
    private static final String TOAST_PREFIX = "CRN Toolbox Center: ";
    private static final String ACTLOG_ID = "ActInput";

    // Default settings
    private static final int DEFAULT_OPENED_TAB_ID = 0;
    private static final String DEFAULT_CONFIG_FILE_PATH = "";
    //private static final boolean DEFAULT_AUTO_DIRECT_INPUT = true;
    private static final int DEFAULT_SENSOR_READING_RATE_ID = 0;
    // Needed if auto DirectInput is on and DirectInput tab was never opened:
    private static final String DEFAULT_SENSOR_READING_RATE = "NORMAL";
    private static final int DEFAULT_CONTROL_PORT = 7777;

    // Preferences
    private static final String PREFS_NAME = "Preferences";
    private static final String PREF_KEY_OPEN_TAB = "OpenTab";
    private static final String PREF_KEY_CONFIG_FILE_PATH = "ConfigFilePath";
    private static final String PREF_KEY_SENSOR_READING_RATE = "SensorReadingRate";
    private static final String PREF_KEY_SENSOR_READING_RATE_ID = "SensorReadingRateID";
    private static final String PREF_KEY_CONTROL_PORT = "ControlPort";

    // Members
    private static Configuration mConfigFile = null;
    private static LogObject mLogger;
    private static CRNToolboxCenter mCurrentInstance = null;
    private static Intent mToolboxService;
    private static Activity mMainActivity;
    private static boolean mToolboxRunning = false;
    private static boolean mDirectInputRunning = false;
    private static boolean mOutputRunning = false;
    private static List<Integer> mConfigFileUsedPorts;
    //private static Set<String> mDirectInputIds;
    private static int mConfigErrCode = 0;
    private static int mLogErrCode = 0;

    // Preferences manager
    private static SharedPreferences mAppPreferences;
    private static SharedPreferences.Editor mPreferencesEditor;

    // Settings
    private static int mOpenTabID = DEFAULT_OPENED_TAB_ID;
    private static String mConfigFilePath = DEFAULT_CONFIG_FILE_PATH;
    //private static boolean mAutoDirectInput = DEFAULT_AUTO_DIRECT_INPUT;
    private static String mSensorReadingRate = DEFAULT_SENSOR_READING_RATE;
    private static int mSensorReadingRateID = DEFAULT_SENSOR_READING_RATE_ID;
    private static int mControlPort = DEFAULT_CONTROL_PORT;
    //private static HashMap<String, Boolean> mMapDirectInputActivated;

    // Service connection
    private static ToolboxService mBoundService;
    private static boolean mIsBound;
    
    //Debug
    //private static Debug mDebug;
    //private static Process mProcess;
    
    //Handler to update Views from different threads
    public static Handler mHandler = new Handler() {
    	public void handleMessage(Message msg) {
    		super.handleMessage(msg);
    		switch (msg.what) {
    		case READER_ERROR:
    			stopToolbox();
    			getInstance().setChanged();
    	        getInstance().notifyObservers(new NotifyObject(NotifyReason.TOOLBOX_SERVICE_ERROR, msg.arg1));
    	        break;
    		case DATAPACKET:
    			DataPacket data = (DataPacket) msg.obj;
    			getInstance().setChanged();
    	    	getInstance().notifyObservers(data);
    	    	break;
    		case TOAST_MESSAGE:
    			//TODO Uses resource strings instead
    			String toastMsg = (String) msg.obj;
    			getInstance().setChanged();
    	        getInstance().notifyObservers(new NotifyObject(NotifyReason.TOAST_MESSAGE, toastMsg));
    		}
    		
    	}
    };

    /**
     * Constructs one instance this class, if not yet done.
     * 
     * @param mainActivity
     *            The main activity of the view
     * 
     */
    public CRNToolboxCenter(Activity mainActivity) {
        deleteObservers();
        mMainActivity = mainActivity;
        //TODO
        if (mCurrentInstance == null) {
            mCurrentInstance = CRNToolboxCenter.this;
            startAndBindToolboxService();
            
            //Initialize internal smartphone sensors
            /*
            mMapDirectInputActivated = new HashMap<String, Boolean>();
            for (int i = 0; i < DIRECT_INPUT_IDs.length; i++) {
                String directInputID = DIRECT_INPUT_IDs[i];
                mMapDirectInputActivated.put(directInputID, false);
            }
            */
            // Make saved preferences accessible
            mAppPreferences = mainActivity.getSharedPreferences(PREFS_NAME, 0);
            mPreferencesEditor = mAppPreferences.edit();

            // Restore saved preferences - the second parameters are default
            // values
            mOpenTabID = mAppPreferences.getInt(PREF_KEY_OPEN_TAB, mOpenTabID);
            mConfigFilePath = mAppPreferences.getString(PREF_KEY_CONFIG_FILE_PATH, mConfigFilePath);
            mSensorReadingRateID = mAppPreferences.getInt(PREF_KEY_SENSOR_READING_RATE_ID,
                    mSensorReadingRateID);
            mSensorReadingRate = mAppPreferences.getString(PREF_KEY_SENSOR_READING_RATE,
                    mSensorReadingRate);
            mControlPort = mAppPreferences.getInt(PREF_KEY_CONTROL_PORT, mControlPort);
            
            if (!mConfigFilePath.equals("")) {
            	
            	mConfigFile = new Configuration();
            	mConfigErrCode = readCRNTCConfigFile(mConfigFilePath);
            
            	
            }  
         }
    }
    
    /**
     * Returns the single instance of this class.
     */
    static public CRNToolboxCenter getInstance() {
        return mCurrentInstance;
    }
    
    /** Asks the model class to retrieve the ACTLog config file
     * 
     */
    
    public void requestACTLog() {
    	
        if (mConfigErrCode == Configuration.SUCCEEDED) {
        	
        	Module actLog = mConfigFile.getModuleById(ACTLOG_ID);
        	getInstance().setChanged();
        	getInstance().notifyObservers(new NotifyObject(NotifyReason.ACTLOG_READ, actLog));
        } else {
        	getInstance().setChanged();
        	getInstance().notifyObservers(new NotifyObject(NotifyReason.CONFIG_FILE_ERROR, mConfigErrCode));
        }
    	
    }
    /** Returns all CRNTC+ modules that have been read from the configfile
     * 
     * @return List<ModuleClass>
     */
    public List<Module> requestModules() {
    	if (mConfigFile != null) {
    		return mConfigFile.getModules();
    	} else {
    		return null;
    	}
    }
    
    
    
    /**Reads in the configfile.
    *Returns the error generated by the config object, SUCCEEDED if everything is ok, SD_READONLY if the sdcard is read only
    *and SD_NOREAD_NOWRITE if the sdcard is neither readable or writable.
    */
    private static int readCRNTCConfigFile(String configPlusFilePath) {
    	int errCode = 0;
    	//boolean mExternalStorageAvailable = false;
    	//boolean mExternalStorageWriteable = false;
    	String state = Environment.getExternalStorageState();

    	if (mConfigFile==null) {
    		mConfigFile = new Configuration(); 
    	}
    	if (Environment.MEDIA_MOUNTED.equals(state)) {
    		
    		//mExternalStorageAvailable = mExternalStorageWriteable = true;
    		
    		errCode = mConfigFile.readConfigFile(configPlusFilePath);
    	    	
    	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
    		// We can only read the media
    		//mExternalStorageAvailable = true;
    		//mExternalStorageWriteable = false;
    		errCode = SD_READONLY;
    	} else {
    		// Something else is wrong. It may be one of many other states, but all we need
    		//  to know is we can neither read nor write
    		//mExternalStorageAvailable = mExternalStorageWriteable = false;
    		errCode = SD_NOREAD_NOWRITE;
    	}
    	
    	mConfigErrCode = errCode;
    	
    	if (mConfigErrCode == Configuration.SUCCEEDED) {
        	
    			getInstance().setChanged();
    			getInstance().notifyObservers(new NotifyObject(NotifyReason.CONFIG_FILE_READ, mConfigFile));
    		
        } else {
        	getInstance().setChanged();
        	getInstance().notifyObservers(new NotifyObject(NotifyReason.CONFIG_FILE_ERROR, mConfigErrCode));
        }
    	
    	
    	return errCode;
    }
    /*
    private int initLogFile() {
    	//boolean mExternalStorageAvailable = false;
    	//boolean mExternalStorageWriteable = false;
    	String state = Environment.getExternalStorageState();
    	
    	if (Environment.MEDIA_MOUNTED.equals(state)) {
        	// We can read and write the media
        	//mExternalStorageAvailable = mExternalStorageWriteable = true;
        	//Only create new LogObject if we can read and write from SD-card!
        	mLogger = LogObject.getInstance();
        	mLogErrCode = SUCCEEDED;
    	
    	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
        	// We can only read the media
        	//mExternalStorageAvailable = true;
        	//mExternalStorageWriteable = false;
        	mLogErrCode = SD_READONLY;
        
    	} else {
        	// Something else is wrong. It may be one of many other states, but all we need
        	//  to know is we can neither read nor write
        	//mExternalStorageAvailable = mExternalStorageWriteable = false;
        	mLogErrCode = SD_NOREAD_NOWRITE;
        
    	}
    	return mLogErrCode;
    }
    */
    /** Asks the model class to retrieve or create a LogObject
     * 
     */
    public void requestLogObject() {
    	//mLogErrCode = initLogFile();
    	if(mLogErrCode == LogObject.SUCCEEDED) {
    		getInstance().setChanged();
    		getInstance().notifyObservers(new NotifyObject(NotifyReason.LOG_FILE_READ, mLogger));
    	} else {
    		getInstance().setChanged();
    		getInstance().notifyObservers(new NotifyObject(NotifyReason.LOG_FILE_ERROR, mLogErrCode));
    	}
    	
    }
    
    /*
     * Starts the ToolboxService and creates a connection to it.
     */
    static public void startAndBindToolboxService() {
        if (mMainActivity != null) {

            // Prepare service parameters as URI
            Uri.Builder builder = new Uri.Builder();
            // Dummy argument needed to know that service was started by the
            // app,
            // not restarted by the system
            builder.appendQueryParameter(KEY_SERVICE_CREATE, VAL_SERVICE_CREATE);
            // Start service
            mToolboxService = new Intent(null, builder.build(), mMainActivity, ToolboxService.class);
            mMainActivity.startService(mToolboxService);
            // Bind service - it may take time until boundService is available!
            doBindService();
        }
    }

    /**
     * When the ToolboxService is connected, get the Toolbox info and notify the
     * view.
     */
    static public void onServiceConnected() {
        if (mBoundService != null) {
            String toolboxInfo = mBoundService.getToolboxLibraryInfo();
            getInstance().setChanged();
            getInstance().notifyObservers(new NotifyObject(NotifyReason.SET_TOOLBOX_INFO, toolboxInfo));
        }
    }

    /**
     * Starts the Toolbox with a configuration file.
     * 
     * @param configFilePath
     *            The configuration file path
     */
    public static void startToolboxWithConfigFile(String configFilePath) {
        if (mBoundService != null) {
           
        	//Debug.startMethodTracing();
        	//try {
        		//mProcess = Runtime.getRuntime().exec("logcat -v time -f /sdcard/logcat");
        		
			//} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			//}
        	
        	String tmpConfigFile = mConfigFile.getTempConfString();
        	
        	recognizeConfigFileParams();
        	//mBoundService.startToolboxWithConfigFile(tmpConfigFile, mConfigFileUsedPorts, mConfigFile.getModules());
        	mBoundService.startToolboxWithConfigFile(tmpConfigFile, mConfigFileUsedPorts, mConfigFile.getDirectInputs(), mConfigFile.getDirectOutputs());
            mToolboxRunning = true;
            getInstance().setChanged();
            getInstance().notifyObservers(new NotifyObject(NotifyReason.TOOLBOX_RUNNING, mToolboxRunning));
            getInstance().setChanged();
            getInstance()
                    .notifyObservers(new NotifyObject(NotifyReason.TOOLBOX_START_STOP,
                                    mToolboxRunning));

            
            //mBoundService.registerInputsOutputs(mConfigFile.getModules());
        	
            startDirectInput();
            startOutput();
            
        }
    }

    /**
     * Starts the Toolbox with control port.
     * 
     * @param controlPort
     *            The control port.
     */
    public static void startToolboxWithControlPort(int controlPort) {
        if (mBoundService != null) {
            mBoundService.startToolboxWithControlPort(controlPort);
            mToolboxRunning = true;
            getInstance().setChanged();
            getInstance().notifyObservers(new NotifyObject(NotifyReason.TOOLBOX_RUNNING, mToolboxRunning));
            getInstance().setChanged();
            getInstance().notifyObservers(new NotifyObject(NotifyReason.TOOLBOX_START_STOP,mToolboxRunning));

        }
    }

    /**
     * Stops the running Toolbox.
     */
    public static void stopToolbox() {
        
    	stopOutput();   
    	
    	stopDirectInput();
    	
        //mProcess.destroy();
        if (mBoundService != null) {
            mBoundService.stopToolbox();
            mToolboxRunning = false;
            getInstance().setChanged();
            getInstance().notifyObservers(new NotifyObject(NotifyReason.TOOLBOX_RUNNING, mToolboxRunning));
            getInstance().setChanged();
            getInstance()
                    .notifyObservers(new NotifyObject(NotifyReason.TOOLBOX_START_STOP,
                                    mToolboxRunning));
        }
        //Debug.stopMethodTracing();
    }

    /**
     * Starts DirectInput to pass sensor data to the Toolbox.
     * 
     * 
     */
    public static int startDirectInput() {
        int return_code = SUCCEEDED;
    	
    	if (mBoundService != null) {
            if (mDirectInputRunning) {
                stopDirectInput();
            }
            //mBoundService.startDirectInput(sensorsToUse/*, sensorRate*/);
            return_code = mBoundService.startDirectInput(mConfigFile.getModules());
            if (return_code == SUCCEEDED) {
	            mDirectInputRunning = true;
	            getInstance().setChanged();
	            getInstance().notifyObservers(new NotifyObject(NotifyReason.DIRECT_INPUT_RUNNING,
	                            mDirectInputRunning));
	            getInstance().setChanged();
	            getInstance().notifyObservers(new NotifyObject(NotifyReason.DIRECT_INPUT_START_STOP,
	                            mDirectInputRunning));
	            return return_code;
            }
        } else {
        	return_code = NO_SERVICE_ERROR;
        	
        }
    	return return_code;
    }
    
    /**
     * Starts Output to receive data from the Toolbox.
     * 
     * 
     */
    public static void startOutput() {
        if (mBoundService != null) {
            if (mOutputRunning) {
                stopOutput();
            }
            //mBoundService.startDirectInput(sensorsToUse/*, sensorRate*/);
            mBoundService.startDirectOutput(mConfigFile.getModules());
            mOutputRunning = true;
            getInstance().setChanged();
            getInstance().notifyObservers(new NotifyObject(NotifyReason.OUTPUT_RUNNING,
                            mOutputRunning));
            getInstance().setChanged();
            getInstance().notifyObservers(new NotifyObject(NotifyReason.OUTPUT_START_STOP,
                    		mOutputRunning));
        }
    }

    /**
     * Stops DirectInput.
     */
    public static void stopDirectInput() {
        if (mBoundService != null && mDirectInputRunning) {
            mBoundService.stopDirectInput();
            mDirectInputRunning = false;
            getInstance().setChanged();
            getInstance().notifyObservers(new NotifyObject(NotifyReason.DIRECT_INPUT_RUNNING,
                            mDirectInputRunning));
            getInstance().setChanged();
            getInstance().notifyObservers(new NotifyObject(NotifyReason.DIRECT_INPUT_START_STOP,
                            mDirectInputRunning));
        }
    }
    
    /**
     * Stops Output.
     */
    public static void stopOutput() {
        if (mBoundService != null && mOutputRunning) {
            mBoundService.stopDirectOutput();
            mOutputRunning = false;
            getInstance().setChanged();
            getInstance().notifyObservers(new NotifyObject(NotifyReason.OUTPUT_RUNNING,
                    		mOutputRunning));
            getInstance().setChanged();
            getInstance().notifyObservers(new NotifyObject(NotifyReason.OUTPUT_START_STOP,
                    		mOutputRunning));
        }
    }

    /**
     * Called when the application is ending.
     */
    public static void onDestroy() {
        doUnbindService();
        getInstance().notifyObservers(new NotifyObject(NotifyReason.ACTIVITY_FINISH, null));
    }

    /**
     * Requests the current log from the Toolbox. The native library then calls
     * receivedToolboxLog().
     */
    public static void getToolboxLog() {
        if (mBoundService != null) {
            mBoundService.requestToolboxLog();
        }
    }

    /**
     * Called by the native library with the current log.
     * 
     * @param logText
     *            The current log messages.
     */
    public static void receivedToolboxLog(String logText) {
        getInstance().setChanged();
        getInstance().notifyObservers(new NotifyObject(NotifyReason.SHOW_TOOLBOX_LOG, logText));
    }

    public static void receivedDirectOutputValuePair(ValuesDirectOutputNamePair pair) {
    	getInstance().setChanged();
    	getInstance().notifyObservers(pair);
    }
    public static void receivedDataPacket(DataPacket data) {
    	
    	mHandler.removeMessages(DATAPACKET);
    	Message msg = Message.obtain();
    	msg.obj = data;
    	msg.what = DATAPACKET;
    	mHandler.sendMessage(msg);
    	
    	//getInstance().setChanged();
    	//getInstance().notifyObservers(data);
    }
    
    public static void receivedError(int errCode) {
    	//stopToolbox();
    	/*
    	getInstance().setChanged();
        getInstance().notifyObservers(
                getInstance().new NotifyObject(NotifyReason.TOOLBOX_SERVICE_ERROR, errCode));
                */
    	Message msg = Message.obtain();
    	msg.arg1 = errCode;
    	msg.what = READER_ERROR;
    	mHandler.sendMessage(msg);
    	stopToolbox();
    }
    
    public static void receivedToastMessage(String message) {
    	
    	Message msg = Message.obtain();
    	msg.obj = message;
    	msg.what = TOAST_MESSAGE;
    	mHandler.sendMessage(msg);
    	
    }

    /**
     * Called by the view to set a user input to save:
     * 
     * @param tabID
     */
    public static void setOpenedTabID(int tabID) {
        mOpenTabID = tabID;
        mPreferencesEditor.putInt(PREF_KEY_OPEN_TAB, mOpenTabID);
        mPreferencesEditor.commit();
    }

    /**
     * Called by the view to set a user input to save:
     * 
     * @param configFilePath
     */
    public static void setConfigFilePath(String configFilePath) {
    	
    	//mConfigFile = null;
    	mConfigErrCode = readCRNTCConfigFile(configFilePath);
        
        getInstance().setChanged();
        mConfigFilePath = configFilePath;
        
        //recognizeConfigFileParams();
          
        if (mConfigErrCode == Configuration.SUCCEEDED) {
        	getInstance().notifyObservers(new NotifyObject(NotifyReason.SET_CONFIG_FILE_PATH, configFilePath));
        	mPreferencesEditor.putString(PREF_KEY_CONFIG_FILE_PATH, configFilePath);
        	mPreferencesEditor.commit();
        }
        else {
        	getInstance().notifyObservers(new NotifyObject(NotifyReason.CONFIG_FILE_ERROR, mConfigErrCode));
            	
        }
        
    }

    /**
     * Called by the view to set a user input to save:
     * 
     * @param autoDirectInput
     */
    /*
    public static void setAutoDirectInput(boolean autoDirectInput) {
        mAutoDirectInput = autoDirectInput;
        mPreferencesEditor.putBoolean(PREF_KEY_AUTO_DIRECT_INPUT, mAutoDirectInput);
        mPreferencesEditor.commit();
    }
    */
    /**
     * Called by the view to set a user input to save:
     * 
     * @param selectionID
     * @param sensorReadingRate
     */
    public static void setSensorReadingRate(int selectionID, String sensorReadingRate) {
        mSensorReadingRateID = selectionID;
        mSensorReadingRate = sensorReadingRate;
        mPreferencesEditor.putInt(PREF_KEY_SENSOR_READING_RATE_ID, mSensorReadingRateID);
        mPreferencesEditor.putString(PREF_KEY_SENSOR_READING_RATE, mSensorReadingRate);
        mPreferencesEditor.commit();
    }

    /**
     * Called by the view to set a user input to save:
     * 
     * @param controlPort
     */
    public static void setControlPort(int controlPort) {
        mControlPort = controlPort;
        mPreferencesEditor.putInt(PREF_KEY_CONTROL_PORT, mControlPort);
        mPreferencesEditor.commit();
    }

    /**
     * Called by the view to set a user input to save:
     * 
     * @param observer
     */
    public void addObserver(MainActivity observer) {
        super.addObserver(observer);
        setChanged();
		notifyObservers(new NotifyObject(NotifyReason.CONFIG_FILE_READ, mConfigFile));
        setChanged();
        notifyObservers(new NotifyObject(NotifyReason.SET_TAB, mOpenTabID));
        
    }

    /**
     * Called by the view to set a user input to save:
     * 
     * @param observer
     */
    public void addObserver(TabStatus observer) {
        super.addObserver(observer);
        setChanged();
        notifyObservers(new NotifyObject(NotifyReason.TOOLBOX_RUNNING, mToolboxRunning));
        setChanged();
        notifyObservers(new NotifyObject(NotifyReason.DIRECT_INPUT_RUNNING, mDirectInputRunning));
    }

    /**
     * Called by the view to set a user input to save:
     * 
     * @param observer
     */
    public void addObserver(TabConfigFile observer) {
        super.addObserver(observer);
        setChanged();
        notifyObservers(new NotifyObject(NotifyReason.SET_CONFIG_FILE_PATH, mConfigFilePath));
        //setChanged();
        //notifyObservers(new NotifyObject(NotifyReason.SET_AUTO_DIRECT_INPUT, mAutoDirectInput));
        setChanged();
        notifyObservers(new NotifyObject(NotifyReason.TOOLBOX_RUNNING, mToolboxRunning));
    }

    /**
     * Called by the view to set a user input to save:
     * 
     * @param observer
     */
    public void addObserver(TabControlPort observer) {
        super.addObserver(observer);
        setChanged();
        notifyObservers(new NotifyObject(NotifyReason.SET_CONTROL_PORT, mControlPort));
        setChanged();
        notifyObservers(new NotifyObject(NotifyReason.TOOLBOX_RUNNING, mToolboxRunning));
    }

    /**
     * Kills the whole process, including Activities, Service and native library
     * thread.
     */
    public static void killProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    
    
    private static void recognizeConfigFileParams() {
        if (mConfigFilePath != null) {
        	
        	if (mConfigFile != null) {
        		mConfigFileUsedPorts = mConfigFile.PortsToUse();
        		//mMapDirectInputActivated = mConfigFile.ModulesToUse();
        	}
           
        }
        
    }
    
    private static ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((ToolboxService.ToolboxBinder) service).getService();
            /*
             * Toast.makeText(mMainActivity, TOAST_PREFIX +
             * "Connected to Toolbox service", Toast.LENGTH_SHORT).show();
             */
            CRNToolboxCenter.onServiceConnected();
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
            Toast.makeText(mMainActivity, TOAST_PREFIX + "Lost connection to Toolbox service",
                    Toast.LENGTH_LONG).show();
        }

    };

    private static synchronized void doBindService() {
        if (!mIsBound) {
            mMainActivity.bindService(new Intent(mMainActivity, ToolboxService.class), mConnection,
                    Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
    }

    private static void doUnbindService() {
        if (mIsBound) {
            mMainActivity.unbindService(mConnection);
            mIsBound = false;
        }
    }
    /**
     * Transmits data to the ToolboxService to be send to the toolbox. This function is called by GUI modules.
     * @param values array of floats containg the data.
     * @param inputID String containing the Id of the GUI module, should coincide with an id from a direct input.
     */
    public void requestDataTransmit(float values[], String inputID) {
    	mBoundService.transmitData(values, inputID);
    }
    
    /**
     * Called by an activity when a module has been updated by the user and needs to be stored on the sdcard
     * @param module that has been changed
     * @return 0 on success.  
     */
    public int requestConfigFileWrite(Module module) {
    	
    	mConfigFile.updateModule(module, mConfigFilePath);
    	return Configuration.SUCCEEDED;
    }

}