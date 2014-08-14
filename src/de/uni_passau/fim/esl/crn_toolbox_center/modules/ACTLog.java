package de.uni_passau.fim.esl.crn_toolbox_center.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.uni_passau.fim.esl.crn_toolbox_center.DataPacket;
import de.uni_passau.fim.esl.crn_toolbox_center.R;

public class ACTLog extends GUIModule{
	
	public static final int SUCCEEDED = 0;
	public static final int ACTIVITY_EXISTS = 56;
	public static final int ACTIVITY_NOT_USER_CREATED = 57;
	public static final int ACTIVITY_NOT_FOUND = 58;
	public static final int ACTIVITY_IN_CATEGORY = 59;
	
	public class MyLastInfo {
		public String last_activity;
		public String last_category;
	}
	public class MyPreferences {
		public boolean feedback_message;		//REQUIRED: Gives a message when the user doesn't hold the selection properly
		public boolean reminder;				//REQUIRED: Gives the user a reminder message when there hasn't been any activity for some time
		public int reminder_time;				//REQUIRED: Specify the time it takes for a reminder message to appear
		public boolean add_activities;			//REQUIRED: Let the user add activities
		public boolean delete_activities;		//REQUIRED: Let the user delete activities that he/she added
		public int feedback_message_duration;	//REQUIRED: The time it takes for a feedback message to disappear
		public boolean reminder_type_sound;		//REQUIRED: Make sound to remind the user to annotate
		public boolean reminder_type_light;		//REQUIRED: Flash light to remind the user to annotate
		public boolean reminder_type_vibration;	//REQUIRED: Vibrate phone to remind the user to annotate
	}
	public class MyActivity {
		public String original_categories[];	//REQUIRED: List of original categories this activity is part of
		public String new_categories[];			//REQUIRED: List of user created categories this activity is part of
		public int number;						//REQUIRED: Unique number assigned to this activity
		public boolean user_activity;			//REQUIRED: This activity is created by the user
		public String name;						//REQUIRED: Name of this activity that is displayed on the screen
	}
	private MyLastInfo last_info;
	private MyPreferences preferences;
	private MyActivity activities[];
		
	public ACTLog() {
		moduleReady = true; //ACTLog module is always ready.
		mTabNameID = R.string.actlog_tabname;
		mDrawableID = R.drawable.actlog2;
		
	}
	

	@Override
	public String getId() {
		
		return id;
	}

	public CharSequence getLastCategory() {
		
		return last_info.last_category;
	}

	public CharSequence getLastActivity() {
		
		return last_info.last_activity;
	}
	
	public String[] getCategoriesString() {
		Set<String> categories = new TreeSet<String>();
		for (int i = 0; i < activities.length; i++) {
			for (int j = 0; j < activities[i].original_categories.length; j++) {
				categories.add(activities[i].original_categories[j]);
			}
			for (int j = 0; j < activities[i].new_categories.length; j++) {
				categories.add(activities[i].original_categories[j]);
			}
		}
		
		return categories.toArray(new String[categories.size()]);
	}

	public boolean getReminder() {
		
		return preferences.reminder;
	}

	public boolean getReminderTypeLight() {
		
		return preferences.reminder_type_light;
	}

	public boolean getReminderTypeSound() {
		
		return preferences.reminder_type_sound;
	}

	public boolean getReminderTypeVibrate() {
		
		return preferences.reminder_type_vibration;
	}

	public int getReminderTime() {
		
		return preferences.reminder_time;
	}

	public boolean getFeedbackMessage() {
		
		return preferences.feedback_message;
	}

	public int getFeedbackDuration() {
		
		return preferences.feedback_message_duration;
	}

	public List<String> getActivityIntersectionList(String currCat) {
		
		Set<String> strSet = new TreeSet<String>();
		for (int i = 0; i < activities.length; i++) {
			for (int j =0; j < activities[i].original_categories.length; j++) {
				if (activities[i].original_categories[j].equals(currCat)) {
					strSet.add(activities[i].name);
				}
			}
			for (int j =0; j < activities[i].new_categories.length; j++) {
				if (activities[i].new_categories[j].equals(currCat)) {
					strSet.add(activities[i].name);
				}
			}
		}
		List<String> strList = new LinkedList<String>(strSet);
		return strList;
	}

	public List<String> getUserActivityStringList() {
		Set<String> strSet = new TreeSet<String>();
		for (int i = 0; i < activities.length; i++) {
			if (activities[i].user_activity) {
				strSet.add(activities[i].name);
			}
		}
		List<String> strList = new LinkedList<String>(strSet);
		return strList;
	}

	public boolean getAddActivities() {
		
		return preferences.add_activities;
	}

	public boolean getDeleteActivities() {
		
		return preferences.delete_activities;
	}

	public MyActivity getActivityByName(String selectedActStr) {
		for (int i = 0; i < activities.length; i++) {
			if (activities[i].name.equals(selectedActStr)) {
				return activities[i];
			}
		}
		return null;
	}
	
	public int getActivityNrByName(String selectedActStr) {
		for (int i = 0; i < activities.length; i++) {
			if (activities[i].name.equals(selectedActStr)) {
				return activities[i].number;
			}
		}
		return -1;
	}

	public void setLastCategory(String currCat) {
		last_info.last_category = currCat;
		
	}

	public void setLastActivity(String selectedActStr) {
		last_info.last_activity = selectedActStr;
		
	}

	public int deleteActivity(String itemAtPosition) {
		List<MyActivity> activitiesList = new ArrayList<MyActivity>(Arrays.asList(activities));
		Iterator<MyActivity> it = activitiesList.iterator();
		MyActivity tmpAct = null;
		while (it.hasNext()) {
			tmpAct = it.next();
			if (tmpAct.name.equals(itemAtPosition)) {
				activitiesList.remove(tmpAct);
				activities = (MyActivity[])activitiesList.toArray(new MyActivity[activitiesList.size()]);
				return SUCCEEDED;
			}
		}
		return ACTIVITY_NOT_FOUND;
					
		
	}

	public int changeActivityCategoryByName(String selectedActStr,
			String currCat) {
		List<MyActivity> activitiesList = Arrays.asList(activities);
		Iterator<MyActivity> it = activitiesList.iterator();
		MyActivity tmpAct = null;
		while (it.hasNext()) {
			tmpAct = it.next();
			if (tmpAct.name.equals(selectedActStr)) {
				List<String> categoriesList = new ArrayList<String>(Arrays.asList(tmpAct.new_categories));
				if (categoriesList.contains(currCat)) {
					return ACTIVITY_IN_CATEGORY;
				} else {
					categoriesList.add(currCat);
					tmpAct.new_categories = (String[])categoriesList.toArray(new String[categoriesList.size()]);
					return SUCCEEDED;
				}
			} 
		}
		return ACTIVITY_NOT_FOUND;
	}

	public List<String> getActivityStringList() {
		Set<String> strSet = new TreeSet<String>();
		for (int i = 0; i < activities.length; i++) {
			
				strSet.add(activities[i].name);
			
		}
		List<String> strList = new LinkedList<String>(strSet);
		return strList;
	}

	private int generateIndex() {
		int max = -1000;
		for (int i = 0; i < activities.length; i++) {
			if (max < activities[i].number) {
				max = activities[i].number;
			}
		}
		max++;
		return max;
	}
	
	public int getNumberByName(String name) {
		for (int i =0; i < activities.length; i++) {
			if (activities[i].name.equals(name)) {
				return activities[i].number;
			}
		}
		return -1;
	}

	public int addActivity(String name, String categories[], String newCategories[], boolean userCreated) {
		
		for (int i = 0; i < activities.length; i++) {
			if (activities[i].name.equals(name)) {
				return ACTIVITY_EXISTS;
			}
		
		}
		
		List<MyActivity> activitiesList = new ArrayList<MyActivity>(Arrays.asList(activities));
		MyActivity tmpActivity = new MyActivity();
		tmpActivity.name = name;
		tmpActivity.number = generateIndex();
		tmpActivity.original_categories = categories;
		tmpActivity.new_categories = newCategories;
		tmpActivity.user_activity = userCreated;
				
		activitiesList.add(tmpActivity);
		activities = (MyActivity[])(activitiesList.toArray(new MyActivity[activitiesList.size()]));
		return SUCCEEDED;
		
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public float[] processDataToFloat(float[] data) {
		return data;
	}


	@Override
	public DataPacket processDataToDataPacket(float[] data) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
