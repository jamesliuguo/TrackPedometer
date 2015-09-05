package com.zhenqianfan13354468.trackpedometer;

import com.baidu.mapapi.SDKInitializer;

import android.app.Application;

public class GlobalApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		// 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
		SDKInitializer.initialize(this);
	}
}
