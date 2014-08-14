package de.uni_passau.fim.esl.crn_toolbox_center.modules;



public class ANTPlusHRM extends ANTPlusModule{
	
	public ANTPlusHRM() {
		channelPeriod = 8070;
		deviceType = 0x78;
		
	}
	/**
     * The possible HRM page toggle bit states.
     */
    public enum HRMStatePage
    {
       /** Toggle bit is 0. */
       TOGGLE0,
       
       /** Toggle bit is 1. */
       TOGGLE1,
       
       /** Initialising (bit value not checked). */
       INIT,
       
       /** Extended pages are valid. */
       EXT
    }
	
	
	
	/** The current HRM page/toggle bit state. */
    private transient HRMStatePage mStateHRM = HRMStatePage.INIT;
	
	/** Last measured BPM form the HRM device */

	@Override
	public float[] decodeSensorData(byte[] value) {
		float[] values = new float[1];
		values[0] = 0;
		if(mStateHRM != HRMStatePage.EXT)
        {
           if(mStateHRM == HRMStatePage.INIT)
           {
              if((value[3] & (byte) 0x80) == 0)
                 mStateHRM = HRMStatePage.TOGGLE0;
              else
                 mStateHRM = HRMStatePage.TOGGLE1;
           }
           else if(mStateHRM == HRMStatePage.TOGGLE0)
           {
              if((value[3] & (byte) 0x80) != 0)
                 mStateHRM = HRMStatePage.EXT;
           }
           else if(mStateHRM == HRMStatePage.TOGGLE1)
           {
              if((value[3] & (byte) 0x80) == 0)
                 mStateHRM = HRMStatePage.EXT;
           }
        }
        
        // Heart rate available in all pages and regardless of toggle bit            
        values[0] = value[10] & 0xFF;
        return values;
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}

	
}
