package com.example.bastian.inertialsensor;



import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Sensorliste extends AppCompatActivity implements SensorEventListener {
    TextView accvalueInfo, rotaccvalueInfo, gyrovalueInfo, dcmvalue;

    private SensorManager mySensorManager;
    private Sensor myAccelerometer, myRotAccelerometer;


    private long startTime = 0, endTime = 0;

    private int corrGyroDrift = 0;
    private double[] sumGyro;
    private double[] driftGyro;
    private int k;
    private double T;

    private double[] c_b_r;
    private double[] c_b_r_k1;

    private double[] f_b_ib;
    private double[] w_b_ib;
    private double[] f_r_ib;
    private double[][] dcm;

    {
        c_b_r = new double[9];
        c_b_r_k1 = new double[9];

        f_r_ib = new double[3];
        f_b_ib = new double[3];
        w_b_ib = new double [3];
        sumGyro = new double[3];
        driftGyro = new double[3];

        dcm = new double[3][3];


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensorliste);

        // connect to xml layout
        accvalueInfo = (TextView) findViewById(R.id.accvalueInfo);
        rotaccvalueInfo = (TextView) findViewById(R.id.rotaccvalueInfo);
        gyrovalueInfo = (TextView) findViewById(R.id.gyrovalueInfo);
        dcmvalue = (TextView) findViewById(R.id.dcmvalue);

        // create SensorManager and get default sensors
        mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        myAccelerometer = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        myRotAccelerometer = mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // put sth in the textView
        accvalueInfo.setText("Hallo");
        rotaccvalueInfo.setText("Ich rotiere");
        gyrovalueInfo.setText("Ich noch mehr");


    }

    @Override
    protected void onResume(){
        super.onResume();
        //register Sensor Listener on startup
        mySensorManager.registerListener(this, myAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        mySensorManager.registerListener(this, myRotAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause(){
        super.onPause();
        // unregister listener saves energy
        mySensorManager.unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // this method is called on every change of ACCELEROMETER AND GYROSCOPE data
        // first


        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            //do sth with linear accelerometer data
            f_b_ib[0] = event.values[0];
            f_b_ib[1] = event.values[1];
            f_b_ib[2] = event.values[2];

            accvalueInfo.setText("[" + String.format("%.02f", f_b_ib[0]) + " " + String.format("%.02f", f_b_ib[1]) + " " + String.format("%.02f", f_b_ib[2]) +" " + "]");
        }

        f_r_ib = Navigation.rotateVectorDCM(c_b_r,f_b_ib);

        rotaccvalueInfo.setText("[" + String.format("%.02f", f_r_ib[0]) + " " + String.format("%.02f", f_r_ib[1]) + " " + String.format("%.02f", f_r_ib[2]) +" " + "]");


        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            //do sth with gyro data
            w_b_ib[0] = event.values[0] - driftGyro[0];
            w_b_ib[1] = event.values[1]- driftGyro[1];
            w_b_ib[2] = event.values[2] - driftGyro[2];

            //Werte des Gyroskops ausgeben

            gyrovalueInfo.setText("[" + String.format("%.02f",f_b_ib[0]) +" " + String.format("%.02f",f_b_ib[1]) +" " + String.format("%.02f",f_b_ib[2]) +" " + "]");

            if (corrGyroDrift ==1 &&  k<200){
                sumGyro[0] += w_b_ib[0];
                sumGyro[1] += w_b_ib[1];
                sumGyro[2] += w_b_ib[2];

                k++;

            }else if (k>=200) {
                corrGyroDrift = 0;

                driftGyro[0] = sumGyro[0] /100.0;
                driftGyro[1] = sumGyro[1] /100.0;
                driftGyro[2] = sumGyro[2] /100.0;

                initDCM();

                Toast.makeText(getApplicationContext(),"Drift berechnet", Toast.LENGTH_SHORT).show();
            }


            //Zeit zwischen zwei Aufrufen
            endTime = System.currentTimeMillis();
            T = (endTime - startTime) /1000.0; //Taktdauer in Sekunden
            startTime = endTime;

            //Update der RKM
            c_b_r_k1 = Navigation.updateDCM(c_b_r_k1,c_b_r,w_b_ib,T);

            //Ausgabe
            dcmvalue.setText("[" + String.format("%.02f",c_b_r[0]) +" " + String.format("%.02f",c_b_r[1]) +" " + String.format("%.02f",c_b_r[2]) +" " + "]\n" + "[" + String.format("%.02f",c_b_r[3]) +" " + String.format("%.02f",c_b_r[4]) +" " + String.format("%.02f",c_b_r[5]) +" " + "]\n" + "[" + String.format("%.02f",c_b_r[6]) +" " + String.format("%.02f",c_b_r[7]) +" " + String.format("%.02f",c_b_r[8]) +" " + "]");










        }
    }

    private void initDCM() {
        for (int i = 0; i < c_b_r.length; i++) {
            c_b_r[i] = 0;
            c_b_r_k1[i] = 0;

        }

        c_b_r[0] = 1;
        c_b_r[4] = 1;
        c_b_r[8] = 1;


        c_b_r_k1[0] = 1;
        c_b_r_k1[4] = 1;
        c_b_r_k1[8] = 1;

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void ClickBtnCal(View view) {
        //Kalibrierung hinzufügen
        Toast.makeText(getApplicationContext(),"Kalibriere, nicht bewegen !!!", Toast.LENGTH_SHORT).show();

        //reset drift and sum

        for (int i=0; i<driftGyro.length; i++){
            driftGyro[i]= 0;
            sumGyro[i] = 0;
        }

        corrGyroDrift = 1;
    }

    public void ClickBtnShow(View view) {


        Intent intent = new Intent(this,Sensor_Overview.class);


        startActivity(intent);

    }
}
