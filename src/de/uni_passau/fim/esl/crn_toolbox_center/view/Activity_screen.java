package de.uni_passau.fim.esl.crn_toolbox_center.view;

import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.google.gson.Gson;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import de.uni_passau.fim.esl.crn_toolbox_center.CRNToolboxCenter;

import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject;
import de.uni_passau.fim.esl.crn_toolbox_center.R;
import de.uni_passau.fim.esl.crn_toolbox_center.Configuration;
import de.uni_passau.fim.esl.crn_toolbox_center.OnAlarmReceiver;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.ACTLog;

public class Activity_screen extends Activity implements Observer{
	static final int SUCCEEDED = 0;
	static final int SD_READONLY = 48;
	static final int SD_NOREAD_NOWRITE = 49;
	
	Button backBtn;
	Button editBtn;
	Button showAllBtn;
	Button commentBtn;
	Button addActBtn;
	EditText textField;
	ListView actList;
	TextView prevCatTxt;
	TextView prevActTxt;
	Intent act_intent;
	ArrayAdapter<String> adapter;
	ArrayAdapter<String> delete_adapter;
	List<String> actL;
	List<String> userActL;
	Toast toast;
	Intent intent;
	ACTLog actLog = null;
	String currCat;
	//LogObject logger = null;
	boolean showAll = false;
	boolean delete = false;
	//private boolean mRunning = false;
	int errCode = SUCCEEDED;

	Gson mGson;
	
	AlarmManager reminderAlarm;
	Intent notificationIntent; 
	PendingIntent contentIntent; 
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_activity);
        
        mGson = new Gson();
        
        //Create all the button instances
        backBtn = (Button)findViewById(R.id.backbtn_actscreen);
        editBtn = (Button)findViewById(R.id.editbtn_actscreen);
        showAllBtn = (Button)findViewById(R.id.showAllbtn);
        commentBtn = (Button)findViewById(R.id.commentbtn_actscreen);
        addActBtn = (Button)findViewById(R.id.addbtn_actscreen);
        
        
        commentBtn.setVisibility(Button.GONE);
        //Create EditText instance
        textField = (EditText)findViewById(R.id.EditText_act);
        
        //Create ListView instance
        actList = (ListView)findViewById(R.id.actList);
        
        //Create the TextView instances
        prevCatTxt = (TextView)findViewById(R.id.lastCat_act);
        prevActTxt = (TextView)findViewById(R.id.lastAct_act);
        
        //Get Intent for this screen
        intent = new Intent();
        intent = getIntent();
        
        //Set the title bar to something more usefull
        this.setTitle(R.string.activity_title);
        
        //Get the category that the user selected in the previous screen
        currCat = intent.getStringExtra("category");
        //Get the ACTLog file from the previous screen.
        String actString = intent.getStringExtra("ACTLog");
        actLog = mGson.fromJson(actString, ACTLog.class);
        //Starts up the reminder alarm for the activity screen. Should not be called after config has been initialized/retrieved.
        startupReminder();
        
        //Get a List object with all the activities that correspond with the category selected in the previous screen
        actL = actLog.getActivityIntersectionList(currCat);
        userActL = actLog.getUserActivityStringList();
        
        adapter = new ArrayAdapter<String>(this, R.layout.list_item, actL);
                
        delete_adapter = new ArrayAdapter<String>(this, R.layout.delete_item, userActL );
       
        actList.setAdapter(adapter);
        
        //Hide or show buttons depending on config file.
        if(actLog.getAddActivities()) {
        	//Button is now visible
        	addActBtn.setVisibility(Button.VISIBLE); 
        } else {
        	addActBtn.setVisibility(Button.INVISIBLE); 
        }
        
        if(actLog.getDeleteActivities()) {
        	editBtn.setVisibility(Button.VISIBLE);
        } else {
        	editBtn.setVisibility(Button.INVISIBLE);
        }
        
        //Retrieve the last selected category and activity.
        prevCatTxt.setText(actLog.getLastCategory());
        prevActTxt.setText(actLog.getLastActivity());
        
        //CRNToolboxCenter.getInstance().addObserver(this);
        
        //Initialize the logObject before we start logging
        //CRNToolboxCenter.getInstance().requestLogObject();
                
        //After a long click write activity to log file.
        actList.setOnItemLongClickListener(new OnItemLongClickListener() {
        	
			public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
				
				String selectedActStr;
				/*
				if(config.feedback_message) {
					if (delete) {
						toast = Toast.makeText(getApplicationContext(),R.string.activity_deleted_msg, config.feedback_message_duration);
					} else {
						toast = Toast.makeText(getApplicationContext(), R.string.activity_logged_msg, config.feedback_message_duration);
					}
					toast.show();
				}
				*/
				selectedActStr = (String)actList.getItemAtPosition(position);
				int tmpNr = actLog.getActivityNrByName(selectedActStr);
				if (tmpNr != -1) {
					if (delete == false) {
						float choice[] = new float[1];
						choice[0] = actLog.getNumberByName(selectedActStr);
						CRNToolboxCenter.getInstance().requestDataTransmit(actLog.processDataToFloat(choice) , actLog.getId());
						if(actLog.getFeedbackMessage()) {
							toast = Toast.makeText(getApplicationContext(), R.string.activity_logged_msg, actLog.getFeedbackDuration());
						}
						actLog.setLastCategory(currCat);
						actLog.setLastActivity(selectedActStr);
						prevCatTxt.setText(actLog.getLastCategory());
						prevActTxt.setText(actLog.getLastActivity());
										
						//Reset the reminder if the user logged an activity.
						if (actLog.getReminder()) {
							reminderAlarm.cancel(contentIntent);
							startupReminder();
						}
					} else {
						errCode = actLog.deleteActivity((String)actList.getItemAtPosition(position));
						CRNToolboxCenter.getInstance().requestConfigFileWrite(actLog);
						if(actLog.getFeedbackMessage()) {
							toast = Toast.makeText(getApplicationContext(),R.string.activity_deleted_msg, actLog.getFeedbackDuration());
						}
						errCode = SUCCEEDED;
						delete_adapter.remove((String)actList.getItemAtPosition(position));
						delete_adapter.notifyDataSetChanged();
					}
					toast.show();
					if(showAll) {
						errCode = actLog.changeActivityCategoryByName(selectedActStr, currCat);
						CRNToolboxCenter.getInstance().requestConfigFileWrite(actLog);
						errCode = SUCCEEDED;
					}
					
					//Save the changes in ACTLog file so that these are known in main screen as well.
					
					String actString = mGson.toJson(actLog);
					intent.putExtra("ACTLog",actString);
    				setResult(RESULT_OK, intent);
				}
				return true;
			}
        	
        });
        actList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				if(actLog.getFeedbackMessage()) {
					if(delete) {
						toast = Toast.makeText(getApplicationContext(), R.string.hold_selection_delete_msg, actLog.getFeedbackDuration());
						toast.show();
					} else {
						toast = Toast.makeText(getApplicationContext(), R.string.hold_selection_select_msg, actLog.getFeedbackDuration());
						toast.show();
					}
				}
				
			}
        });
        
        //On Back button click return to main screen.
        backBtn.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		showAll = false;
        		//Remove the focus from the EditText because it can cause the softkeyboard to appear during a orientation change
				textField.clearFocus();
				setResult(RESULT_OK,intent);
				finish();
			}
         });
        //Show all activities regardless of category
        showAllBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				if(showAll == false) {
					Iterator<String> it = actLog.getActivityStringList().iterator();
					adapter.clear();
					while(it.hasNext()) {
						adapter.add(it.next());
					}
					adapter.notifyDataSetChanged();
					showAllBtn.setText(R.string.hide_all_btn_act);
					showAll = true;
				} else {
					Iterator<String> it = actLog.getActivityIntersectionList(currCat).iterator();
					adapter.clear();
					while(it.hasNext()) {
						adapter.add(it.next());
					}
					adapter.notifyDataSetChanged();
					showAllBtn.setText(R.string.show_all_btn_act);
					showAll = false;
				}
				//Remove the focus from the EditText because it can cause the softkeyboard to appear during a orientation change
				textField.clearFocus();
			}
		});
        //Add value from text field as a new activity if the text field isn't empty
        addActBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
        		//MyActivity newActivity;
        		String name;
        		String[] category = new String[1];
        		String[] new_category = new String[1];
        		if(actLog.getAddActivities()) {	
        			if(textField.length()>0) {
        				name = new String(textField.getText().toString());
        				category[0] = currCat;
        				new_category[0] = "";
        				
        				//newActivity = new MyActivity(name,actLog.generateIndex(),category,new_category,true,100);
        				actLog.addActivity(name,category,new_category,true);
        				CRNToolboxCenter.getInstance().requestConfigFileWrite(actLog);
        				
						if (errCode == Configuration.SUCCEEDED) {
        					adapter.add(name);
        					//Save the changes in ACTLog file so that these are known in main screen as well.
        					String actString = mGson.toJson(actLog);
        					intent.putExtra("ACTLog",actString);
        					setResult(RESULT_OK, intent);
        				}
						errorHandler(errCode);
						errCode = SUCCEEDED;
        			}
				
        		}
        		textField.setText("");
        		//Remove the focus from the EditText because it can cause the softkeyboard to appear during a orientation change
				textField.clearFocus();
        	}
		});
        commentBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if(textField.length()>0) {
					//TODO Think about logging comments
					//errCode = logger.logComment(textField.getText().toString());
					errorHandler(errCode);
					errCode = SUCCEEDED;
				}
				textField.setText("");
				//Remove the focus from the EditText because it can cause the softkeyboard to appear during a orientation change
				textField.clearFocus();
			}
		});
        editBtn.setOnClickListener(new OnClickListener() {
			
        	//adapter is replaced with delete_adapter which contains all the user created activities. 
        	//Not sure if this is the correct way of doing it. There might be memory leaks!
			public void onClick(View v) {
				if (delete == false) {
					Iterator<String> it = actLog.getUserActivityStringList().iterator();
					delete_adapter.clear();
					while(it.hasNext()) {
						delete_adapter.add(it.next());
					}
					actList.setAdapter(delete_adapter);
					editBtn.setText(R.string.done_btn_act);
					
					showAllBtn.setVisibility(Button.INVISIBLE);
					addActBtn.setVisibility(Button.INVISIBLE);
					commentBtn.setVisibility(Button.INVISIBLE);
					textField.setVisibility(EditText.INVISIBLE);
					
					delete = true;
				} else {
					Iterator<String> it = actLog.getActivityIntersectionList(currCat).iterator();
					adapter.clear();
					while(it.hasNext()) {
						adapter.add(it.next());
					}
					
					actList.setAdapter(adapter);
					editBtn.setText(R.string.edit_btn_act);
					
					showAllBtn.setVisibility(Button.VISIBLE);
					if (actLog.getAddActivities()) {
						addActBtn.setVisibility(Button.VISIBLE);
					}
					commentBtn.setVisibility(Button.VISIBLE);
					textField.setVisibility(EditText.VISIBLE);
					
					delete = false;
				}
				
			}
		});
    }
    
    private void startupReminder() {
    	if (actLog.getReminder()) {
        	
        	reminderAlarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        	notificationIntent = new Intent(Activity_screen.this, OnAlarmReceiver.class);
        	notificationIntent.putExtra("light", actLog.getReminderTypeLight());
        	notificationIntent.putExtra("sound", actLog.getReminderTypeSound());
        	notificationIntent.putExtra("vibrate", actLog.getReminderTypeVibrate());
        	
        	contentIntent = PendingIntent.getBroadcast(Activity_screen.this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        	reminderAlarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
        			SystemClock.elapsedRealtime() + actLog.getReminderTime() * 60000, actLog.getReminderTime() * 60000, contentIntent);
        }
    }
    
    //Save the showAll and delete state because this gets destroyed when the layout orientation changes
    @Override 
    public void onSaveInstanceState(Bundle outState) 
    {
   
    	//---save whatever you need to persist—
        outState.putBoolean("showAll", showAll);
        outState.putBoolean("delete", delete);
        
        super.onSaveInstanceState(outState); 
    }
    
    //Retrieve the showAll and delete state
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) 
    {
        super.onRestoreInstanceState(savedInstanceState);
        //---retrieve the information persisted earlier---
        showAll = savedInstanceState.getBoolean("showAll");
        delete = savedInstanceState.getBoolean("delete");
        if(showAll) {
        	Iterator<String> it = actLog.getActivityStringList().iterator();
			adapter.clear();
			while(it.hasNext()) {
				adapter.add(it.next());
			}
			adapter.notifyDataSetChanged();
        	showAllBtn.setText(R.string.hide_all_btn_act);
        } else {
        	Iterator<String> it = actLog.getActivityIntersectionList(currCat).iterator();
			adapter.clear();
			while(it.hasNext()) {
				adapter.add(it.next());
			}
			adapter.notifyDataSetChanged();
        	showAllBtn.setText(R.string.show_all_btn_act);
        }
        if(delete) {
        	actList.setAdapter(delete_adapter);
        	editBtn.setText(R.string.done_btn_act);
        	showAllBtn.setVisibility(Button.INVISIBLE);
			addActBtn.setVisibility(Button.INVISIBLE);
			commentBtn.setVisibility(Button.INVISIBLE);
			textField.setVisibility(EditText.INVISIBLE);
        } else {
        	actList.setAdapter(adapter);
        	editBtn.setText(R.string.edit_btn_act);
        	showAllBtn.setVisibility(Button.VISIBLE);
			addActBtn.setVisibility(Button.VISIBLE);
			commentBtn.setVisibility(Button.VISIBLE);
			textField.setVisibility(EditText.VISIBLE);
        }
        
    }
    
    private void errorHandler(int errVal) {
    	String errMsg;
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
    	//Handle errors
        switch(errVal) {
        case Configuration.IO_EXCEPTION: errMsg = getString(R.string.io_exception_conf_err); break;
        case Configuration.JSON_PREFS_ERROR: errMsg = getString(R.string.json_prefs_err); break;
        case Configuration.JSON_ACTIVITIES_ERROR: errMsg = getString(R.string.json_activities_err); break;
        case Configuration.JSON_LASTINFO_ERROR: errMsg = getString(R.string.json_lastinfo_err); break;
        case Configuration.JSON_WRITE_ERROR: errMsg = getString(R.string.json_write_err); break;
        case Configuration.ACTIVITY_EXISTS: errMsg = getString(R.string.activity_exists_err); break;
        case Configuration.ACTIVITY_IN_CATEGORY: errMsg = getString(R.string.activity_in_category_err); break;
        //case ConfigObject.ACTIVITY_NOT_FOUND: errMsg = getString(R.string.activity_notfound_err); break;
        case Configuration.ACTIVITY_NOT_USER_CREATED: errMsg = getString(R.string.activity_notuser_created_err); break;
        case SD_READONLY: errMsg = getString(R.string.sd_readonly_err); break;
        case SD_NOREAD_NOWRITE: errMsg = getString(R.string.sd_noreadandwrite_err); break;
        default: errMsg = ""; break;
        }
        
        //TODO: Should be a user prompt dialog and app should quit when error occurs
        if (errVal != SUCCEEDED) {
        	
        	//All errCode's smaller then 50 are fatal so quit.
        	if(errVal < 50){
        		builder.setTitle("Fatal error!");
        		builder.setCancelable(false);
        		builder.setMessage(errMsg);
        		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        			public void onClick(DialogInterface dialog, int whichButton) {
        				intent.putExtra("fatalError", true); 
        				setResult(RESULT_CANCELED, intent);
        				Activity_screen.this.finish();
        				  }
        				});
        		
        		AlertDialog alert = builder.create();
        		alert.show();
        		
        	}
        	toast = Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_LONG);
        	toast.show();
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        	showAll = false;
    		//Remove the focus from the EditText because it can cause the softkeyboard to appear during a orientation change
			textField.clearFocus();
			setResult(RESULT_OK,intent);
			finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    
    @Override  
    public Object onRetainNonConfigurationInstance() 
    {   
        return(adapter);   
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

	public void update(Observable observable, Object object) {
		if (object instanceof NotifyObject) {
            NotifyObject notifyObject = (NotifyObject) object;
            Object data = notifyObject.getData();
            switch (notifyObject.getNotifyReason()) {

            //TODO Think about log object. Perhaps for comments?
            /*
            case LOG_FILE_READ:
                if (data instanceof LogObject) {
                   logger = (LogObject) data;
                   
                }
                break;
             */
            case LOG_FILE_ERROR:
                if (data instanceof Integer) {
                    Integer tmpErr = (Integer) data;
                    errorHandler(tmpErr);
                }
                break;

            case TOOLBOX_RUNNING:
            	if (data instanceof Boolean)
            		//mRunning  = (Boolean) data;
            	break;
            case ACTIVITY_FINISH:
                this.finish();
                break;

            default:
                break;
            }
        }
		
	}
    
}