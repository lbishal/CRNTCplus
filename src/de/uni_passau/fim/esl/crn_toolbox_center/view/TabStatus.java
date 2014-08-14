/*
 * This file is part of the CRN Toolbox Center.
 * Copyright 2010-2011 Jakob Weigert, University of Passau
 */

package de.uni_passau.fim.esl.crn_toolbox_center.view;

import java.text.DateFormat;
import java.util.Date;
import java.util.Observable;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TextView;
import de.uni_passau.fim.esl.crn_toolbox_center.CRNToolboxCenter;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject;
import de.uni_passau.fim.esl.crn_toolbox_center.R;

/**
 * Represents the Status tab.
 * 
 * @author Jakob Weigert <weigert@fim.uni-passau.de> (University of Passau)
 * 
 */
public class TabStatus extends MyTabActivity {

    private TextView mTextViewToolboxStatus;
    private TextView mTextViewLibInfo;
    private Resources mResources;
    private StringBuilder mStatusProtocol;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_status);

        mStatusProtocol = new StringBuilder();
        mResources = getResources();
        mTextViewToolboxStatus = (TextView) findViewById(R.id.TextViewToolboxStatus);
        mTextViewLibInfo = (TextView) findViewById(R.id.TextViewLibInfo);

        appendToProtocol(mResources.getString(R.string.toolboxinfo_app_started));
        CRNToolboxCenter.getInstance().addObserver(this);

    }

    private void appendToProtocol(String status) {

        DateFormat dateFormat = DateFormat.getTimeInstance();
        String time = dateFormat.format(new Date());

        String message = time + " | " + status + "\n";
        mStatusProtocol.append(message);
        mTextViewToolboxStatus.setText(mStatusProtocol);

    }

    // @Override
    public void update(Observable observable, Object object) {
        if (object instanceof NotifyObject) {
            NotifyObject notifyObject = (NotifyObject) object;
            Object data = notifyObject.getData();

            switch (notifyObject.getNotifyReason()) {

            case SET_TOOLBOX_INFO:
                if (data instanceof String) {
                    String toolboxInfo = (String) data;
                    if (mTextViewLibInfo != null) {
                        mTextViewLibInfo.setText(toolboxInfo);
                    }
                }
                break;

            case TOOLBOX_START_STOP:
                if (data instanceof Boolean) {
                    boolean running = (Boolean) data;
                    if (running) {
                        appendToProtocol(mResources.getString(R.string.toolboxinfo_toolbox_started));
                    } else {
                        appendToProtocol(mResources.getString(R.string.toolboxinfo_toolbox_stopped));
                    }
                }
                break;

            case DIRECT_INPUT_START_STOP:
                if (data instanceof Boolean) {
                    boolean running = (Boolean) data;
                    if (running) {
                        appendToProtocol(mResources
                                .getString(R.string.toolboxinfo_direct_input_started));
                    } else {
                        appendToProtocol(mResources
                                .getString(R.string.toolboxinfo_direct_input_stopped));
                    }
                }
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