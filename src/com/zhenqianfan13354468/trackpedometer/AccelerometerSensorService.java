package com.zhenqianfan13354468.trackpedometer;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;

/**
 * 加速传感器的service
 *
 */
public class AccelerometerSensorService extends Service {

	private SensorManager accelerometerSM;
	private AccelerometerSensorListener accelerometerSD;
	Sensor accelerometerSensor;

	public static boolean isRun;// service是否在运行

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		new Thread(new Runnable() {
			public void run() {
				initAccelerometerSensor();
			}
		}).start();
	}

	private void initAccelerometerSensor() {
		isRun = false;

		// 获取传感器管理器
		accelerometerSM = (SensorManager) getSystemService(SENSOR_SERVICE);

		// 获取加速度传感器
		if (accelerometerSM != null) {
			accelerometerSensor = accelerometerSM
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}

		// 为加速度传感器注册监听事件
		if (accelerometerSensor != null) {
			accelerometerSD = new AccelerometerSensorListener(this);
			// SENSOR_DELAY_UI，SENSOR_DELAY_FASTEST，SENSOR_DELAY_GAME
			accelerometerSM.registerListener(accelerometerSD,
					accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
		}

		// 判断是否已正常开启
		if (accelerometerSM != null && accelerometerSensor != null
				&& accelerometerSD != null) {
			isRun = true;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		
		if (accelerometerSD != null) {
			accelerometerSM.unregisterListener(accelerometerSD);
		}
		isRun = false;
		super.onDestroy();
	}

}
