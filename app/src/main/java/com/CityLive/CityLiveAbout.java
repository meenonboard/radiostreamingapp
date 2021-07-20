package com.CityLive;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class CityLiveAbout extends Activity{
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);    
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		menu.add("Back");
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem menuitem){
		if (menuitem.getTitle().equals("Back")){
			this.finish();
    	}
		return true;
	}  
}
