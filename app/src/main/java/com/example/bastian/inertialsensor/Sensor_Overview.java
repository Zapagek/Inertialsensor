package com.example.bastian.inertialsensor;

/**
 * Created by WdM on 14.12.2015.
 */

        import java.util.List;
        import android.content.Context;
        import android.hardware.Sensor;
        import android.hardware.SensorManager;
        import android.os.Bundle;
        import android.support.v7.app.AppCompatActivity;
        import android.widget.ArrayAdapter;
        import android.widget.ListView;

public class Sensor_Overview extends AppCompatActivity {

    SensorManager allsensors;
    List<Sensor> sensor;
    ListView sensorliste;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_overview);

        allsensors = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorliste = (ListView) findViewById (R.id.listview1);
        sensor = allsensors.getSensorList(Sensor.TYPE_ALL);
        sensorliste.setAdapter(new ArrayAdapter<Sensor>(this, android.R.layout.simple_list_item_1,  sensor));
    }
}