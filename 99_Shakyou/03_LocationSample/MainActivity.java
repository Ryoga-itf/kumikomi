import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Insets;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.locationsample.R;

public class MainActivity extends AppCompatActivity implements LocationListener, View.OnClickListener {
    private LocationManager mLocationManager;
    private TextView mWifiLatitudeTextView;
    private TextView mWifiLongitudeTextView;
    private TextView mWifiAccuracyTextView;
    private TextView mWifiAltitudeTextView;
    private TextView mGpsLatitudeTextView;
    private TextView mGpsLongitudeTextView;
    private TextView mGpsAccuracyTextView;
    private TextView mGpsAltitudeTextView;
    private static int WIFI = 0;
    private static int GPS = 1;
    private int mLocationType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mWifiLatitudeTextView = (TextView) findViewById(R.id.text_view_wifi_latitude_value);
        mWifiLongitudeTextView = (TextView) findViewById(R.id.text_view_wifi_longitude_value);
        mWifiAccuracyTextView = (TextView) findViewById(R.id.text_view_wifi_accuracy_value);
        mWifiAltitudeTextView = (TextView) findViewById(R.id.text_view_wifi_altitude_value);
        mGpsLatitudeTextView = (TextView) findViewById(R.id.text_view_gps_latitude_value);
        mGpsLongitudeTextView = (TextView) findViewById(R.id.text_view_gps_longitude_value);
        mGpsAccuracyTextView = (TextView) findViewById(R.id.text_view_gps_accuracy_value);
        mGpsAltitudeTextView = (TextView) findViewById(R.id.text_view_gps_altitude_value);
        Button gpsButton = (Button) findViewById(R.id.button_gps); gpsButton.setOnClickListener(this);
        Button wifiButton = (Button) findViewById(R.id.button_wifi);wifiButton.setOnClickListener(this);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onResume() {
        Log.d("PlaceSample", "plog onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("PlaceSample", "plog onPause");
        if (mLocationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLocationManager.removeUpdates(this);
        }
        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("PlaceSample", "plog onLocationChanged");
        if (mLocationType == GPS) {
            mGpsLatitudeTextView.setText(String.valueOf(location.getLatitude()));
            mGpsLongitudeTextView.setText(String.valueOf(location.getLongitude()));
            mGpsAccuracyTextView.setText(String.valueOf(location.getAccuracy()));
            mGpsAltitudeTextView.setText(String.valueOf(location.getAltitude()));
        } else if (mLocationType == WIFI) {
            mWifiLatitudeTextView.setText(String.valueOf(location.getLatitude()));
            mWifiLongitudeTextView.setText(String.valueOf(location.getLongitude()));
            mWifiAccuracyTextView.setText(String.valueOf(location.getAccuracy()));
            mWifiAltitudeTextView.setText(String.valueOf(location.getAltitude()));
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String s, int status, Bundle bundle) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                Log.v("PlaceSample", "AVAILABLE");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.v("PlaceSample", "OUT_OF_SERVICE");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.v("PlaceSample", "TEMPORARILY_UNAVAILABLE");
                break;
        }
    }

    @Override
    public void onProviderEnabled(String s) {}

    @Override
    public void onProviderDisabled(String s) {}

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_gps) {
            mLocationType = GPS;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } else if (view.getId() == R.id.button_wifi) {
            mLocationType = WIFI;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }
    }
}