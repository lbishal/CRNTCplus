/*
 * This file is part of the CRN Toolbox Center.
 * Copyright 2010-2011 Jakob Weigert, University of Passau
 */

package de.uni_passau.fim.esl.crn_toolbox_center.view;

import java.util.Observable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ToggleButton;
import de.uni_passau.fim.esl.crn_toolbox_center.CRNToolboxCenter;
import de.uni_passau.fim.esl.crn_toolbox_center.R;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject;

/**
 * Represents the ControlPort tab.
 * 
 * @author Jakob Weigert <weigert@fim.uni-passau.de> (University of Passau)
 * 
 */
public class TabControlPort extends MyTabActivity {

    private EditText mEditTextControlPort;
    private ToggleButton mButtonStartStopToolbox;

    /**
     * Called when the activity is first created.
     */
    // @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_controlport);
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

            case SET_CONTROL_PORT:
                if (data instanceof Integer) {
                    int controlPort = (Integer) data;
                    mEditTextControlPort.setText("" + controlPort);
                }
                break;

            case TOOLBOX_RUNNING:
                if (data instanceof Boolean) {
                    boolean running = (Boolean) data;
                    mEditTextControlPort.setEnabled(!running);
                    mEditTextControlPort.setFocusable(!running);
                    mEditTextControlPort.setFocusableInTouchMode(!running);
                    mButtonStartStopToolbox.setChecked(running);
                }
                break;

            default:
                break;
            }
        }
    }

    private void initializeView() {

        mEditTextControlPort = (EditText) findViewById(R.id.EditTextControlPort);

        mEditTextControlPort.addTextChangedListener(new TextWatcher() {
            // @Override
            public void afterTextChanged(Editable arg0) {
                CRNToolboxCenter.setControlPort(getControlPortFromView());
            }
            // @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
            // @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
        });

        mButtonStartStopToolbox = (ToggleButton) findViewById(R.id.ButtonStartControlPort);
        mButtonStartStopToolbox.setOnClickListener(new OnClickListener() {
            // @Override
            public void onClick(View v) {
                if (mButtonStartStopToolbox.isChecked()) {
                    clickActionStartControlPort(v);
                } else {
                    clickActionStopConrolPort(v);
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

    private int getControlPortFromView() {
        String controlPortString = mEditTextControlPort.getText().toString();
        int controlPort = 0;
        try {
            controlPort = Integer.parseInt(controlPortString);
        } catch (NumberFormatException e) {
            // TODO Check if port is inside a port range?
        }
        return controlPort;
    }

    private void clickActionStartControlPort(View sender) {
        CRNToolboxCenter.startToolboxWithControlPort(getControlPortFromView());
    }

    private void clickActionStopConrolPort(View sender) {
        CRNToolboxCenter.stopToolbox();
    }

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}

}