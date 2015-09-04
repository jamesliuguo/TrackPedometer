package com.zhenqianfan13354468.trackpedometer;

import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

public class LightSensorService extends Service implements SensorEventListener {

	// Light 传感器
	private SensorManager lightSM;
	Sensor lightSensor;

	private static final int LIGHT_BORDER = 10;
	public static boolean isInPocket;
	public static float light = 100000;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d("tag", "service create");
		new Thread(new Runnable() {
			public void run() {
				initLightSensor();
			}
		}).start();
	}

	protected void initLightSensor() {
		isInPocket = false;

		// 获取系统服务（SENSOR_SERVICE)返回一个SensorManager 传感器管理器对象
		lightSM = (SensorManager) getSystemService(SENSOR_SERVICE);
		// 通过SensorManager对象获取相应的传感器
		lightSensor = lightSM.getDefaultSensor(Sensor.TYPE_LIGHT);
		// 注册相应的SensorService
		// 更新速率还有SENSOR_DELAY_UI、SENSOR_DELAY_FASTEST、SENSOR_DELAY_GAME等，
		// 根据不同应用，需要的反应速率不同，具体根据实际情况设定
		lightSM.registerListener(this, lightSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		Sensor sensor = event.sensor;
		synchronized (this) {
			if (sensor.getType() == Sensor.TYPE_LIGHT) {
				light = event.values[0];
				if (light < LIGHT_BORDER) {
					isInPocket = true;
				} else {
					isInPocket = false;
				}
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (lightSM != null) {
			lightSM.unregisterListener(this);
		}

	}

}
