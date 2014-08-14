package de.uni_passau.fim.esl.crn_toolbox_center.readers;

import java.util.Iterator;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.dsi.ant.AntDefine;
import com.dsi.ant.AntInterface;
import com.dsi.ant.AntInterfaceIntent;
import com.dsi.ant.AntMesg;
import com.dsi.ant.exception.AntInterfaceException;
import com.dsi.ant.exception.AntServiceNotConnectedException;

import de.uni_passau.fim.esl.crn_toolbox_center.ToolboxService;
import de.uni_passau.fim.esl.crn_toolbox_center.ToolboxService.ValuesDirectInputNamePair;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.ANTModule;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.Module;

public class ANTReader extends ReaderClass {
	private static final byte SET_NETWORK_KEY_MSG_128BIT = 0x76;
	private static final byte RESPONSE_NO_ERROR = 0x00;
	
	private static final byte RESPONSE_ID_OFFSET = 0x03;
	private static final byte RESPONSE_CODE_OFFSET = 0x04;
	
	public static final String[] mSupportedModules = {"BodyANT", "VPatch", "ETHOS", "SuuntoBelt", "ANTPlusHRM"};
	
	private static final String TAG = "CRNTC+ ANT";
	private static AntInterface sAntReceiver;
	private boolean mServiceConnected = true;
	private boolean mEnabling = true;
	private boolean mClaimedAntInterface;
	private boolean mAntInterrupted = false;
	private IntentFilter statusIntentFilter;
    private IntentFilter messageIntentFilter;
    float[] mValues = new float[6];
    ValuesDirectInputNamePair pair;
    
    private ANTReader mAntReader = null;
    
    //private List<ModuleClass> mModules;
	
    private AntInterface.ServiceListener mAntServiceListener = new AntInterface.ServiceListener() 
	 { 
		 public void onServiceConnected() {
	     
			 Log.d(TAG, "mAntServiceListener onServiceConnected()");
	               
	         mServiceConnected = true;
	               
	         try {
	        	 if(sAntReceiver != null) {
	        		 if(mEnabling) {
	        			 //TODO This call sometimes fails the first time after reboot. Don't know why.
	        			 sAntReceiver.enable();
	        		 }
	                   
	        		 mClaimedAntInterface = sAntReceiver.hasClaimedInterface();

	        		 if(mClaimedAntInterface) {
	        			Log.d(TAG, "mAntServiceListener Claimed interface");
	                	// mAntMessageReceiver should be registered any time we have control of the ANT Interface
	        			receiveAntRxMessages(true);
	        		 } else {
	        			 // Need to claim the ANT Interface if it is available, now the service is connected
	        			 if(mAntInterrupted == false) {
	        				 mClaimedAntInterface = sAntReceiver.claimInterface();
	        			 } else {
	                       		Log.i(TAG,"Not attempting to claim the ANT Interface as application was interrupted (leaving in previous state).");
	                       }
	        		 }
	        		 if (mClaimedAntInterface) {
	 	        		//connectAllChannels();
	        		 }
	        	 }
	        } catch(AntInterfaceException e) {
	        	 e.printStackTrace();
	        	 setChanged();
	 			 notifyObservers(ToolboxService.ANT_FAILURE);
	        }
	        	 //Log.d(TAG, "mAntServiceListener Displaying icons only if radio enabled");
	        	 //updateDisplay();
		 }
	        
	         
		 

	     public void onServiceDisconnected() {
	    	 Log.d(TAG, "mAntServiceListener onServiceDisconnected()");
	         mServiceConnected = false;
	         mEnabling = false;
	               
	         if(mClaimedAntInterface) {
	        	 receiveAntRxMessages(false);
	         }
	               
	               //updateDisplay();
	     }
	 };


	
    public ANTReader(Context context, List<Module> modulesToUse) {
    	super(context,modulesToUse);
    	  
    	//mModules = modulesToUse;
    	statusIntentFilter = new IntentFilter();
    	statusIntentFilter.addAction(AntInterfaceIntent.ANT_ENABLED_ACTION);
    	statusIntentFilter.addAction(AntInterfaceIntent.ANT_DISABLED_ACTION);
    	statusIntentFilter.addAction(AntInterfaceIntent.ANT_RESET_ACTION);
    	statusIntentFilter.addAction(AntInterfaceIntent.ANT_INTERFACE_CLAIMED_ACTION);
    
    	messageIntentFilter = new IntentFilter();
    	messageIntentFilter.addAction(AntInterfaceIntent.ANT_RX_MESSAGE_ACTION);
    	//mContext = context;
    	pair = new ValuesDirectInputNamePair(null,null);
    	if(AntInterface.hasAntSupport(mContext)) {
    		sAntReceiver = new AntInterface();
    	} else {
    		Log.e(TAG, "No ANT Support.");
    		setChanged();
 			notifyObservers(ToolboxService.ANT_NOT_SUPPORTED);
    	}
    	if (sAntReceiver == null) {
    		Log.e(TAG, "No ANT Service, quitting.");
    		setChanged();
 			notifyObservers(ToolboxService.ANT_SERVICE_NOT_FOUND);
    		
    		
    	} else {
    		Log.d(TAG, "ANTInterface object created succesfully ");
    		sAntReceiver.initService(mContext, mAntServiceListener);
    		mServiceConnected = sAntReceiver.isServiceConnected();
    		mContext.registerReceiver(mAntStatusReceiver, statusIntentFilter);
    	
    		if(mServiceConnected)
    		{
    			Log.d(TAG, "Connected with service succesfully");
    			try
    			{
    				mClaimedAntInterface = sAntReceiver.hasClaimedInterface();
    				if(mClaimedAntInterface)
    				{
    					receiveAntRxMessages(true);
    					Log.d(TAG, "Interface claimed succesfully");
    				}
    			}
    			catch (AntInterfaceException e)
    			{
                e.printStackTrace();
                setChanged();
	 			notifyObservers(ToolboxService.ANT_FAILURE);
    			}
    		}
    	}
    	
    	
     	mAntReader = ANTReader.this;
    }
    
    public ANTReader getInstance() {
    	return mAntReader;
    }
    
    /** Connect all ANT Sensors defined by modulesToUse
     * 
     */
    private boolean connectAllChannels() {
    	boolean val = false;
    	Iterator<Module> it = mModules.iterator();
    	ANTModule module = null;
    	while (it.hasNext()) {
    		module = (ANTModule)it.next();
    		
    		val = antChannelSetup(module.getNetworkNr(),
    				module.getChannelNr(), 
    				module.getDeviceNr(), 
    				module.getDeviceType(),
    				module.getTransmissionType(),
    				module.getChannelPeriod(),
    				module.getRfFrequency(),
    				module.getProximityTreshold());
    		if (val == false) { 
    			return val;
    		}
    	}
    	return true;
    }
    
    
    /**
     *  Receives all of the ANT status intents. 
     */
    private final BroadcastReceiver mAntStatusReceiver = new BroadcastReceiver() 
    {     
    	public void onReceive(Context context, Intent intent) 
        {
    		//byte[] networkKeyMsg = {0x11,0x76,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
    		String ANTAction = intent.getAction();
    		if (ANTAction.equals(AntInterfaceIntent.ANT_ENABLED_ACTION)) {
    			
    		} else if (ANTAction.equals(AntInterfaceIntent.ANT_DISABLED_ACTION)) {
    			
    		} else if (ANTAction.equals(AntInterfaceIntent.ANT_RESET_ACTION)) {
    		
    		} else if (ANTAction.equals(AntInterfaceIntent.ANT_INTERFACE_CLAIMED_ACTION)) {
    			boolean wasClaimed = mClaimedAntInterface;
    		//TODO Add Airplane mode status 	
    			try {
					mClaimedAntInterface = sAntReceiver.hasClaimedInterface();
					if (mClaimedAntInterface) {
						
						Log.i(TAG, "mAntStatusReceiver->onReceive: ANT Interface claimed");
						
						
	                    receiveAntRxMessages(true);
	                    //sAntReceiver.ANTTxMessage(networkKeyMsg);
	                    connectAllChannels(); 
	                    
					} else {
						if (wasClaimed) {
							Log.i(TAG, "mAntStatusReceiver->onReceive: ANT Interface released");

	                        receiveAntRxMessages(false);
	                        //mAntStateText.setText(R.string.Text_ANT_In_Use);
						}
					}
				} catch (AntInterfaceException e) {
					e.printStackTrace();
		    		setChanged();
		 			notifyObservers(ToolboxService.ANT_FAILURE);
				}
    			
    		}
        }
        
    };
    
    /** 
     * Receives all of the ANT message intents and dispatches to the proper handler.
     */
    private final BroadcastReceiver mAntMessageReceiver = new BroadcastReceiver() 
    {      
	    public void onReceive(Context context, Intent intent) 
	    {
	          
	    	String ANTAction = intent.getAction();
	         
	    	//Log.d(TAG, "enter onReceive: " + ANTAction);
	          if (ANTAction.equals(AntInterfaceIntent.ANT_RX_MESSAGE_ACTION)) {
	        	  
	        	 //Log.d(TAG, "mAntMessageReceiver->onReceive: ANT RX MESSAGE");
		          byte[] ANTRxMessage = intent.getByteArrayExtra(AntInterfaceIntent.ANT_MESSAGE);
		          
		          
		          //String text = "Rx:";
		          
		          /*
		          for(int i = 0;i < ANTRxMessage.length; i++)
		        	  text += "[" + Integer.toHexString((int) ANTRxMessage[i] & 0xFF) + "]";
		          
		          Log.d(TAG, text);
		          */
		          //There are more message types ANT can respond to. See the ANT+ demo project for an example
		          switch(ANTRxMessage[AntMesg.MESG_ID_OFFSET]) {
		          case AntMesg.MESG_ACKNOWLEDGED_DATA_ID:
		          case AntMesg.MESG_BROADCAST_DATA_ID:
		        	  decodeANTMessage(ANTRxMessage);
		        	  break;
		          case AntMesg.MESG_CHANNEL_ID_ID:
		        	  
		        	  //setDeviceNr(ANTRxMessage);
		        	  break;
		          case AntMesg.MESG_RESPONSE_EVENT_ID:
		        	  switch (ANTRxMessage[RESPONSE_ID_OFFSET]) {
		        	  case SET_NETWORK_KEY_MSG_128BIT:
		        		  //if (ANTRxMessage[RESPONSE_CODE_OFFSET] == RESPONSE_NO_ERROR ) {
			                    //connectAllChannels(); //TODO Only when using ANT devices with network keys. 
		        		  //}
		        	  }
		        	  /*
		        	  String text = "Rx:";
			            
			          for(int i = 0;i < ANTRxMessage.length; i++)
			        	  text += "[" + Integer.toHexString((int) ANTRxMessage[i] & 0xFF) + "]";
			          
			          Log.d(TAG, text);
			          */
		        	  break;
		         
		          default:
		        	  break;
		          }
	             
	             
	             
	             
	          }
	       }
    };
    /**
     * Decodes a byte array received over ANT. Based on the channel number of the message the correct module is selected
     * @param message Byte array received in a ANT message broadcast receiver.
     */
    private void decodeANTMessage(byte[] message) {
    	Iterator<Module> it = mModules.iterator();
   	 	ANTModule module = null;
   	 	
   	 	byte rxChannelNumber = (byte)(message[AntMesg.MESG_DATA_OFFSET] & AntDefine.CHANNEL_NUMBER_MASK);
   	 	while (it.hasNext()) {
   	 		module = (ANTModule)it.next();
   	 		if (module.getChannelNr() == rxChannelNumber) {
   	 			
   	 			/*
   	 			if (module.getDeviceNr() == 0) {
   	 				try {
   	 					sAntReceiver.ANTRequestMessage((byte) 0x00, AntMesg.MESG_CHANNEL_ID_ID);
   	 				} catch (AntInterfaceException e) {
   	 					// TODO Auto-generated catch block
   	 					e.printStackTrace();
   	 				}  
   	 			}
   	 			*/
   	 			mValues = module.decodeSensorData(message);
   	 			//module.setReady(true);
   	 			pair.setDirectInputName(module.getId());
   	 			pair.setValues(mValues);
   	 			if (mValues != null) {
   	 				//setChanged();
   	 				//notifyObservers(pair);
   	 				sendData(pair);
   	 			}
   	 			
   	 			
            	
            
   	 		}
   	 	}
    }
    
    private void setDeviceNr(byte[] message) {
    	
    	Iterator<Module> it = mModules.iterator();
   	 	ANTModule module = null;
   	 	short deviceNum = (short) ((message[AntMesg.MESG_DATA_OFFSET + 1]&0xFF | ((message[AntMesg.MESG_DATA_OFFSET + 2]&0xFF) << 8)) & 0xFFFF);
   	 	byte rxChannelNumber = (byte)(message[AntMesg.MESG_DATA_OFFSET] & AntDefine.CHANNEL_NUMBER_MASK);
   	 	while (it.hasNext()) {
	 		module = (ANTModule)it.next();
	 		if (module.getChannelNr() == rxChannelNumber) {
	 			//mModules.remove(module);
	 			module.setDeviceNr(deviceNum);
	 			//mModules.add(module);
	 		}
   	 	}
    }
    
    /**
     * Enable/disable receiving ANT Rx messages.
     *
     * @param register If want to register to receive the ANT Rx Messages
     */
    private void receiveAntRxMessages(boolean register)
    {
        if(register) {
        
            Log.i(TAG, "receiveAntRxMessages: START");
            
            mContext.registerReceiver(mAntMessageReceiver, messageIntentFilter);
            
            
        }
        else {
        
            try {
            
                mContext.unregisterReceiver(mAntMessageReceiver);
            }
            catch(IllegalArgumentException e) {
            
                // Receiver wasn't registered, ignore as that's what we wanted anyway
            }

            Log.i(TAG, "receiveAntRxMessages: STOP");
        }
    }
    
    public void shutDown()  {
    	
    	Iterator<Module> it = mModules.iterator();
    	ANTModule module = null;
    	while(it.hasNext()) {
    		module = (ANTModule)it.next();
    		
    		try {
    			sAntReceiver.ANTCloseChannel(module.getChannelNr());
    			sAntReceiver.ANTUnassignChannel(module.getChannelNr());
    			Log.d(TAG, "Channels closed");
    			module.setReady(false);
    		} catch(AntInterfaceException e) {
    			e.printStackTrace();
    		}
    		
    	}
    	
    	try {
        
            mContext.unregisterReceiver(mAntStatusReceiver);
        }
        catch(IllegalArgumentException e) {
        
            // Receiver wasn't registered, ignore as that's what we wanted anyway
        }
        
        receiveAntRxMessages(false);
        
        if(mServiceConnected) {
            try {
                if(mClaimedAntInterface) {
                    Log.d(TAG, "AntChannelManager.shutDown: Releasing interface");
                    sAntReceiver.releaseInterface();
                }
                sAntReceiver.stopRequestForceClaimInterface();
            }
            catch(AntServiceNotConnectedException e) {
                // Ignore as we are disconnecting the service/closing the app anyway
            }
            catch(AntInterfaceException e) {
               Log.w(TAG, "Exception in AntChannelManager.shutDown", e);
               e.printStackTrace();
               setChanged();
               notifyObservers(ToolboxService.ANT_FAILURE);
            }
        }
       
    }
    
   
	
	 /**
	     * ANT Channel Configuration.
	     *
	     * @param networkNumber the network number
	     * @param channelNumber the channel number
	     * @param deviceNumber the device number
	     * @param deviceType the device type
	     * @param txType the tx type
	     * @param channelPeriod the channel period
	     * @param radioFreq the radio freq
	     * @param proxSearch the prox search
	     * @return true, if successfully configured and opened channel
	     */   
	    private boolean antChannelSetup(byte networkNumber, byte channelNumber, short deviceNumber, byte deviceType, byte txType, short channelPeriod, byte radioFreq, byte proxSearch)
	    {
	       boolean channelOpen = false;
	       
	       
	       try
	       {
	    	   
	    	   //deviceNumber = convertToShort(deviceNumber);
	           sAntReceiver.ANTAssignChannel(channelNumber, AntDefine.PARAMETER_RX_NOT_TX, networkNumber);  // Assign as slave channel on selected network (0 = public, 1 = ANT+, 2 = ANTFS)
	           sAntReceiver.ANTSetChannelId(channelNumber, deviceNumber, deviceType, txType);
	           sAntReceiver.ANTSetChannelPeriod(channelNumber, channelPeriod);
	           sAntReceiver.ANTSetChannelRFFreq(channelNumber, radioFreq);
	           //sAntReceiver.ANTSetChannelTxPower(channelNumber, (byte) 3);
	           sAntReceiver.ANTSetChannelSearchTimeout(channelNumber, (byte) 0xFF); // 0xFF is infinite search timeout
	           //sAntReceiver.ANTSetLowPriorityChannelSearchTimeout(channelNumber,(byte) 0xFF); // Set search timeout to 30 seconds (low priority search)
	           
	           
	           if(deviceNumber == 0)
	           {
	               sAntReceiver.ANTSetProximitySearch(channelNumber, proxSearch);   // Configure proximity search, if using wild card search
	           }
	          
	           sAntReceiver.ANTOpenChannel(channelNumber);
	           
	           channelOpen = true;
	       }
	       catch(AntInterfaceException aie)
	       {
	    	   aie.printStackTrace();
	    	   setChanged();
               notifyObservers(ToolboxService.ANT_FAILURE);
	       }
	      
	       return channelOpen;
	    }

	/*
	@Override
	public ModuleClass getModuleById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getSupportedModules() {
		return mSupportedModules;
	}
	 */
}
