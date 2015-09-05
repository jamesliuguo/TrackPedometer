package com.zhenqianfan13354468.trackpedometer;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

public class LightSensorService extends Service implements SensorEventListener {

	// Light ������
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

		// ��ȡϵͳ����SENSOR_SERVICE)����һ��SensorManager ����������������
		lightSM = (SensorManager) getSystemService(SENSOR_SERVICE);
		// ͨ��SensorManager�����ȡ��Ӧ�Ĵ�����
		lightSensor = lightSM.getDefaultSensor(Sensor.TYPE_LIGHT);
		// ע����Ӧ��SensorService
		// �������ʻ���SENSOR_DELAY_UI��SENSOR_DELAY_FASTEST��SENSOR_DELAY_GAME�ȣ�
		// ���ݲ�ͬӦ�ã���Ҫ�ķ�Ӧ���ʲ�ͬ���������ʵ������趨
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
