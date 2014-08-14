/*
 * This file is part of the CRN Toolbox Center.
 * Copyright 2010-2011 Jakob Weigert, University of Passau
 */

package de.uni_passau.fim.esl.crn_toolbox_center;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;
import de.uni_passau.fim.esl.crn_toolbox_center.SocketCleaner.SocketType;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.Module;
import de.uni_passau.fim.esl.crn_toolbox_center.outputs.FileWriterOutput;
import de.uni_passau.fim.esl.crn_toolbox_center.outputs.FileWriterOutputMean;
import de.uni_passau.fim.esl.crn_toolbox_center.outputs.FileWriterOutputSFTP;
import de.uni_passau.fim.esl.crn_toolbox_center.outputs.OutputClass;
import de.uni_passau.fim.esl.crn_toolbox_center.outputs.GraphOutput;
import de.uni_passau.fim.esl.crn_toolbox_center.readers.ANTReader;
import de.uni_passau.fim.esl.crn_toolbox_center.readers.BluetoothReader;
import de.uni_passau.fim.esl.crn_toolbox_center.readers.CRNTReaderTCP;
import de.uni_passau.fim.esl.crn_toolbox_center.readers.LocationReader;
import de.uni_passau.fim.esl.crn_toolbox_center.readers.MicReader;
import de.uni_passau.fim.esl.crn_toolbox_center.readers.RandomReader;
import de.uni_passau.fim.esl.crn_toolbox_center.readers.ReaderClass;
import de.uni_passau.fim.esl.crn_toolbox_center.readers.SensorReader;
import de.uni_passau.fim.esl.crn_toolbox_center.readers.SystemStatusReader;
import de.uni_passau.fim.esl.crn_toolbox_center.view.MainActivity;
//import android.R;

/**
 * The Service which performs all actions regarding the Toolbox. Therefore, the
 * native library is referenced from this Service and the native methods are
 * declared here.
 * 
 * Services on Android are no separate threads but belong to the application
 * thread. When the Toolbox should be started, this Service starts a thread to
 * run the Toolbox in.
 * 
 * @author Jakob Weigert <weigert@fim.uni-passau.de> (University of Passau)
 * 
 */
public class ToolboxService extends Service implements Observer {

    // File name: "libcrnt.so" => library name: "crnt"
    private static final String LIBRARY_NAME = "crnt";

    // Load the native library
    static {
        System.loadLibrary(LIBRARY_NAME);
    }
    // Error codes range between 100 > ...
    private final static int SUCCEEDED = 0;
    private final static int INPUT_ERROR = 100;
    private final static int OUTPUT_ERROR = 102;
    private final static int NO_MODULES = 103;
    
    public static final int ANT_FAILURE = 104;
    public static final int ANT_CANT_FIND_DEVICE = 105;
    public static final int ANT_NOT_SUPPORTED = 106;
    public static final int ANT_SERVICE_NOT_FOUND = 107;
    public static final int BLUETOOTH_NO_PAIRED_DEVICE = 108;
    public static final int BLUETOOTH_CANT_CONNECT = 109;
    public static final int BLUETOOTH_NO_BLUETOOTH_SUPPORT = 110;
    public static final int BLUETOOTH_NOT_ENABLED = 111;
  
    
    private native String toolboxCommand(int commandCode);
    private native String toolboxCommand(int commandCode, float[] floatParam);
    private native String toolboxCommand(int commandCode, float[] floatParam, String stringParam);
    private native String toolboxCommand(int commandCode, float[] floatParam, String stringParam,
            int intParam);

    // Command code definitions
    private static final int COMMAND_CODE_RETURN_VERSION = 000;
    private static final int COMMAND_CODE_STOP_TOOLBOX = 001;
    private static final int COMMAND_CODE_START_CONFIGFILE = 002;
    private static final int COMMAND_CODE_START_CONTROLPORT = 006;
    private static final int COMMAND_CODE_SEND_FLOAT_DIRECTINPUT = 011;
    private static final int COMMAND_CODE_RECEIVE_FLOAT_DIRECTOUTPUT = 010;
    private static final int COMMAND_CODE_GET_LOG = 021;
    private static final int COMMAND_CODE_REGISTER_DOW_CALLBACK = 007;
    private static final int COMMAND_CODE_REGISTER_DIR_IDS = 012;
    private static final int COMMAND_CODE_REGISTER_DOW_IDS = 013;

    private static final String KEY_SERVICE_CREATE = "serviceCreateReason";

    // Messages
    private static final String TOAST_PREFIX = "Toolbox Service: ";
    private static final String NOTIFICATION_TITLE = "CRN Toolbox Center";
    private static final String TICKER_TEXT = "CRN Toolbox started";
    private static final int NOTIFICATION_ICON = R.drawable.icon_esl;
    private static final String NOTIFICATION_TEXT = "Toolbox is running. Touch for options...";
    private static final String ERROR_NO_TOOLBOX_INFO = "Error: Unable to retreive Toolbox info.";

    // Timeouts
    static final public int WAIT_FOR_TOOLBOX_TO_END = 1500; // milliseconds

    // Members
    private boolean mThisServiceIsStarted = false;
    private final IBinder mToolboxServiceBinder = new ToolboxBinder();
    private ToolboxThread mToolboxThread; // Thread running the Toolbox
    private int mControlPortUsed = 0;
    private List<Integer> mConfigFileUsedPorts;
    private boolean mReadersReady = false;
    private int mReadersReadyCount = 0;
    private boolean mOutputsStopped = true;
    private int mStoppedOutputsCount = 0;

    // Members - DirectInput
    
    private boolean mToolboxIsRunning;
    
    private List<ReaderClass> mReaders = new LinkedList<ReaderClass>();
    private List<OutputClass> mOutputs = new LinkedList<OutputClass>();
    @SuppressWarnings("rawtypes")
	private List<Class> mReaderClasses = new LinkedList<Class>();
    @SuppressWarnings("rawtypes")
	private List<Class> mOutputClasses = new LinkedList<Class>();
    //private static final Map<String, Class> mMapReaderClasses = new HashMap<String, Class>();

    /**
     * Called when this service is created
     */
    @Override
    public void onCreate() {
        super.onCreate();
        
        mReaderClasses.add(SensorReader.class);
        mReaderClasses.add(ANTReader.class);
        mReaderClasses.add(LocationReader.class);
        mReaderClasses.add(CRNTReaderTCP.class);
        mReaderClasses.add(SystemStatusReader.class);
        mReaderClasses.add(RandomReader.class);
        mReaderClasses.add(BluetoothReader.class);
        mReaderClasses.add(MicReader.class);    
        
        mOutputClasses.add(FileWriterOutput.class);
        mOutputClasses.add(GraphOutput.class);
        mOutputClasses.add(FileWriterOutputSFTP.class);
        mOutputClasses.add(FileWriterOutputMean.class);
        
        
       
        
    }

    /**
     * Called when this service is started by an application (or restarted by
     * the system).
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        /*
         * Kind of workaround because of how Android handles crashed services:
         * if service is restarted by the system without arguments (after
         * crashing), stop itself
         */

        if (intent == null || intent.getData() == null
                || intent.getData().getQueryParameter(KEY_SERVICE_CREATE) == null
                || intent.getData().getQueryParameter(KEY_SERVICE_CREATE) == "") {
            // Toast.makeText(this, TOAST_PREFIX + "Started without arguments", Toast.LENGTH_SHORT).show();
            this.stopForeground(true);
            this.stopSelf();
            return START_NOT_STICKY;
        }

        if (mThisServiceIsStarted) {
            Toast.makeText(this, TOAST_PREFIX + "Running already", Toast.LENGTH_SHORT).show();
        }

        mThisServiceIsStarted = true;

        /*
         * If the service crashes, let it start once again to stop itself
         */
        //return START_STICKY;
        return START_NOT_STICKY;
    }

    /**
     * Called when a connection to this service is established.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mToolboxServiceBinder;
    }

    /**
     * Called before this service is destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        // Toast.makeText(this, TOAST_PREFIX + "Destroying...", Toast.LENGTH_SHORT).show();

        stopDirectInput();

        if (mToolboxIsRunning) {
            stopToolbox();
        }
    }

    /**
     * Puts this service in a foreground state (display info to the user).
     */
    private void serviceForegroundStart() {

        // make notification and start service in foreground
        int id = 315;
        long when = System.currentTimeMillis();
        Notification notification = new Notification(NOTIFICATION_ICON, TICKER_TEXT, when);
        Context context = getApplicationContext();
        CharSequence contentTitle = NOTIFICATION_TITLE;
        CharSequence contentText = NOTIFICATION_TEXT;
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

        this.startForeground(id, notification);
    }

    /**
     * Returns the Toolbox native library information (version...) string.
     * 
     * @return The information string
     */
    public String getToolboxLibraryInfo() {
        String toolboxInfoString = toolboxCommand(COMMAND_CODE_RETURN_VERSION);
        
        if (toolboxInfoString == null) {
            toolboxInfoString = ERROR_NO_TOOLBOX_INFO;
        }
        return toolboxInfoString;
    }

    /**
     * Starts a new Toolbox thread with the given configuration file path. Stops
     * any existing Toolbox thread first.
     * 
     * @param configFilePath
     *            The configuration file path
     */
    //public void startToolboxWithConfigFile(String configFilePath, List<Integer> usedPorts, List<ModuleClass> modules) {
    public void startToolboxWithConfigFile(String configFilePath, List<Integer> usedPorts, LinkedList<String> inputs, LinkedList<String> outputs) {
        // Stop old Toolbox first if existent
        if (mToolboxIsRunning) {
            Toast.makeText(this, TOAST_PREFIX + "Stopping old Toolbox", Toast.LENGTH_SHORT).show();
            stopToolbox();
        }

        serviceForegroundStart();

        Toast.makeText(this, TOAST_PREFIX + "Starting Toolbox with " + configFilePath,
                Toast.LENGTH_SHORT).show();

        int commandCode = COMMAND_CODE_START_CONFIGFILE;
        String stringParam = configFilePath;
        int intParam = 0;

        mConfigFileUsedPorts = usedPorts;
        //mModules = modules;
        //mToolboxThread = new ToolboxThread(commandCode, stringParam, intParam);
        mToolboxThread = new ToolboxThread(commandCode, stringParam, intParam, inputs, outputs);
        mToolboxThread.start();
        mToolboxIsRunning = true;

    }

    /**
     * Starts a new Toolbox thread with the given control port. Stops any
     * existing Toolbox thread first.
     * 
     * @param controlPort
     *            The control port
     */
    public void startToolboxWithControlPort(int controlPort) {

        // Stop old thread first if existent
        if (mToolboxIsRunning) {
            Toast.makeText(this, TOAST_PREFIX + "Stopping old Toolbox", Toast.LENGTH_SHORT).show();
            stopToolbox();
        }

        serviceForegroundStart();

        Toast.makeText(this, TOAST_PREFIX + "Starting Toolbox with control port " + controlPort,
                Toast.LENGTH_SHORT).show();

        int commandCode = COMMAND_CODE_START_CONTROLPORT;
        String stringParam = "";
        int intParam = controlPort;

        mToolboxThread = new ToolboxThread(commandCode, stringParam, intParam);
        mToolboxThread.start();
        mControlPortUsed = controlPort;
        mToolboxIsRunning = true;

    }

    /**
     * Stops the running Toolbox, if any.
     */
    public void stopToolbox() {

        if (mToolboxIsRunning) {

        	//toolboxCommand(COMMAND_CODE_RECEIVE_FLOAT_DIRECTOUTPUT, null, "ETHOSOutput", 10);
            ToolboxThread toolboxStopThread = new ToolboxThread(COMMAND_CODE_STOP_TOOLBOX);
            toolboxStopThread.start();
        	//toolboxCommand(COMMAND_CODE_STOP_TOOLBOX);
            try {
                Thread.sleep(WAIT_FOR_TOOLBOX_TO_END);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            
            mToolboxThread = null;
            toolboxStopThread = null;

            this.stopForeground(true);

            mToolboxIsRunning = false;

            if (mControlPortUsed != 0) {
                // Start up socket cleaner to surely close the used socket for
                // control port
                SocketCleaner socketCleaner = new SocketCleaner(SocketType.TCP_CLIENT,
                        mControlPortUsed);
                socketCleaner.start();
                mControlPortUsed = 0;
            }

            if (mConfigFileUsedPorts != null) {
                for (int port : mConfigFileUsedPorts) {
                    // Start up socket cleaner to surely close the used ports
                    // created by the config file
                    
                	SocketCleaner socketCleaner = new SocketCleaner(SocketType.TCP_CLIENT, port);
                    socketCleaner.start();
                    mControlPortUsed = 0;
                }
            }
        }

    }

     /**
     * Starts DirectInput, which delivers sensor data to the Toolbox. Creates all the needed ReaderClasses 
     * and adds the appropriate modules to them.
     * 
     * @param modulesToUse
     *            The modules of the sensors to use as ModuleClass.
     * 
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public int startDirectInput(List<Module> modulesToUse) {
    	
    	Iterator<Class> readerIterator = mReaderClasses.iterator();
    	Context context = getApplicationContext();
    	Class<ReaderClass> reader = null;
    	Class[] paramTypes = {Context.class, List.class};
    	//String moduleStr;
    	Constructor<ReaderClass> readerConstructor;
    	Field supportedModules;
    	ReaderClass ReaderObject = null;
    	boolean moduleFound = false;
    	int return_code = SUCCEEDED;
    	
    	stopDirectInput();
    	//Smartphone sensors block
        if (modulesToUse != null ) {
        	mReadersReadyCount = 0;
        	while (readerIterator.hasNext()) {
        		reader = readerIterator.next();
        		try {
        			List<Module> modules = new ArrayList<Module>();
					supportedModules = reader.getField("mSupportedModules");
					
					String[] moduleStr = (String[]) supportedModules.get(null);
					Iterator<Module> moduleIterator = modulesToUse.iterator();
					while (moduleIterator.hasNext()) {
						
				        Module module = moduleIterator.next();
	        			for(String m : moduleStr )
	        			if (m.equals(module.getType())) {
	        				modules.add(module);
	        				moduleFound = true;
	        			}
	        		}
					if (moduleFound) {
						readerConstructor = reader.getConstructor(paramTypes);
						Object[] paramValues = {context, modules};
						ReaderObject = (ReaderClass) readerConstructor.newInstance(paramValues);
						ReaderObject.addObserver(this);
						mReaders.add(ReaderObject);
						
					}
					//modules.clear();
					moduleFound = false;
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					
					e.printStackTrace();
					return INPUT_ERROR;
				}
        		
        	}
        	//mReadersReadyThread = new ReadersReady();
        } else {
        	return_code = NO_MODULES;
        }
    	return return_code;
        
            
               
    }

    /**
     * Starts Output, which receives data from the Toolbox.
     * 
     * @param modulesToUse
     *            output modules
     * 
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public int startDirectOutput(List<Module> modulesToUse) {
    	Iterator<Class> outputIterator = mOutputClasses.iterator();
    	Context context = getApplicationContext();
    	Class<OutputClass> output = null;
    	Class[] paramTypes = {Context.class, List.class};
    	//String moduleStr;
    	Constructor<OutputClass> outputConstructor;
    	Field supportedModules;
    	OutputClass OutputObject = null;
    	boolean moduleFound = false;
    	int return_code = SUCCEEDED;
    	
    	stopDirectOutput();
    	//Smartphone sensors block
        if (modulesToUse != null ) {
        	while (outputIterator.hasNext()) {
        		output = outputIterator.next();
        		try {
        			List<Module> modules = new ArrayList<Module>();
					supportedModules = output.getField("mSupportedModules");
					
					String[] moduleStr = (String[]) supportedModules.get(null);
					Iterator<Module> moduleIterator = modulesToUse.iterator();
					while (moduleIterator.hasNext()) {
						
				        Module module = moduleIterator.next();
	        			for(String m : moduleStr )
	        			if (m.equals(module.getType())) {
	        				modules.add(module);
	        				moduleFound = true;
	        			}
	        		}
					if (moduleFound) {
						outputConstructor = output.getConstructor(paramTypes);
						Object[] paramValues = {context, modules};
						OutputObject = (OutputClass) outputConstructor.newInstance(paramValues);
						OutputObject.addObserver(this);
						mOutputs.add(OutputObject);
						mOutputsStopped = false;
						
					}
					//modules.clear();
					moduleFound = false;
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					
					e.printStackTrace();
					return INPUT_ERROR;
				}
        		
        	}
        } else {
        	return_code = NO_MODULES;
        }
    	return return_code;
    }
    
    /**
     * Stops DirectInput
     */
    public void stopDirectInput() {

    	Iterator<ReaderClass> readerIterator = mReaders.iterator();
    	ReaderClass reader = null;
    	while (readerIterator.hasNext()) {
    		reader = readerIterator.next();
    		reader.shutDown();
    		reader.deleteObservers();
    	}
    	
    	mReaders.clear();
    	mReadersReady = false;
    }
    
    /**
     * Stops Output
     */
    public void stopDirectOutput() {
    	Iterator<OutputClass> outputIterator = mOutputs.iterator();
    	OutputClass output = null;
    	while (outputIterator.hasNext()) {
    		output = outputIterator.next();
    		output.shutDown();
    		output.deleteObservers();
    	}
    	
    	mOutputs.clear();
        
    }

    /**
    * Registers modules with the corresponding tasks in the toolbox
    * @param modulesToUse List of modules wich need to be connected to the corresponding tasks in the toolbox
    */
    public void registerInputsOutputs(List<Module> modulesToUse) {
    	Iterator<Module> it = modulesToUse.iterator();
        Module module = null;
        while (it.hasNext()) {
        	module = it.next() ;
            toolboxCommand(COMMAND_CODE_REGISTER_DIR_IDS, null, module.getId());
            toolboxCommand(COMMAND_CODE_REGISTER_DOW_IDS, null, module.getId());
        }
    
    }
    
    /**
     * Requests the current log from the Toolbox. The native library then calls
     * the callbackToApp().
     */
    public void requestToolboxLog() {
        toolboxCommand(COMMAND_CODE_GET_LOG);
    }

    /**
     * Method called by the native library to send a string back to the app.
     * Used to get log messages from the Toolbox.
     * 
     * @param callbackString
     */
    public void callbackToApp(String callbackString) {
        CRNToolboxCenter.receivedToolboxLog(callbackString);
    }
    

    /**
     * Called by the SensorReader if it has new sensor data to deliver to the
     * Toolbox. Delivers the data to the Toolbox via DirectInput.
     */
    public void update(Observable observable, Object data) {
        if (data instanceof ValuesDirectInputNamePair) {
            ValuesDirectInputNamePair pair = (ValuesDirectInputNamePair) data;
            if (pair.getValues() != null && mReadersReady) {
            	toolboxCommand(COMMAND_CODE_SEND_FLOAT_DIRECTINPUT, pair.getValues(),
            			pair.getDirectInputName());
            }
        }
        if (data instanceof ValuesDirectOutputNamePair) {
        	ValuesDirectOutputNamePair pair = (ValuesDirectOutputNamePair) data;
            if (pair.getValues() != null) {
            	CRNToolboxCenter.receivedDirectOutputValuePair(pair);
            }
        }
        if (data instanceof NotifyObject) {
        	NotifyObject msg = (NotifyObject) data;
        	handleMessage(msg);
        	
        }
     
        if (data instanceof DataPacket) {
        	DataPacket packet = (DataPacket) data;
        	CRNToolboxCenter.receivedDataPacket(packet);
        	
        	
        }
        
    }
    /**
     * Called by CRNToolboxCenter to transmit data to the Toolbox from the GUI 
     * @param values array of floats containg the data
     * @param inputID String which contains the id of the GUI module, should coincide with a DirectInput id in the toolbox
     */
    public void transmitData(float values[], String inputID) {
    	
    	toolboxCommand(COMMAND_CODE_SEND_FLOAT_DIRECTINPUT, values,
                inputID);
    }
    
    
    private void handleMessage(NotifyObject msg) {
    	switch (msg.getNotifyReason()) {
    	case READER_ERROR:
    		int errCode = (Integer) msg.getData();
    		CRNToolboxCenter.receivedError(errCode);
    		break;
    	case READER_READY:
    		mReadersReadyCount++;
    		
    		if (mReadersReadyCount == mReaders.size()) {
    			mReadersReady = true;
    			CRNToolboxCenter.receivedToastMessage("All readers ready!");
    		}
    		break;
    	case OUTPUT_STOPPED:
    		mStoppedOutputsCount++;
    		if (mStoppedOutputsCount == mOutputs.size()) {
    			mOutputsStopped = true;
    		}
    		
    	}
    }
    
    private void handleMessage(DataPacket data) {
    	CRNToolboxCenter.receivedDataPacket(data);
    }
    
    
    
    /**
     * Representing a Thread that runs the Toolbox.
     */
    class ToolboxThread extends Thread {

        int mCommandCode;
        float[] mFloatParam = null; // TODO
        String mStringParam = "";
        int mIntParam = 0;
        LinkedList<String> mDirectInputs;
        LinkedList<String> mDirectOutputs;
        boolean initInputs = false;
        boolean initOutputs = false;

        public ToolboxThread(int commandCode) {
            this.mCommandCode = commandCode;
        }

        public ToolboxThread(int commandCode, String stringParam) {
            this.mCommandCode = commandCode;
            if (stringParam != null) {
                this.mStringParam = stringParam;
            }
        }

        public ToolboxThread(int commandCode, int intParam) {
            this.mCommandCode = commandCode;
            this.mIntParam = intParam;
        }

        public ToolboxThread(int commandCode, String stringParam, int intParam) {
            this.mCommandCode = commandCode;
            if (stringParam != null) {
                this.mStringParam = stringParam;
            }
            this.mIntParam = intParam;
        }

        public ToolboxThread(int commandCode, float[] floatParam, String stringParam, int intParam) {
            this.mCommandCode = commandCode;
            this.mFloatParam = floatParam;
            if (stringParam != null) {
                this.mStringParam = stringParam;
            }
            this.mIntParam = intParam;
        }
        
        public ToolboxThread(int commandCode, String stringParam, int intParam, LinkedList<String> inputs, LinkedList<String> outputs) {
        	
        	this.mCommandCode = commandCode;
            if (stringParam != null) {
                this.mStringParam = stringParam;
            }
            this.mIntParam = intParam;
        
        	
        	this.mDirectInputs = inputs;
        	this.mDirectOutputs = outputs;
        	if (this.mDirectInputs.size() > 0) {
        		this.initInputs = true;
        	} else {
        		this.initInputs = false;
        	}
        	if (this.mDirectOutputs.size() > 0) {
        		this.initOutputs = true;
        	} else {
        		this.initOutputs = false;
        	}
        	
        }

        public void run() {
           
            if (this.mCommandCode == COMMAND_CODE_START_CONFIGFILE) {
            	toolboxCommand(mCommandCode, mFloatParam, mStringParam, mIntParam);
            	if (this.initInputs) {
            		Iterator<String> inputIterator = this.mDirectInputs.iterator();
            		String input = null;
            		while(inputIterator.hasNext()) {
            			input = inputIterator.next();
            			toolboxCommand(COMMAND_CODE_REGISTER_DIR_IDS, null, input);
            		}
            	}
            	if (this.initOutputs) {
            		Iterator<String> outputIterator = this.mDirectOutputs.iterator();
            		String output = null;
            		while(outputIterator.hasNext()) {
            			output = outputIterator.next();
            			//TODO Crashes sometimes, probably error in crnt.cpp
            			toolboxCommand(COMMAND_CODE_REGISTER_DOW_IDS, null, output);
            		}
            	}
            } else 
            	if (this.mCommandCode == COMMAND_CODE_STOP_TOOLBOX) {
            	try {
					sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	toolboxCommand(mCommandCode, mFloatParam, mStringParam, mIntParam);
            
            } else {
            	 toolboxCommand(mCommandCode, mFloatParam, mStringParam, mIntParam);
            }
        }

    }
    
    

    /**
     * Represents a pair consisting of a DirectInputName and an array of values.
     */
    public static class ValuesDirectInputNamePair {
        String mDirectInputName;
        float[] mValues;

        public ValuesDirectInputNamePair() {
        	mDirectInputName = null;
        	mValues = null;
        }
        
        public ValuesDirectInputNamePair(String directInputName, float[] values) {
            super();
            this.mDirectInputName = directInputName;
            this.mValues = values;
        }

        public String getDirectInputName() {
            return mDirectInputName;
        }
        
        public void setDirectInputName(String name) {
        	mDirectInputName = name;
        }

        public float[] getValues() {
            return mValues;
        }
        
        public void setValues(float [] values) {
        	mValues = values;
        }
    }
    
    /**
     * Represents a pair consisting of a DirectInputName and an array of values.
     */
    public static class ValuesDirectOutputNamePair {
        String mDirectInputName;
        float[] mValues;

        public ValuesDirectOutputNamePair() {
        	mDirectInputName = null;
        	mValues = null;
        }
        
        public ValuesDirectOutputNamePair(String directOutputName, float[] values) {
            super();
            this.mDirectInputName = directOutputName;
            this.mValues = values;
        }

        public String getDirectOutputName() {
            return mDirectInputName;
        }
        
        public void setDirectOutputName(String name) {
        	mDirectInputName = name;
        }

        public float[] getValues() {
            return mValues;
        }
        
        public void setValues(float [] values) {
        	mValues = values;
        }
    }

    /**
     * The Binder to connect to this Service.
     */
    public class ToolboxBinder extends Binder {
        ToolboxService getService() {
            return ToolboxService.this;
        }
    }
    
   

}
