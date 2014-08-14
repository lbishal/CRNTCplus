package de.uni_passau.fim.esl.crn_toolbox_center;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class OnAlarmReceiver extends BroadcastReceiver {
	private static final int REMINDER_ID = 1;
	@Override
	public void onReceive(Context context, Intent intent) {
		String ns = Context.NOTIFICATION_SERVICE;
		int icon = R.drawable.icon;
		CharSequence tickerText = context.getString(R.string.ticker_text_notification);
		CharSequence contentText = context.getString(R.string.content_text_notification);
		CharSequence contentTitle = context.getString(R.string.app_name);
		long when = System.currentTimeMillis();
		Bundle bundle; 
		
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns); 
		Notification reminderNotification = new Notification(icon,tickerText,when);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
		bundle = intent.getExtras();
		
		if (bundle.getBoolean("light")) {
			reminderNotification.defaults |= Notification.DEFAULT_LIGHTS;
			reminderNotification.flags |= Notification.FLAG_SHOW_LIGHTS;
		}
		if (bundle.getBoolean("sound")) {
			reminderNotification.defaults |= Notification.DEFAULT_SOUND;
		}
		if (bundle.getBoolean("vibrate")) {
			reminderNotification.defaults |= Notification.DEFAULT_VIBRATE;
		}
		reminderNotification.flags |= Notification.FLAG_AUTO_CANCEL;
		reminderNotification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		mNotificationManager.notify(REMINDER_ID, reminderNotification);
		

	}

}
