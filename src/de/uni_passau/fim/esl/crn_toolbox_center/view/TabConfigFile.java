/*
 * This file is part of the CRN Toolbox Center.
 * Copyright 2010-2011 Jakob Weigert, University of Passau
 */

package de.uni_passau.fim.esl.crn_toolbox_center.view;

import java.io.File;
import java.util.Observable;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;

import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;
import de.uni_passau.fim.esl.crn_toolbox_center.CRNToolboxCenter;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject;
import de.uni_passau.fim.esl.crn_toolbox_center.R;
import de.uni_passau.fim.esl.crn_toolbox_center.tools.FileSystemBrowserJson;

/**
 * Represents the ConfigFile tab.
 * 
 * @author Jakob Weigert <weigert@fim.uni-passau.de> (University of Passau)
 * 
 */
public class TabConfigFile extends MyTabActivity {

    private static final int REQUEST_CODE_SELECT_FILE = 315;
    private static final int RESULT_CODE_FILE_SELECTED = 42;

    private Button mButtonSelectFile;
    private EditText mEditTextConfigFilePath;
    //private CheckBox mCheckBoxAutoDirectInput;
    private ToggleButton mButtonStartStopToolbox;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_configfile);
        initializeView();
        CRNToolboxCenter.getInstance().addObserver(this);
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

            case SET_CONFIG_FILE_PATH:
                if (data instanceof String) {
                    String configFilePath = (String) data;
                    mEditTextConfigFilePath.setText(configFilePath);
                }
                break;
            /*
            case SET_AUTO_DIRECT_INPUT:
                if (data instanceof Boolean) {
                    boolean active = (Boolean) data;
                    mCheckBoxAutoDirectInput.setChecked(active);
                }
                break;
			*/
            case TOOLBOX_RUNNING:
                if (data instanceof Boolean) {
                    boolean running = (Boolean) data;
                    mEditTextConfigFilePath.setEnabled(!running);
                    mEditTextConfigFilePath.setFocusable(!running);
                    mEditTextConfigFilePath.setFocusableInTouchMode(!running);
                    mButtonSelectFile.setEnabled(!running);
                    //mCheckBoxAutoDirectInput.setEnabled(!running);
                    mButtonStartStopToolbox.setChecked(running);
                    mButtonStartStopToolbox.setEnabled(true);
                }
                break;

            default:
                break;
            }
        }
    }

    /**
     * Called when a file is chosen using the file system browser. Sets the file
     * path to the edit text field.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_FILE && resultCode == RESULT_CODE_FILE_SELECTED) {
            try {
                String selectedFilePath = data.getData().getPath();
                mEditTextConfigFilePath.setText(selectedFilePath);
                CRNToolboxCenter.setConfigFilePath(selectedFilePath);
            } catch (Exception e) {
            	e.printStackTrace();
                Toast.makeText(this, getResources().getString(R.string.configfile_no_file),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeView() {

        mEditTextConfigFilePath = (EditText) findViewById(R.id.EditTextConfigFilePath);

        mEditTextConfigFilePath.addTextChangedListener(new TextWatcher() {
            // @Override
            public void afterTextChanged(Editable arg0) {
                //CRNToolboxCenter.setConfigFilePath(mEditTextConfigFilePath.getText().toString());
            }

            // @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                }

            // @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                }
        });

        mButtonSelectFile = (Button) findViewById(R.id.ButtonSelectFile);
        mButtonSelectFile.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                selectFile();
            }
        });
        /*
        mCheckBoxAutoDirectInput = (CheckBox) findViewById(R.id.CheckBoxAutoDirectInput);
        mCheckBoxAutoDirectInput.setOnClickListener(new OnClickListener() {
            // @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) v;
                //clickActionAutoDirectInput(checkBox.isChecked());
            }
        });
        */
        mButtonStartStopToolbox = (ToggleButton) findViewById(R.id.ButtonStartConfigFile);
        mButtonStartStopToolbox.setOnClickListener(new OnClickListener() {
            // @Override
            public void onClick(View v) {
                if (mButtonStartStopToolbox.isChecked()) {
                    clickActionStartConfigFile(v);
                } else {
                    clickActionStopConfigFile(v);
                }
            }
        });
        mButtonStartStopToolbox.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    String wait = getResources().getString(R.string.button_wait);
                    mButtonStartStopToolbox.setText(wait);
                }
                return false;
            }
        });

    }

    private void selectFile() {
        Intent intent = new Intent(this, FileSystemBrowserJson.class);
        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
    }
    /*
    private void clickActionAutoDirectInput(boolean active) {
        CRNToolboxCenter.setAutoDirectInput(active);
    }
	*/
    private void clickActionStartConfigFile(View sender) {
        String configFilePath = mEditTextConfigFilePath.getText().toString();

        // Check if file exists
        File configFile = new File(configFilePath);
        if (configFile.exists()) {
            // Start Toolbox
            CRNToolboxCenter.startToolboxWithConfigFile(configFilePath);
        } else {
            String errorText = getResources().getString(R.string.configfile_not_fond);
            Toast.makeText(this, errorText, Toast.LENGTH_LONG).show();
            mButtonStartStopToolbox.setChecked(false);
        }
    }

    private void clickActionStopConfigFile(View sender) {
        CRNToolboxCenter.stopToolbox();
    }

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}

}