package de.uni_passau.fim.esl.crn_toolbox_center;

import java.io.File; 
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import de.uni_passau.fim.esl.crn_toolbox_center.modules.ACTLog;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.ANTPlusHRM;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.AndroidPlot;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.BatteryStatus;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.BodyANT;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.ETHOS;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.FileWriter;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.FileWriterMean;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.FileWriterSFTP;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.Module;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.RandomGenerator;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.Shimmer;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.SimpleGraph;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.SimpleGraphTCP;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.SmartphoneAccelerometer;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.SmartphoneLight;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.SmartphoneLocation;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.SmartphoneMagnet;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.SmartphoneMic;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.SmartphoneOrientation;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.SmartphoneProximity;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.SuuntoBelt;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.ZephyrHxM;

public class Configuration {
	// Configuration error codes range between 0 and 100!
	public static final int SUCCEEDED = 0;
	public static final int JSON_PREFS_ERROR = 1;
	public static final int JSON_ACTIVITIES_ERROR = 2;
	public static final int JSON_LASTINFO_ERROR = 3;
	public static final int IO_EXCEPTION = 4;
	public static final int JSON_WRITE_ERROR = 5;
	public static final int JSON_CRNT_NO_PLUS_ERROR = 6;
	public static final int JSON_CRNT_PLUS_ERROR = 7;
	public static final int MODULE_NOT_FOUND = 8;
	public static final int TMP_CONF_WRITE_ERROR = 9;
	public static final int JSON_NO_CRNT = 10;
	public static final int JSON_CRN_ERROR = 11;
	public static final int JSON_NO_TYPE = 12;
	
	public static final int ACTIVITY_EXISTS = 56;
	public static final int ACTIVITY_NOT_USER_CREATED = 57;
	public static final int ACTIVITY_NOT_FOUND = 58;
	public static final int ACTIVITY_IN_CATEGORY = 59;
	public static final int JSON_UNKOWN_MODULE = 60;
	
	private static final String TEMP_CONF_PATH = "sdcard/crntTempConf.json";
	
	//CRNTC+ members
	private List<Module> mModules;
	@SuppressWarnings("rawtypes")
	private static final Map<String, Class> mMapModuleClasses = new HashMap<String, Class>();
	
	//CRNT members

	private JSONObject mCRNTJson;
	private List<Integer> mUsedPorts;
	private LinkedList<String> mDirectInputs = new LinkedList<String>();
	private LinkedList<String> mDirectOutputs = new LinkedList<String>();
	private String mTaskIds = "";
	
	private Gson mGson;
	
	public Configuration() {
		 
		 mModules = new ArrayList<Module>();
		 
		 mGson = new GsonBuilder().setPrettyPrinting().create();
		 mUsedPorts = new LinkedList<Integer>();
		 
		 //Module class definitions for Gson parsing
		 mMapModuleClasses.put("SmartphoneAccelerometer", SmartphoneAccelerometer.class);
		 mMapModuleClasses.put("SmartphoneMagnet", SmartphoneMagnet.class);
		 mMapModuleClasses.put("SmartphoneOrientation", SmartphoneOrientation.class);
		 mMapModuleClasses.put("SmartphoneLight", SmartphoneLight.class);
		 mMapModuleClasses.put("SmartphoneProximity", SmartphoneProximity.class);
		 mMapModuleClasses.put("SmartphoneLocation", SmartphoneLocation.class);
		 mMapModuleClasses.put("ACTLogAnnotation", ACTLog.class);
		 mMapModuleClasses.put("BodyANT", BodyANT.class);
		 mMapModuleClasses.put("SimpleGraphTCP", SimpleGraphTCP.class);
		 mMapModuleClasses.put("BatteryStatus", BatteryStatus.class);
		 mMapModuleClasses.put("ETHOS", ETHOS.class);
		 mMapModuleClasses.put("SuuntoBelt", SuuntoBelt.class);
		 mMapModuleClasses.put("RandomGenerator" , RandomGenerator.class);
		 mMapModuleClasses.put("Shimmer", Shimmer.class);
		 mMapModuleClasses.put("FileWriter", FileWriter.class);
		 mMapModuleClasses.put("ANTPlusHRM", ANTPlusHRM.class);
		 mMapModuleClasses.put("SimpleGraph", SimpleGraph.class);
		 mMapModuleClasses.put("AndroidPlot", AndroidPlot.class);
		 mMapModuleClasses.put("FileWriterSFTP", FileWriterSFTP.class);
		 mMapModuleClasses.put("ZephyrHxM", ZephyrHxM.class);
		 mMapModuleClasses.put("FileWriterMean", FileWriterMean.class);
		 mMapModuleClasses.put("SmartphoneMic",SmartphoneMic.class);
		 
	}
	
	 // Will fill in all the config values based on the input String. Returns 0 if successful, 1 otherwise.
	 private int obtainConfigFromString(String jString) {
		int errCode = SUCCEEDED;
		
		
		JSONObject jObject;
		JSONObject jCRNTC;
		JSONObject jCRNTConfig;
		JSONArray jTasks;
		JSONArray jModules;
		JSONObject jArrayObject;
				
		try {
			jObject = new JSONObject(jString);
			
			jCRNTC = jObject.getJSONObject("CRNT+");
			jModules = jCRNTC.getJSONArray("modules");
			for (int i=0; i < jModules.length(); i++) {
				//ModuleClass mClass;
				jArrayObject = jModules.getJSONObject(i);
				errCode = addModule(jArrayObject);
				if (errCode == JSON_NO_TYPE) {
					return errCode;
				}
				
			}
		} catch (JSONException e) {
			e.printStackTrace();
			errCode = Configuration.JSON_CRNT_PLUS_ERROR;
			return errCode;
		}
		
		try {
			mDirectInputs.clear();
			mDirectOutputs.clear();
			jObject = new JSONObject(jString);
			jCRNTConfig = jObject.getJSONObject("CRNT");
			
			mCRNTJson = jCRNTConfig;
			jTasks = jCRNTConfig.getJSONArray("tasks");
			for (int i=0; i < jTasks.length(); i++) {
				jArrayObject = jTasks.getJSONObject(i);
				mTaskIds += jArrayObject.getString("id");
				if (jArrayObject.has("port")) {
					mUsedPorts.add(jArrayObject.getInt("port"));
				}
				if (jArrayObject.getString("type").equals("DirectInputReader")) {
					mDirectInputs.add(jArrayObject.getString("id"));
				}
				if (jArrayObject.getString("type").equals("DirectOutputWriter")) {
					mDirectOutputs.add(jArrayObject.getString("id"));
				}
				
			}
			errCode = WriteTempCRNTConfig(jCRNTConfig);
			
		} catch (JSONException e) {
			e.printStackTrace();
			errCode = Configuration.JSON_CRN_ERROR;
			return errCode;
		}
		
		
		return errCode;
}
	 
	 public int readConfigFile(String pathName) {
		InputStream instream;
	    File file;
	    byte[] data;
	    String jString;
	    int errCode = Configuration.SUCCEEDED;
	    
	    //First search in the ACTLog folder on the sdcard
	    file = new File(pathName);
	    try {
			instream = new FileInputStream(file);
			data = new byte[instream.available()];
			instream.read(data);
			jString = new String(data);
			mModules.clear();
			mUsedPorts.clear();
			errCode = obtainConfigFromString(jString);
			instream.close();
			
			} catch (IOException e) {
				e.printStackTrace();
				errCode = Configuration.IO_EXCEPTION;
				
			}
			return errCode;
	 }
	 
	 private int writeConfigFile(String pathName) {
		File f;
		FileOutputStream out;
		String prefString = "";
		byte[] data = null;
		
		JSONObject jConfigObject = new JSONObject();
		JSONObject jCRNTCplusObject = new JSONObject();
		JsonArray jArray = new JsonArray();
		JSONArray jTmpObject;
		
		Iterator<Module> it = mModules.iterator();
			
		f = new File(pathName);
				
		if (f.exists()) {
			try {
				
				Module tmpModule;
				while (it.hasNext()) {
					tmpModule = it.next();
					jArray.add(mGson.toJsonTree(tmpModule));
										
				}
				
				jTmpObject = new JSONArray(jArray.toString());
				
				jCRNTCplusObject.put("modules", jTmpObject);
				jConfigObject.put("CRNT+", jCRNTCplusObject);
				jConfigObject.put("CRNT",mCRNTJson);
				
				prefString = jConfigObject.toString(1);
			} catch (JSONException e) {
				e.printStackTrace();
				return Configuration.JSON_WRITE_ERROR;
			}
			try {
				out = new FileOutputStream(f);
				data = prefString.getBytes();
				out.write(data);
				out.flush();
				out.close();
				
			} catch (IOException e) {
				e.printStackTrace();
				return Configuration.JSON_WRITE_ERROR;
				
			}
			
		}
		return Configuration.SUCCEEDED; 
	 }
	 /**
	  * Replaces the old instance of module with a new one and removes it from the configuration. 
	  * The configuration file on the sdcard will then be updated. 
	  * @param module new instance of the updated module
	  * @param filePath
	  * @return 0 on success
	  */
	 public int updateModule(Module module, String filePath) {
		 Iterator<Module> it = mModules.iterator();
		 Module tmpModule = null;
		 while (it.hasNext()) {
			 tmpModule = it.next();
			 if (tmpModule.getId().equals(module.getId())) {
				 mModules.remove(tmpModule);
				 mModules.add(module);
				 return writeConfigFile(filePath);
			 }
		 }
		 
		 return MODULE_NOT_FOUND;
	 }
	
	 private int addModule(JSONObject jObject) {
		 Module mClass;
		 
		 String type = new String();
		 try {
			type = jObject.getString("type");
		 } catch (JSONException e) {
			
			e.printStackTrace();
			return JSON_NO_TYPE;
		}
		 
		 if (mMapModuleClasses.containsKey(type)) {
			 mClass = mGson.fromJson(jObject.toString(), mMapModuleClasses.get(type));
			 			 
			 mModules.add(mClass);
			 return SUCCEEDED;
		 } else {
			 return JSON_UNKOWN_MODULE;
		 }
	 }
	 
	 /** Returns a HashMap of all the module ids which are in both CRNTC+ and CRNT
	  * 
	  * @return HashMap<String, Boolean>
	  */
	 public HashMap<String, Boolean> ModulesToUse() {
		 Iterator<Module> moduleIt = mModules.iterator();
		 
		 HashMap<String, Boolean> resultMap = new HashMap<String, Boolean>();
		  
		 while(moduleIt.hasNext()) {
			 Module tmpModule = moduleIt.next();
			 if (mTaskIds.contains(tmpModule.getId())) {
				 resultMap.put(tmpModule.getId(), true);
			 }
		 }
		
		 return resultMap;
	 }
	 
	 /** Returns all the ports that are being used by the tasks in CRNT
	  * 
	  * @return List<Integer>
	  */
	 public List<Integer> PortsToUse() {
		 return mUsedPorts;
	 }
	 
	 public String getTempConfString() {
		 return TEMP_CONF_PATH;
	 }
	 
	 public LinkedList<String> getDirectInputs() {
		 return mDirectInputs;
	 }
	 
	 public LinkedList<String> getDirectOutputs() {
		 return mDirectOutputs;
	 }
	 
	 /** Writes a temporary config file for the CRNT library. Returns 0 if succeeded.
	  * 
	  * @param config
	  * @return integer
	  */
	 private int WriteTempCRNTConfig(JSONObject config) {
		 //TODO Doesn't work yet due to bug. Auto generated configfiles can not yet be read by the toolbox
		 int errCode = Configuration.SUCCEEDED;
		 File f;
		 FileOutputStream out;
		 String prefString = "";
		 byte[] data = null;
		 
		 f = new File(TEMP_CONF_PATH);
			
		 if (f.exists()) {
			 f.delete();
		 }
		 try {
			prefString = config.toString(2);
			
		 } catch (Exception e) {
			e.printStackTrace();
			
		 }
		 try {
			out = new FileOutputStream(f);
			data = prefString.getBytes();				
			out.write(data);
			out.flush();
			out.close();
		 } catch (FileNotFoundException e) {
			 // TODO Auto-generated catch block
			 errCode = TMP_CONF_WRITE_ERROR;
			  
		 } catch (IOException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
			 errCode = TMP_CONF_WRITE_ERROR;
		 }
			
		 
		 return errCode;
	 }
	 
	 
	 public List<Module> getModules() {
		 return mModules;
	 }
	 
	 /** Returns a module based on id. Returns null if id does not exist
	  * 
	  * @param id 
	  * @return ModuleClass which matches the id or null of id does not exist
	  */
	 public Module getModuleById(String id) {
		 Iterator<Module> it = mModules.iterator();
		 Module module;
		 while (it.hasNext()) {
			 module = it.next();
			 if (module.getId().equals(id)) {
				 return module;
			 }
		 }
		 return null;
	 }
	 
	 
	 
	 
}


