package de.uni_passau.fim.esl.crn_toolbox_center;


/**
 * Representing a object which is notified to observers containing the
 * notify reason and the notify data.
 */
public class NotifyObject {

	/**
     * An enum containing possible notification reasons.
     */
    public enum NotifyReason {
        SET_CONFIG_FILE_PATH, READER_READY, SET_TOOLBOX_INFO, READER_ERROR, ALL_READERS_READY,
        SET_TAB, SET_CONTROL_PORT, SET_SENSOR_READING_RATE_ID, TOOLBOX_RUNNING, DIRECT_INPUT_RUNNING,
        TOOLBOX_START_STOP, DIRECT_INPUT_START_STOP, SHOW_TOOLBOX_LOG, ACTIVITY_FINISH, CONFIG_FILE_READ, 
        CONFIG_FILE_ERROR, ACTLOG_READ, SD_READONLY, SD_NOREAD_NOWRITE, LOG_FILE_READ, LOG_FILE_ERROR,OUTPUT_RUNNING,
        OUTPUT_START_STOP, TOOLBOX_SERVICE_ERROR, OUTPUT_STOPPED, TOAST_MESSAGE, BT_SENSOR_DATA
    }

	
    private NotifyReason mNotifyReason;
    private Object mData;

    /**
     * Creates a new NotifyObject.
     * 
     * @param notifyReason
     *            The notify reason
     * @param data
     *            The data to notify
     */
    public NotifyObject(NotifyReason notifyReason, Object data) {
        super();
        this.mNotifyReason = notifyReason;
        this.mData = data;
    }

    /**
     * Gets the notify reason.
     * 
     * @return The notify reason
     */
    public NotifyReason getNotifyReason() {
        return mNotifyReason;
    }

    /**
     * Gets the notify data.
     * 
     * @return The notify data
     */
    public Object getData() {
        return mData;
    }

}
