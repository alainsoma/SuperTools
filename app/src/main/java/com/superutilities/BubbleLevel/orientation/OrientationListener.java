package com.superutilities.BubbleLevel.orientation;

public interface OrientationListener {

	public void onOrientationChanged(Orientation orientation, float pitch, float roll, float balance);
	
	public void onCalibrationSaved(boolean success);
	
	public void onCalibrationReset(boolean success);
	
}
