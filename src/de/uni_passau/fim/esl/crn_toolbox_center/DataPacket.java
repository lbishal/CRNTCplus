package de.uni_passau.fim.esl.crn_toolbox_center;

	/**
	 * This class is adapted from M.A.S.S. - Software Engineering practical 2009-2010
	 * 
	 * Represents the data received from a Context Recognition Network Toolbox for
	 * one channel combination. It contains also all other information a data
	 * visualization needs to visualize the channel combination.
	 * 
	 */
	public final class DataPacket {

	    private static final long serialVersionUID = 1L;

	    // the seconds part of the time stamp of the data
	    private final Long timestampSec;

	    // the microseconds part of the time stamp of the data
	    private final Long timestampUSec;

	    // the data
	    private final Double[] values;
	    
	    // ID string of the module that sent the packet
	    private final String ID;


	    /**
	     * Creates a data packet with the given parameters.
	     * 
	     * @param timestampSec
	     *            the seconds part of the received time stamp of the contained
	     *            data or <code>null</code> if it is invalid. Must be greater
	     *            than or equal to zero if not <code>null</code>.
	     * @param timestampUSec
	     *            the microseconds part of the received time stamp of the
	     *            contained data or <code>null</code> if it is invalid. Must be
	     *            greater than or equal to zero if not <code>null</code>.
	     * @param values
	     *            a Double[] which contains the received value of channel number
	     *            <code>i</code> at position <code>i</code>. A <code>null</code>
	     *            value for one position signals that invalid data was received
	     *            for the corresponding channel. The array can also be
	     *            <code>null</code> when the data is totally invalid.
	     * @param state
	     *            the current state of the channel combination. May not be
	     *            <code>null</code>.
	     */
	    public DataPacket(Long timestampSec, Long timestampUSec,
	            Double[] values, String id) {
	        if ((timestampSec != null) && (timestampSec < 0)) {
	            throw new IllegalArgumentException(
	                    "TimestampSec must be greater than or equal to zero or "
	                            + "null!");
	        }
	        if ((timestampUSec != null) && (timestampUSec < 0)) {
	            throw new IllegalArgumentException(
	                    "TimestampUSec must be greater than or equal to zero or "
	                            + "null!");
	        }
	        
	        this.timestampSec = timestampSec;
	        this.timestampUSec = timestampUSec;
	        this.values = values;
	        this.ID = id;
	    }


	    /**
	     * Returns the seconds part of the time stamp of the contained data or
	     * <code>null</code> if it is invalid.
	     * 
	     * @return the seconds part of the data time stamp
	     */
	    public Long getTimestampSec() {
	        return timestampSec;
	    }

	    /**
	     * Returns the microseconds part of the time stamp of the contained data or
	     * <code>null</code> if it is invalid.
	     * 
	     * @return the microseconds part of the data time stamp
	     */
	    public Long getTimestampUSec() {
	        return timestampUSec;
	    }

	    /**
	     * Returns the data in a Double[] which contains the data of channel number
	     * <code>i</code> at position <code>i</code>. A <code>null</code> value for
	     * one position signals that invalid data for the corresponding channel was
	     * received. A return value of <code>null</code> instead of an array signals
	     * that the data is totally invalid.
	     * 
	     * @return a Double[] containing the data or <code>null</code> if the data
	     *         is totally invalid
	     */
	    public Double[] getValues() {
	        return values;
	    }
	    
	    public String getID() {
	    	return ID;
	    }

	}
