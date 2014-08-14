package de.uni_passau.fim.esl.crn_toolbox_center.view;

import java.util.Observable;
import com.google.gson.Gson;
import de.uni_passau.fim.esl.crn_toolbox_center.CRNToolboxCenter;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject;
import de.uni_passau.fim.esl.crn_toolbox_center.DataPacket;
import de.uni_passau.fim.esl.crn_toolbox_center.ToolboxService.ValuesDirectOutputNamePair;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.GUIModule;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.SimpleGraph;
import android.os.Bundle;

public class TabSimpleGraph extends MyTabActivity{

	public static final String mSupportedModule = "SimpleGraph";
	//public static final String mSupportedModule = "";
	
	
	private static final int FACTOR_STRETCH_TIME = 30;
	private static final int FACTOR_STRETCH_VALUE = 2;

	private static double mStartSecond;
	private Chart2DView mChart2dView;
	private boolean mTracesInitialized;
	private SimpleGraph simpleGraph;
	    
	/**
	* Called when the activity is first created.
	*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    mChart2dView = new Chart2DView(this);
	    setContentView(mChart2dView);

	    
	   simpleGraph = (SimpleGraph) initTabActivity(SimpleGraph.class);
	}
	
	public void update(Observable observable, Object data) {
		if (mModule != null) {
			if (data instanceof ValuesDirectOutputNamePair) {
				ValuesDirectOutputNamePair pair = (ValuesDirectOutputNamePair) data;
	            if (pair.getDirectOutputName().equals(mModule.getId())) {
	            	update(pair.getValues());
	            }
	        }
			if (data instanceof DataPacket) {
				DataPacket dataPacket = (DataPacket) data;
	            if (dataPacket.getID().equals(mModule.getId())) {
	            	update(dataPacket);
	            }
	        }
		}
		if (data instanceof NotifyObject){
			NotifyObject notifyObject = (NotifyObject) data;
            Object objData = notifyObject.getData();
            switch (notifyObject.getNotifyReason()) {
            case TOOLBOX_START_STOP:
            	shutDown();
            	break;
            }
		}
		
	}
	
	 /**
     * Adds a data packet to this graph. Updates the state and the graph.
     * 
     * @param values
     *            Array of floats. First two elements contain timestamp data.
     */
    private void update(float[] values) {

    	Double[] dataValues = new Double[values.length-2];
        
    	for (int i = 0, j = 2; j < values.length; i++, j++) {
        	dataValues[i] = (double) values[j];
        }
    	
        double timestampSecond = values[0];
        double microSeconds = values[1];
        if (mStartSecond < 0) {
            mStartSecond = timestampSecond;
        }
        timestampSecond = (timestampSecond - mStartSecond);
        double timeX = timestampSecond + (microSeconds / 1000000);
        timeX *= FACTOR_STRETCH_TIME;

        if (mChart2dView != null && values != null) {
            
            if (!mTracesInitialized){
                mChart2dView.initTraces(dataValues.length);
                mTracesInitialized = true;
            }

            for (int i = 0; i < dataValues.length; i++) {
                if (dataValues[i] != null) {
                	dataValues[i] *= FACTOR_STRETCH_VALUE;
                    	mChart2dView.addCoordinate(i, new Coordinate(timeX, dataValues[i]));
                }
                
            }

        }

    }

    /**
     * Adds a data packet to this graph. Updates the state and the graph.
     * 
     * @param packet
     *            the data packet to add
     */
    private void update(DataPacket packet) {

        Double[] values = packet.getValues();
        
        double timestampSecond = packet.getTimestampSec();
        double microSeconds = packet.getTimestampUSec();
        if (mStartSecond < 0) {
            mStartSecond = timestampSecond;
        }
        timestampSecond = timestampSecond - mStartSecond;
        double timeX = timestampSecond + (microSeconds / 1000000);
        timeX *= FACTOR_STRETCH_TIME;

        if (mChart2dView != null && values != null) {
            
            if (!mTracesInitialized){
                mChart2dView.initTraces(values.length);
                mTracesInitialized = true;
            }

            for (int i = 0; i < values.length; i++) {
                if (values[i] != null) {
                    values[i] *= FACTOR_STRETCH_VALUE;
                    mChart2dView.addCoordinate(i, new Coordinate(timeX, values[i]));
                }
            }

        }

    }
    
    
    /**
     * Called when the activity is not visible any more. 
     */
    @Override
    protected void onPause() {
        super.onPause();
        //CRNTSimpleVisAndroid.disconnect();
        //    finish();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    }

	@Override
	public void shutDown() {
		mTracesInitialized = false;
    	mChart2dView.reset();
		
	}

}
