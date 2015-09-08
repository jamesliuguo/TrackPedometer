package com.zhenqianfan13354468.trackpedometer;

import java.util.HashMap;
import java.util.Iterator;

import com.zhenqianfan13354468.trackpedometer.ChartView.Mstyle;

import android.R.integer;
import android.app.Service;
import android.content.Intent;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

public class LightSensorService extends Service implements SensorEventListener {
	private static final String TAG = LightSensorService.class.getSimpleName();
	// private static final String TAG_STRING = "LightSensorService";

	private SensorManager lightSM;
	private Sensor lightSensor;

	public static float LIGHT = 100000;
	public static boolean isInPocket = false;
	private ChartView cvLight;
	private static HashMap<Double, Double> map;

	int lightNum;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.i(TAG, "onCreate");
		new Thread(new Runnable() {
			public void run() {
				Log.i(TAG, "subThread run");
				initLightSensor();
			}
		}).start();
		cvLight = TabFragmentStep.cvLight;
		lightNum = 0;
		map = new HashMap<Double, Double>();
		map.put((double) (0), (double) 0);
		cvLight.SetTuView(map, 500, 25, "x", "y", false);
		cvLight.setMargint(30);
		cvLight.setMarginb(50);
		cvLight.setMstyle(Mstyle.Line);
		// cvLight.draw(canvas);

	}

	protected void initLightSensor() {
		lightSM = (SensorManager) getSystemService(SENSOR_SERVICE);

		lightSensor = lightSM.getDefaultSensor(Sensor.TYPE_LIGHT);

		lightSM.registerListener(this, lightSensor,
				SensorManager.SENSOR_DELAY_GAME);
	}

	public static Canvas canvas = new Canvas();

	@SuppressWarnings("rawtypes")
	@Override
	public void onSensorChanged(SensorEvent event) {
		Log.i(TAG, "onSensorChanged");

		Sensor sensor = event.sensor;
		synchronized (this) {
			if (sensor.getType() == Sensor.TYPE_LIGHT) {
				LIGHT = event.values[0];
				TabFragmentStep.tvLight.setText("感光度变化(Lus-Time)  "
						+ LightSensorService.LIGHT);
				Log.i(TAG, LIGHT + "");

				if (LIGHT < TabFragmentStep.LIGHT_BORDER) {
					isInPocket = true;
				} else {
					isInPocket = false;
				}


				if (lightNum < 21) {
					map.put((double) lightNum, (double) LIGHT);
					lightNum++;
				} else {
					for (int i = 0; i < lightNum - 1; ++i) {
						map.put((double) i, map.get((double) (i + 1)));
					}
					map.put((double) (lightNum - 1), (double) LIGHT);
				}

				//动态调整y轴坐标
				double mValue = 0;
				for (int i = 0; i < lightNum; ++i) {
					if (map.get((double) i) > mValue) {
						mValue = map.get((double) i);
					}
				}
				if (mValue <= 10) {
					cvLight.setTotalvalue(50);
					cvLight.setPjvalue(5);
				} else {
					cvLight.setTotalvalue((int) (mValue + 10));
					cvLight.setPjvalue((int) (mValue + 10) / 10);
				}
				cvLight.postInvalidate();// nn的找这个函数找了我那么久
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		Log.i(TAG, "onAccuracyChanged");
		// TODO Auto-generated method stub

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		super.onDestroy();
		if (lightSM != null) {
			lightSM.unregisterListener(this);
		}

	}

}
