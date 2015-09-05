package com.zhenqianfan13354468.trackpedometer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;

public class TabFragmentMap extends Fragment {
	private static final String LTAG = TabFragmentMap.class.getSimpleName();

	private View view;
	private MapView mMapView = null; 
	private BaiduMap mBaiduMap;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.tab_fragment_map, container, false);

		initView();
		return view;
	}

	private void initView() {
		// TODO Auto-generated method stub
		mMapView = (MapView) view.findViewById(R.id.bmapView); 
		mBaiduMap = mMapView.getMap(); 
//		//普通地图  
//		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);  
//		//卫星地图  
//		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
		
//		//开启交通图   
//		mBaiduMap.setTrafficEnabled(true);
		

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

	}
	
	

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}
}
