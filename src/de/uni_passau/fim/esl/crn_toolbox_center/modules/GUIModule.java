package de.uni_passau.fim.esl.crn_toolbox_center.modules;

public abstract class GUIModule extends OutputModule{
	protected transient int mTabNameID;
	protected transient int mDrawableID;
	protected transient String mTitleName;
	protected String tabName;				//OPTIONAL: Name of the tab that should be displayed
	
	
	public int getTabNameID() {
		return mTabNameID;
	}
	
	public int getDrawableID() {
		return mDrawableID;
	}
	
	public String getTabName() {
		return tabName;
	}
}
