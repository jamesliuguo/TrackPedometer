package com.zhenqianfan13354468.trackpedometer;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;

public class AccelerometerSensorService extends Service {

	// Accelerometer传感器
	private SensorManager accelerometerSM;
	private AccelerometerSensorListener accelerometerSD;
	Sensor accelerometerSensor;

	
	
	public static boolean isRun;

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

		// 获取系统服务（SENSOR_SERVICE)返回一个SensorManager 传感器管理器对象
		accelerometerSM = (SensorManager) getSystemService(SENSOR_SERVICE);

		// 通过SensorManager对象获取相应的重力传感器
		if (accelerometerSM != null) {
			accelerometerSensor = accelerometerSM
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}

		// 注册相应的SensorService
		if (accelerometerSensor != null) {
			accelerometerSD = new AccelerometerSensorListener(this);
			// 更新速率还有SENSOR_DELAY_UI、SENSOR_DELAY_FASTEST、SENSOR_DELAY_GAME等，
			// 根据不同应用，需要的反应速率不同，具体根据实际情况设定
			accelerometerSM.registerListener(accelerometerSD,
					accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
		}

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
		super.onDestroy();
		if (accelerometerSD != null) {
			accelerometerSM.unregisterListener(accelerometerSD);
		}
		isRun = false;
	}
}
