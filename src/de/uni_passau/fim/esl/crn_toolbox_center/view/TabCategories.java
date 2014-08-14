package de.uni_passau.fim.esl.crn_toolbox_center.view;



import java.util.Observable;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject;
import de.uni_passau.fim.esl.crn_toolbox_center.R;
import de.uni_passau.fim.esl.crn_toolbox_center.OnAlarmReceiver;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.ACTLog;


public class TabCategories extends MyTabActivity{
	
	public static final String mSupportedModule = "ACTLogAnnotation";
		
	static final int ACTIVITY_REQUEST = 0;
	static final int SUCCEEDED = 0;
	static final int SD_READONLY = 48;
	static final int SD_NOREAD_NOWRITE = 49;
	
	private ListView catList;
	private TextView catText;
	private TextView actText;
	private ArrayAdapter<String> adapter;
	private String[] categories;

	private Intent activitiesIntent;
	//private Activity_screen aScreen;
	private Toast toast;
	private ACTLog actLog = null;
	int errCode = SUCCEEDED;
	//private String errMsg;
	private boolean mRunning = false;
	
	// Preferences manager
    private static SharedPreferences mCategoriesPrefs;
    private static SharedPreferences.Editor mPrefsEditor;
	
    private AlarmManager reminderAlarm;
    private Intent notificationIntent; 
    private PendingIntent contentIntent; 
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_categories);
        
        mCategoriesPrefs = getSharedPreferences("categoryPrefs", 0);
        mPrefsEditor = mCategoriesPrefs.edit();
        
        mRunning = mCategoriesPrefs.getBoolean("running", false);
        //Create listView for the Categories
        catList = (ListView)findViewById(R.id.catList);
        
        //Create text views for previous category and activity
        catText = (TextView)findViewById(R.id.lastCat_main);
        actText = (TextView)findViewById(R.id.lastAct_main);
        
        //Set the title bar to something more useful
        this.setTitle(R.string.main_title);
        
     
        
        //CRNToolboxCenter.getInstance().addObserver(this);
        actLog = (ACTLog) initTabActivity(ACTLog.class);
        if (actLog != null) {  
        	
        	//Retrieve the last selected category and activity
        	catText.setText(actLog.getLastCategory());
        	actText.setText(actLog.getLastActivity());
        	//if (mRunning == true) {
        		//categories = (String[])actLog.categories.toArray(new String[config.categories.size()]);
        		categories = actLog.getCategoriesString();
        		//Couple strings with list items
        		adapter = new ArrayAdapter<String>(this, R.layout.list_item, categories);
        
        		catList.setAdapter(adapter);
        		//catList.setVisibility(ListView.GONE);
        
	        	if (actLog.getReminder()) {
	        		
	        		reminderAlarm = (AlarmManager) getSystemService(ALARM_SERVICE);
	        		notificationIntent = new Intent(TabCategories.this, OnAlarmReceiver.class);
	        		notificationIntent.putExtra("light", actLog.getReminderTypeLight());
	        		notificationIntent.putExtra("sound", actLog.getReminderTypeSound());
	        		notificationIntent.putExtra("vibrate", actLog.getReminderTypeVibrate());
	        	
	        		contentIntent = PendingIntent.getBroadcast(TabCategories.this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	        		reminderAlarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
	        			SystemClock.elapsedRealtime() + actLog.getReminderTime() * 60000 , actLog.getReminderTime() * 60000, contentIntent);
	        	}
        	//}
        	//Let user know that category needs to be held for a short while to select it.
        	catList.setOnItemClickListener(new OnItemClickListener () {	
        		public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
        			if(actLog.getFeedbackMessage()) {
        				toast = Toast.makeText(getApplicationContext(), R.string.hold_selection_select_msg, actLog.getFeedbackDuration());
        				toast.show();
        			}				
        		}
        	
        	});
        	//Select category only when user does a long click and open the next screen.
        	catList.setOnItemLongClickListener(new OnItemLongClickListener() {

        		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        			
        			activitiesIntent = new Intent(TabCategories.this, Activity_screen.class);
        			//Make sure that the selected category gets transmitted to the next screen.
        			activitiesIntent.putExtra("category", categories[position]);
        			activitiesIntent.putExtra("ACTLog", mGson.toJson(actLog));
        			
        			//main_intent.putExtra("ACTLog", mGson.toJson(actLog));
        			if (actLog.getReminder()) {
        				reminderAlarm.cancel(contentIntent);
        			}
        			//CRNToolboxCenter.startToolboxWithConfigFile("sdcard/CRNT config files/1sensor2file.json");
        			startActivityForResult(activitiesIntent, ACTIVITY_REQUEST);
        			return false;
        		}
        
        	});
        }
    }
        
    @Override
    public void onDestroy() {
    	super.onDestroy();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	
    }
    
    @Override
    public void onResume() { 
    	super.onResume();
    	//if (mRunning == true) {
    	//	catList.setVisibility(ListView.VISIBLE);
    	//} else {
    	//	catList.setVisibility(ListView.GONE);
    	//}
    	
    }
    
    //This gets called when the activity screen closes. The updated config file is retrieved.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTIVITY_REQUEST) {
            if (resultCode == RESULT_OK) {
                
            	String actString = data.getStringExtra("ACTLog");
            	actLog = mGson.fromJson(actString, ACTLog.class);
                catText.setText(actLog.getLastCategory());
                actText.setText(actLog.getLastActivity());
                
            }
            if (resultCode == RESULT_CANCELED) {
            	finish();
            }
        }
    }	
      
    public void update(Observable observable, Object object) {
		if (object instanceof NotifyObject) {
            NotifyObject notifyObject = (NotifyObject) object;
            Object data = notifyObject.getData();
            switch (notifyObject.getNotifyReason()) {
           
            case TOOLBOX_RUNNING:
            	if (data instanceof Boolean)
            		mRunning = (Boolean) data;
            		mPrefsEditor.putBoolean("running", mRunning);
            		mPrefsEditor.commit();
            	break;
            case ACTIVITY_FINISH:
                this.finish();
                break;

            default:
                break;
            }
        }
		
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}
	
    
}
