package caceresenzo.apps.boxplay.utils;

import android.os.Bundle;

public interface Restorable {
	
	public void saveInstanceState(Bundle outState);
	
	public void restoreInstanceState(Bundle savedInstanceState);
	
}