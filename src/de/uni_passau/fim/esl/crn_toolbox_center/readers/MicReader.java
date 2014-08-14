package de.uni_passau.fim.esl.crn_toolbox_center.readers;

import java.util.List;

import android.content.Context;
import android.media.AudioRecord;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.Module;

public class MicReader extends ReaderClass{
	public static final String[] mSupportedModules = {"SmartphoneMic"};
	private AudioRecord mAudioRecorder;
	
	public MicReader(Context context, List<Module> modulesToUse) {
		super(context, modulesToUse);
		// TODO Auto-generated constructor stub
	}

	private class AudioRecordListener implements OnRecordPositionUpdateListener {

		public void onMarkerReached(AudioRecord recorder) {
			// TODO Auto-generated method stub
			
		}

		public void onPeriodicNotification(AudioRecord recorder) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}

}
