package de.uni_passau.fim.esl.crn_toolbox_center;

import android.os.Parcel;
import android.os.Parcelable;

public class MyActivity implements Parcelable {
	public String name;
	public int number;
	public int nCategories;
	public int nNew;
	public String[] categories;
	public String[] new_categories;
	public boolean user_created;
	//public int probability; 
	
	public MyActivity(Parcel in) {
		this();
		readFromParcel(in);
	}
	
	public int describeContents() {
        return 0;
    }
	
	public void writeToParcel(Parcel out, int flags) {
        boolean[] boolArray = new boolean[1];
		out.writeString(name);
		out.writeInt(number);
		out.writeInt(nCategories);
		out.writeInt(nNew);
        out.writeStringArray(categories);
        out.writeStringArray(new_categories);
        boolArray[0] = user_created;
        out.writeBooleanArray(boolArray);
        //out.writeInt(probability);
    }

	private void readFromParcel(Parcel in) {
		boolean[] boolArray = new boolean[1];
		name = in.readString();
		number = in.readInt();
		nCategories = in.readInt();
		nNew = in.readInt();
		categories = new String[nCategories];
		new_categories = new String[nNew];
		in.readStringArray(categories);
		in.readStringArray(new_categories);
		in.readBooleanArray(boolArray);
		user_created = boolArray[0];
		//probability = in.readInt();		
	}
	 public static final Parcelable.Creator<MyActivity> CREATOR =
	    	new Parcelable.Creator<MyActivity>() {
	            public MyActivity createFromParcel(Parcel in) {
	                return new MyActivity(in);
	            }
	 
	            public MyActivity[] newArray(int size) {
	                return new MyActivity[size];
	            }
	        };
	
	public MyActivity() {
		name = "";
		user_created = false;
		//probability = 100;
		nCategories = 0;
		nNew = 0;
	}
	public MyActivity(String name,int number ,String[] categories, String[] new_categories, boolean user, int probability) {
		this.name = name;
		this.number = number;
		this.categories = categories;
		this.new_categories = new_categories;
		this.user_created = user;
		//this.probability = probability; 
		nCategories = categories.length;
		nNew = new_categories.length;
		
	}
}
