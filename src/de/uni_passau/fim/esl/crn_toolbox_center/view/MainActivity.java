/*
 * This file is part of the CRN Toolbox Center.
 * Copyright 2010-2011 Jakob Weigert, University of Passau
 */

package de.uni_passau.fim.esl.crn_toolbox_center.view;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.google.gson.Gson;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;

import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import de.uni_passau.fim.esl.crn_toolbox_center.CRNToolboxCenter;
import de.uni_passau.fim.esl.crn_toolbox_center.Configuration;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject;
import de.uni_passau.fim.esl.crn_toolbox_center.LogObject;
import de.uni_passau.fim.esl.crn_toolbox_center.R;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.GUIModule;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.Module;


/**
 * The Activity which is first created when the application is started. It
 * starts the model class CRNToolboxCenter with a reference to itself.
 * Furthermore, it contains all the Tabs and creates the menu and dialogs.
 * 
 * @author Jakob Weigert <weigert@fim.uni-passau.de> (University of Passau)
 * 
 */
public class MainActivity extends TabActivity implements Observer {

    private static final String TAB_INFO = "tabInfo";
    private static final String TAB_CONFIGFILE = "tabConfigFile";
    //private static final String TAB_DIRECTINPUT = "tabDirectInput";
    private static final String TAB_CONTROLPORT = "tabControlPort";
    
    private static final int NUMBER_OF_TABS = 5;
    private static final int COLOR_TAB_INACTIVE = Color.DKGRAY;
    private static final int COLOR_TAB_TEXT_INACTIVE = Color.WHITE;
    private static final int COLOR_TAB_ACTIVE = Color.argb(255, 245, 245, 245);
    private static final int COLOR_TAB_TEXT_ACTIVE = Color.BLACK;

    private TabHost mTabHost;
    private TabWidget mTabWidget;
    private int mTabID;
    private List<Class> mTabActivities = new LinkedList<Class>();
    private LogObject batteryLogger;
    
    /**
     * Called when the activity is first created. Starts the model and creates
     * the tabs. Registers this Activity as Observer to the model.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
       
        // start the model
        new CRNToolboxCenter(this); // TODO?

        mTabActivities.add(TabCategories.class);
        mTabActivities.add(TabSimpleGraph.class);
        mTabActivities.add(TabAndroidPlot.class);
        
        // make the tabs
        setContentView(R.layout.tabhost);
        createTabs();
        
        CRNToolboxCenter.getInstance().addObserver(this);
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryLogger = new LogObject(LogObject.LOG_BATTERY, "sdcard/CRNTC+BatteryLog");
        registerReceiver(batteryReceiver, filter);
    }

    /** Receive smart-phone battery information. */
    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
            int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
            
            batteryLogger.logBattery(level, scale, voltage, temp);
          
            
            Log.i("BatteryManager", "level is "+level+"/"+scale+", temp is "+temp+", voltage is "+voltage);
        }
    };
    
    /**
     * Creates the menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);
        return true;
    }

    /**
     * Called when an option in the menu is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_entry_tools:
            showDialogTools();
            return true;
        case R.id.menu_entry_log:
            CRNToolboxCenter.getToolboxLog();
            return true;
        case R.id.menu_entry_about:
            showDialogAbout();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Listener for the tab widget. Sets the selected tab.
     */
    class TabWidgetOnClickListener implements OnClickListener, OnKeyListener {
        int mTabNumber;

        public TabWidgetOnClickListener(int tabNumber) {
            super();
            this.mTabNumber = tabNumber;
        }

        // @Override
        public void onClick(View v) {
            setTab(mTabNumber);
        }

        // @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            setTab(mTabNumber);
            return true;
        }
    }

    /**
     * Called when the Activity is being destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryReceiver);
        CRNToolboxCenter.onDestroy();
    }

    /**
     * Called by the model to update the view.
     */
    // @Override
    public void update(Observable observable, Object object) {
        if (object instanceof NotifyObject) {
            NotifyObject notifyObject = (NotifyObject) object;
            Object data = notifyObject.getData();
            switch (notifyObject.getNotifyReason()) {
            
            case CONFIG_FILE_READ:
            	
            	mTabHost.setCurrentTab(0); //Needed due to bug in TabWidget
            	mTabHost.clearAllTabs();
            	createTabs();
            	mTabHost.setCurrentTab(mTabID); //Set tab to last selected
            	break;
            
            case SET_TAB:
                if (data instanceof Integer) {
                    mTabID = (Integer) data;
                    setTab(mTabID);
                }
                break;

            case SHOW_TOOLBOX_LOG:
                if (data instanceof String) {
                    String toolboxLog = (String) data;
                    DialogLog dialogLog = new DialogLog(this);
                    dialogLog.show();
                    dialogLog.setLogText(toolboxLog);
                }
                break;
            case CONFIG_FILE_ERROR:
                if (data instanceof Integer) {
                    Integer tmpErr = (Integer) data;
                    errorHandler(tmpErr);
                }
                break;
            case TOOLBOX_SERVICE_ERROR:
            	if (data instanceof Integer) {
                    Integer tmpErr = (Integer) data;
                    errorHandler(tmpErr);
                }
                break; 
            case ACTIVITY_FINISH:
                this.finish();
                break;
            case TOAST_MESSAGE:
            	if (data instanceof String) {
            		String msg = (String) data;
            		Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
                	toast.show();
            	}
            default:
                break;
            }
        }
    }

    /**
     * Creates the tabs.
     */
    private void createTabs() {

        Resources res = getResources();
        mTabHost = getTabHost();
        TabHost.TabSpec spec;
        Intent tabIntent;
        Gson gson = new Gson();
        List<Module> modules = CRNToolboxCenter.getInstance().requestModules(); //TODO Maybe change to Observer model
        Module module = null;
        String moduleGSONString = null;
        String moduleString;
        
        Iterator<Class> tabIterator = mTabActivities.iterator();
        
        Field supportedModule;
        
        Class<TabActivity> tabActivity = null;
       
                
        tabIntent = new Intent().setClass(this, TabStatus.class);
        
        spec = mTabHost
        		.newTabSpec(TAB_INFO)
                .setIndicator(res.getString(R.string.toolboxinfo_tabname),
                        res.getDrawable(R.drawable.info2))
                .setContent(tabIntent);
        mTabHost.addTab(spec);

        
        tabIntent = new Intent().setClass(this, TabConfigFile.class);
    	
    	spec = mTabHost
                .newTabSpec(TAB_CONFIGFILE)
                .setIndicator(res.getString(R.string.configfile_tabname),
                        res.getDrawable(R.drawable.config2))
                .setContent(tabIntent);
        mTabHost.addTab(spec);
        
        /*
        tabIntent = new Intent().setClass(this, TabControlPort.class);
        spec = mTabHost
                .newTabSpec(TAB_CONTROLPORT)
                .setIndicator(res.getString(R.string.controlport_tabname),
                        res.getDrawable(R.drawable.port2))
                .setContent(tabIntent);
        mTabHost.addTab(spec);
         */
        
        if (modules != null) {
        	Iterator<Module> moduleIterator = modules.iterator();
        	while (moduleIterator.hasNext()) {
        		module = moduleIterator.next();
        		if (module instanceof GUIModule) {
	        		GUIModule guiModule = (GUIModule) module;
        			tabIterator = mTabActivities.iterator();
					while(tabIterator.hasNext()) {
						tabActivity = tabIterator.next();
						
		        		
							try {
								supportedModule = tabActivity.getField("mSupportedModule");
								moduleString = (String) supportedModule.get(null);
								//for(String m : moduleStrings )
				        		if (moduleString.equals(guiModule.getType())) {
				        			moduleGSONString = gson.toJson(guiModule);
				        			tabIntent = new Intent().setClass(this, tabActivity);
				        				
				        			tabIntent.putExtra(MyTabActivity.TAB_MODULE_ID, moduleGSONString);
				        			try {
				    					
				    					int tabNameID = guiModule.getTabNameID();
				    					int drawableID = guiModule.getDrawableID();
				    					//String tabName = (String) tabNameField.get(null);
				    					String tabName = guiModule.getTabName();
				    					
				    					spec = mTabHost
				    		               .newTabSpec(tabName)
				    		               .setIndicator(tabName,
				    		               		res.getDrawable(drawableID))
				    		               .setContent(tabIntent);
				    		        	mTabHost.addTab(spec);
				    					
				    				} catch (SecurityException e) {
				    				
				    						e.printStackTrace();
				    						
				    				} catch (IllegalArgumentException e) {
				    						
				    						e.printStackTrace();
				    					
				        			}
				        		}
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
							}
						
					}
				}

        	}
        }
        
       
        mTabWidget = (TabWidget) mTabHost.getTabWidget();
        for (int i = 0 ; i < mTabWidget.getChildCount(); i++) {
        	mTabWidget.getChildAt(i).setOnClickListener(new TabWidgetOnClickListener(i));
        	mTabWidget.getChildAt(i).setOnKeyListener(new TabWidgetOnClickListener(i));
        }
        
       
    }

    /**
     * Opens the specified tab.
     * 
     * @param tabNumber
     *            The number of the tab
     */
    private void setTab(int tabNumber) {
        mTabHost.setCurrentTab(tabNumber);
        CRNToolboxCenter.setOpenedTabID(tabNumber);

        // set tab style
        for (int i = 0; i < NUMBER_OF_TABS; ++i) {
            if (mTabWidget.getChildAt(i) instanceof RelativeLayout) {
                RelativeLayout tabHeader = (RelativeLayout) mTabWidget.getChildAt(i);
                if (tabHeader.getChildAt(1) instanceof TextView) {
                    TextView tabHeaderTextView = (TextView) tabHeader.getChildAt(1);
                    tabHeader.setBackgroundColor(COLOR_TAB_INACTIVE);
                    tabHeaderTextView.setTextColor(COLOR_TAB_TEXT_INACTIVE);
                }
            }
        }

        // hightlight active tab
        if (mTabWidget.getChildAt(tabNumber) instanceof RelativeLayout) {
            RelativeLayout tabHeader = (RelativeLayout) mTabWidget.getChildAt(tabNumber);
            if (tabHeader.getChildAt(1) instanceof TextView) {
                TextView tabHeaderTextView = (TextView) tabHeader.getChildAt(1);
                tabHeader.setBackgroundColor(COLOR_TAB_ACTIVE);
                tabHeaderTextView.setTextColor(COLOR_TAB_TEXT_ACTIVE);
            }
        }
    }

    private void showDialogTools() {

        Resources res = getResources();
        Context mContext = this;
        Dialog dialog = new Dialog(mContext);

        dialog.setContentView(R.layout.dialog_tools);
        dialog.setTitle(res.getString(R.string.dialog_title_tools));

        dialog.show();

        dialog.findViewById(R.id.ButtonKillProcesss).setOnClickListener(new OnClickListener() {
            @SuppressWarnings("static-access")
            // @Override
            public void onClick(View v) {
                CRNToolboxCenter.getInstance().killProcess();
            }

        });
    }

    private void showDialogAbout() {
        Resources res = getResources();
        Context mContext = this;
        Dialog dialog = new Dialog(mContext);

        dialog.setContentView(R.layout.dialog_about);
        dialog.setTitle(res.getString(R.string.dialog_title_about));

        dialog.show();
    }

    /**
     * Represents a dialog window which shows the current Toolbox log and
     * scrolls automatically to the end of the log.
     * 
     * @author Jakob Weigert
     * 
     */
    class DialogLog extends Dialog {

        private TextView dialogLogText;

        public DialogLog(Context context) {
            super(context);
            setContentView(R.layout.dialog_log);
            setTitle(getResources().getString(R.string.dialog_title_log));
        }

        void setLogText(String logText) {
            dialogLogText = (TextView) findViewById(R.id.dialog_log_text);
            dialogLogText.setText(logText);

        }

        @Override
        public void onWindowFocusChanged(boolean hasFocus) {
            super.onWindowFocusChanged(hasFocus);
            if (dialogLogText != null) {
                int lines = dialogLogText.getLineCount();
                ScrollView scrollView = (ScrollView) findViewById(R.id.dialog_log_scrollview);
                scrollView.scrollTo(0, lines * dialogLogText.getLineHeight());
            }
        }

    }
    //TODO Create proper error handling
    private void errorHandler(int errVal) {
    	String errMsg;
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	Toast toast;
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
        case Configuration.JSON_CRNT_PLUS_ERROR: errMsg = getString(R.string.json_CRNTC_error); break;
        case Configuration.JSON_CRN_ERROR: errMsg = getString(R.string.json_CRNT_error); break;
        case Configuration.JSON_NO_TYPE: errMsg = getString(R.string.json_CRNTC_no_type); break;
        case Configuration.JSON_UNKOWN_MODULE: errMsg = getString(R.string.json_CRNTC_unknown_module); break;
        
        default: errMsg = "         "; break;
        }
        
        //TODO: Should be a user prompt dialog and app should quit when error occurs
        if (errVal != Configuration.SUCCEEDED) {
        	
        	//All errCode's smaller then 50 are fatal so quit.
        	if(errVal < 50){
        		builder.setTitle("Fatal error!");
        		builder.setCancelable(false);
        		builder.setMessage(errMsg);
        		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
        	           public void onClick(DialogInterface dialog, int id) {
        	               
        	           }
        	       }); 
        		
        		AlertDialog alert = builder.create();
        		alert.show();
        	}
        	toast = Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_LONG);
        	toast.show();
        }
        
    }

}
