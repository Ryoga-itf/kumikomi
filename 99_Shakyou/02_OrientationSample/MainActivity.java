package com.example.orientationsample;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private CheckBox mCheckBoxOrientation;
    private TextView mAzimuthText, mPitchText, mRollText;
    private float[] mAccelerationValue = new float[3];
    private float[] mGeoMagneticValue = new float[3];
    private float[] mOrientationValue = new float[3];
    private float[] mInRotationMatrix = new float[9];
    private float[] mOutRotationMatrix = new float[9];
    private float[] mInclinationMatrix = new float[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_UI);
        mAzimuthText = (TextView) findViewById(R.id.text_view_azimuth);
        mRollText = (TextView) findViewById(R.id.text_view_roll);
        mPitchText = (TextView) findViewById(R.id.text_view_pitch);
        Button buttonOrientation = (Button) findViewById(R.id.button_orientation);
        buttonOrientation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!mCheckBoxOrientation.isChecked()) {
                    SensorManager.getRotationMatrix(mInRotationMatrix, mInclinationMatrix, mAccelerationValue, mGeoMagneticValue);
                    SensorManager.remapCoordinateSystem(mInRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, mOutRotationMatrix);
                    SensorManager.getOrientation(mOutRotationMatrix, mOrientationValue);
                    String azimuthText = String.valueOf(Math.floor(Math.toDegrees((double) mOrientationValue[0])));
                    String pitchText = String.valueOf(Math.floor(Math.toDegrees((double) mOrientationValue[1])));
                    String rollText = String.valueOf(Math.floor(Math.toDegrees((double) mOrientationValue[2])));
                    mAzimuthText.setText(azimuthText);
                    mPitchText.setText(pitchText);
                    mRollText.setText(rollText);
                }
            }
        });
        mCheckBoxOrientation = (CheckBox) findViewById(R.id.checkbox_orientation);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:mGeoMagneticValue = sensorEvent.values.clone();break;
            case Sensor.TYPE_ACCELEROMETER:mAccelerationValue = sensorEvent.values.clone();break;
        }
        if (mCheckBoxOrientation.isChecked()) {
            SensorManager.getRotationMatrix(mInRotationMatrix, mInclinationMatrix, mAccelerationValue, mGeoMagneticValue);
            SensorManager.remapCoordinateSystem(mInRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, mOutRotationMatrix);
            SensorManager.getOrientation(mOutRotationMatrix, mOrientationValue);
            String azimuthText = String.valueOf(Math.floor(Math.toDegrees((double) mOrientationValue[0])));
            String pitchText = String.valueOf(Math.floor(Math.toDegrees((double) mOrientationValue[1])));
            String rollText = String.valueOf(Math.floor(Math.toDegrees((double) mOrientationValue[2])));
            mAzimuthText.setText(azimuthText);
            mPitchText.setText(pitchText);
            mRollText.setText(rollText);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}