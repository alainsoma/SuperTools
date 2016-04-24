package com.superutilities.BubbleLevel;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.superutilities.R;

public class FragmentBubbleLevel extends Fragment implements SensorEventListener {

    private OnFragmentInteractionListener mListener;
    TextView textView;
    private Sensor sensor;
    View view;

    //Bubble Level
    private double highAccel, totalAccel;
    private SensorManager mSensorManager;
    //private static Context CONTEXT;
    private Board board;

    public FragmentBubbleLevel() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.bubblelevel_horizontal_layout, container, false);

        sensorInitialize();

        textView = (TextView)view.findViewById(R.id.textView2);

        return  view;
    }

    private void sensorInitialize(){
        //board = new Board(getActivity());
        board = (Board)view.findViewById(R.id.viewBubbleLevel);
        //getActivity().setContentView(board);
        //CONTEXT = getContext();
        highAccel = 0;
        totalAccel = 0;
        this.mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //mSensorManager.connectSimulator();
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);

    }

    public void onStart() {
        super.onStart();
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void onResume() {
        super.onResume();
        this.mSensorManager.registerListener(this, this.sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void onPause() {
        super.onPause();
        this.mSensorManager.unregisterListener(this);
    }

    public void onStop() {
        super.onStop();
        this.mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){

            this.showSensorValues(event);
        }
    }


    private void showSensorValues(SensorEvent event){

        board.getBubble().setY(215+(int)(event.values[1]*1.194));
        board.invalidate();
        float x,y,z;
        x = event.values[0];
        y = event.values[1];
        z = event.values[2];

        textView.setText("\n" + "El valor de X: " + x + "\n" + "El valor de Y: " + y + "\n" + "El valor de Z: " + z);
        Log.d("SensorChangeEvent", "SENSORCHANGED: " + textView.getText().toString());

        board.invalidate();



    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
