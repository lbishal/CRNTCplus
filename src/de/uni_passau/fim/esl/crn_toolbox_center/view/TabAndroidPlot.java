package de.uni_passau.fim.esl.crn_toolbox_center.view;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Observable;

import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.LineAndPointRenderer;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import de.uni_passau.fim.esl.crn_toolbox_center.CRNToolboxCenter;
import de.uni_passau.fim.esl.crn_toolbox_center.DataPacket;
import de.uni_passau.fim.esl.crn_toolbox_center.NotifyObject;
import de.uni_passau.fim.esl.crn_toolbox_center.R;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.AndroidPlot;
import android.graphics.Color;
import android.os.Bundle;

public class TabAndroidPlot extends MyTabActivity {

	public static final String mSupportedModule = "AndroidPlot";
	//public static final String mSupportedModule = "SimpleGraph";
	private static final int[] TRACE_COLORS = { Color.RED, Color.BLUE,
        Color.argb(255, 50, 155, 50), Color.CYAN, Color.MAGENTA, Color.YELLOW };
	private static final int HISTORY_SIZE = 30;            // number of points to plot in history
	private XYPlot levelsPlot = null;
	private XYPlot historyPlot = null;
	private SimpleXYSeries levelsSeries = null;
	private LinkedList<SimpleXYSeries> plotSeries;
	private LinkedList<Number>[] plotData;
	
	private AndroidPlot androidPlot;

	
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_androidplot);
		
		//CRNToolboxCenter.getInstance().addObserver(this);
		androidPlot = (AndroidPlot) initTabActivity(AndroidPlot.class);
		
		levelsSeries = new SimpleXYSeries("Levels");
		
		// setup the APR Levels plot:
		levelsPlot = (XYPlot) findViewById(R.id.levelsPlot);
		levelsPlot.addSeries(levelsSeries, BarRenderer.class, new BarFormatter(Color.argb(100, 0, 200, 0), Color.rgb(0, 80, 0)));
		levelsPlot.setDomainStepValue(3);
		levelsPlot.setTicksPerRangeLabel(3);
		
		int minRange = androidPlot.getMinRange();
		int maxRange = androidPlot.getMaxRange();
		
		if (minRange != maxRange) {
			levelsPlot.setRangeBoundaries(minRange, maxRange, BoundaryMode.FIXED);
		}
        // update our domain and range axis labels:
		String xname = androidPlot.getXName();
		if (xname == null) {
			levelsPlot.setDomainLabel("X");
		} else {
			levelsPlot.setDomainLabel(xname);
		}
		levelsPlot.getDomainLabelWidget().pack();
		
		String yname = androidPlot.getYName();
		if (yname == null) {
			levelsPlot.setRangeLabel("Y");
		} else {
			levelsPlot.setRangeLabel(yname);
		}
        levelsPlot.getRangeLabelWidget().pack();
 
        levelsPlot.setGridPadding(15, 0, 15, 0);
        levelsPlot.disableAllMarkup();
        
     // get a ref to the BarRenderer so we can make some changes to it:
        BarRenderer barRenderer = (BarRenderer) levelsPlot.getRenderer(BarRenderer.class);
        if(barRenderer != null) {
            // make our bars a little thicker than the default so they can be seen better:
            barRenderer.setBarWidth(25);
        }
        
        plotData = new LinkedList[mModule.getNChannels()];
        plotSeries = new LinkedList<SimpleXYSeries>();
        for (int i = 0; i < mModule.getNChannels(); i++) {
        	plotData[i] = new LinkedList<Number>();
        	SimpleXYSeries series = new SimpleXYSeries(Integer.toString(i));
        	plotSeries.add(series);
        }
        
     // setup the APR History plot:
        historyPlot = (XYPlot) findViewById(R.id.historyPlot);
        int nPoints = androidPlot.getNPoints();
        if (minRange != maxRange) {
        	historyPlot.setRangeBoundaries(minRange, maxRange, BoundaryMode.FIXED);
        }
        if (nPoints > 0) {
        	historyPlot.setDomainBoundaries(0, nPoints, BoundaryMode.FIXED);
        } else {
        	historyPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);
        }
        for (int i = 0; i < mModule.getNChannels(); i++) {
        	historyPlot.addSeries(plotSeries.get(i), LineAndPointRenderer.class, new LineAndPointFormatter(TRACE_COLORS[i % TRACE_COLORS.length], Color.BLACK, 0));
        }
        
        historyPlot.setDomainStepValue(5);
        historyPlot.setTicksPerRangeLabel(3);
        if (xname == null) {
        	historyPlot.setDomainLabel("X");
        } else {
        	historyPlot.setDomainLabel(xname);
        }
        historyPlot.getDomainLabelWidget().pack();
        if (yname == null) {
        	historyPlot.setRangeLabel("Y");
        } else {
        	historyPlot.setDomainLabel(yname);
        }
        historyPlot.getRangeLabelWidget().pack();
        historyPlot.disableAllMarkup();
        
        
	}
	
	public void update(Observable observable, Object data) {
		if (mModule != null) {
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
     * @param packet
     *            the data packet to add
     */
    private void update(DataPacket packet) {
    	// update instantaneous data:
        
    	Number[] series1Numbers = packet.getValues();
        
    	levelsSeries.setModel(Arrays.asList(series1Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
        
    	
        if (plotData[0].size() > HISTORY_SIZE) {
        	for (int i = 0; i < plotData.length; i++) {
        		plotData[i].removeFirst();
        	}
        	
        }
        for (int i = 0; i < plotData.length; i++) {
    		plotData[i].addLast(series1Numbers[i]);
    		plotSeries.get(i).setModel(plotData[i], SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
    	}
        
        levelsPlot.redraw();
        historyPlot.redraw();
    }
	
	@Override
	public void shutDown() {
		//finish();
		
	}

}
