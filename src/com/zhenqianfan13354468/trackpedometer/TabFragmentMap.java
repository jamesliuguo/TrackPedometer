package com.zhenqianfan13354468.trackpedometer;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.zhenqianfan13354468.trackpedometer.MyOrientationListener.OnOrientationListener;

public class TabFragmentMap extends Fragment implements OnClickListener {
	// private static final String LTAG = TabFragmentMap.class.getSimpleName();

	private View view;
	private static MapView mMapView = null;
	private static BaiduMap mBaiduMap;
	private UiSettings ui;

	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	boolean isFirstLoc = true;// 是否首次定位
	private LocationMode mCurrentMode;
	private BitmapDescriptor mCurrentMarker;
	private MyOrientationListener myOrientationListener;
	private float mCurrentX;
	private BDLocation mLocation = null; // baidu
	private Location currentLocation = null;// gps
	private static LocationManager locationManager;
	private static ConnectivityManager mConnectivityManager;
	private String provider;

	private double mLatitude;
	private double mLongtitude;

	private PopupMenu popupMenu;
	private Button bt_detLocation;
	private Button bt_ctrlTrack;
	private Button bt_mapMenu;
	Menu popMenu;

	// 轨迹相关
	boolean isRecording = false;
	boolean isRecodStart = false;
	boolean isRecordEnd = false;
	private LatLng lastLatLng;
	private LatLng nowLatLng;
	private LatLng latlng;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		mConnectivityManager = (ConnectivityManager) getActivity()
				.getSystemService(FragmentActivity.CONNECTIVITY_SERVICE);
		locationManager = (LocationManager) getActivity().getSystemService(
				FragmentActivity.LOCATION_SERVICE);
		//initGPS();

	}

	private void initGPS() {

		// // 从GPS获取最近的定位信息
		provider = LocationManager.GPS_PROVIDER;
		currentLocation = locationManager.getLastKnownLocation(provider);

		// 绑定监听，有4个参数
		// 参数1，设备：有GPS_PROVIDER和NETWORK_PROVIDER两种
		// 参数2，位置信息更新周期，单位毫秒
		// 参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息
		// 参数4，监听
		// 备注：对于参数2和3：若参数3不为0，则以参数3为准；若参数3为0，则通过时间来定时更新；两者为0，则随时刷新
		locationManager.requestLocationUpdates(provider, 1000, 1,
				new LocationListener() {

					@Override
					public void onStatusChanged(String provider, int status,
							Bundle extras) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onProviderEnabled(String provider) {
						// TODO Auto-generated method stub
						updateMapFromGPS(locationManager
								.getLastKnownLocation(provider));
					}

					@Override
					public void onProviderDisabled(String provider) {
						// TODO Auto-generated method stub
						updateMapFromGPS(null);
					}

					@Override
					public void onLocationChanged(Location location) {
						// TODO Auto-generated method stub
						updateMapFromGPS(location);
						currentLocation = location;
						Toast.makeText(getActivity(), "GPS定位成功",
								Toast.LENGTH_SHORT);
					}
				});

	}

	private void initMap() {

		mMapView = (MapView) view.findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();

		ui = mBaiduMap.getUiSettings();

		// BaiduMapOptions options = new BaiduMapOptions();
		// options.compassEnabled(false); // 不允许指南针
		// options.zoomControlsEnabled(false); // 不显示缩放按钮
		// options.scaleControlEnabled(false); // 不显示比例尺

		mMapView.removeViewAt(1); // 去掉百度logo
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(17.0f);
		mBaiduMap.setMapStatus(msu);
		modifyLocMarker(LocationMode.NORMAL);
	}

	protected void updateMapFromGPS(Location location) {
		if (location != null) {
			// 纬度
			double latitude = location.getLatitude();
			// 经度
			double longitude = location.getLongitude();

			LatLng latLng = new LatLng((int) (latitude * 1E6),
					(int) (longitude * 1E6));
			MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
			mBaiduMap.animateMapStatus(msu);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.tab_fragment_map, container, false);
		Log.i("map", "ontcreatview");
		initView();
		initMap();
		initLocation();

		return view;
	}

	private void initLocation() {

		// 开启定位图层
		// mBaiduMap.setMyLocationEnabled(true);
		// 定位初始化
		mLocClient = new LocationClient(getActivity());
		mLocClient.registerLocationListener(myListener);

		LocationClientOption option = new LocationClientOption();

		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置百度经纬度坐标系格式
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息 反编译获得具体位置，只有网络定位才可以
		option.setScanSpan(1000);// 设置发起定位请求的间隔时间为1000ms
		option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
		option.setAddrType("all"); // 设置使其可以获取具体的位置，把精度纬度换成具体地址
		// option.disableCache(true);//禁止启用缓存定位
		mLocClient.setLocOption(option);
		// mLocClient.start();

		mCurrentX = 0;
		myOrientationListener = new MyOrientationListener(getActivity());
		myOrientationListener
				.setOnOrientationListener(new OnOrientationListener() {

					@Override
					public void onOrientationChanged(float x) {
						mCurrentX = x;
					}
				});
	}

	private void initView() {
		bt_detLocation = (Button) view.findViewById(R.id.bt_detLocation);
		bt_detLocation.setOnClickListener(this);
		bt_ctrlTrack = (Button) view.findViewById(R.id.bt_ctrlTrack);
		bt_ctrlTrack.setOnClickListener(this);
		bt_mapMenu = (Button) view.findViewById(R.id.bt_mapmenu);
		bt_mapMenu.setOnClickListener(this);
		popupMenu = new PopupMenu(getActivity(),
				view.findViewById(R.id.bt_mapmenu));
		popMenu = popupMenu.getMenu();
		MenuInflater menuInflater = getActivity().getMenuInflater();
		menuInflater.inflate(R.menu.submenu_map_setting, popMenu);
		popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// TODO Auto-generated method stub
				switch (item.getItemId()) {
				case R.id.it_normal:
					modifyLocMarker(LocationMode.NORMAL);
					break;
				case R.id.it_follow:
					modifyLocMarker(LocationMode.FOLLOWING);
					break;
				case R.id.it_compass:
					modifyLocMarker(LocationMode.COMPASS);
					break;
				case R.id.it_normalmap:
					mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
					break;
				case R.id.it_satellitemap:
					mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
					break;
				case R.id.it_screenshot:

					break;
				}

				return false;
			}
		});
	}

	private void modifyLocMarker(LocationMode locationMode) {
		// 修改为自定义marker
		mCurrentMarker = BitmapDescriptorFactory
				.fromResource(R.drawable.mylocation);
		mCurrentMode = locationMode;
		mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
				mCurrentMode, true, mCurrentMarker));

	}

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;

			MyLocationData locData = new MyLocationData.Builder()//
					.direction(mCurrentX)//
					.accuracy(location.getRadius())//
					.latitude(location.getLatitude())//
					.longitude(location.getLongitude())//
					.build();
			mBaiduMap.setMyLocationData(locData);// 更新定位数据
			mMapView.refreshDrawableState();// 刷新
			// 更新经纬度
			mLatitude = location.getLatitude();
			mLongtitude = location.getLongitude();
			mLocation = location;

			if (isFirstLoc) {
				centerToMyLocation();
				isFirstLoc = false;
			}
			latlng = new LatLng(mLatitude, mLongtitude);
			if (isRecording) {
				if (isRecodStart) {
					lastLatLng = latlng;
					isRecodStart = false;
					drawMaker(latlng, R.drawable.startpoint);
				}
				nowLatLng = latlng;

				if (!nowLatLng.equals(lastLatLng)) {
					drawPolyline(lastLatLng, nowLatLng);
				}
			}

			if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
				Toast.makeText(getActivity(), "BDGPS定位成功", Toast.LENGTH_SHORT);

			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
				Toast.makeText(getActivity(), "BD网络定位成功", Toast.LENGTH_SHORT);
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	private void centerToMyLocation() {
		if (mLatitude == 0 && mLongtitude == 0) {
			return;
		}
		LatLng latLng1 = new LatLng(mLatitude, mLongtitude);
		MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng1);
		mBaiduMap.animateMapStatus(msu);
	}

	public void drawPolyline(LatLng latlng1, LatLng latlng2) {
		// 画线
		List<LatLng> points = new ArrayList<LatLng>();
		points.add(latlng1);
		points.add(latlng2);
		OverlayOptions polyline = new PolylineOptions().width(4)
				.color(0xAAFF0000).points(points);
		mBaiduMap.addOverlay(polyline);

	}

	public void drawMaker(LatLng latlng, int id_source) {
		// 构建Marker图标
		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(id_source);
		// 构建MarkerOption，用于在地图上添加Marker
		OverlayOptions option = new MarkerOptions().position(latlng).icon(
				bitmap);
		// 在地图上添加Marker，并显示
		mBaiduMap.addOverlay(option);

	}

	@Override
	public void onResume() {
		Log.i("map", "onResume");
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
	}

	@Override
	public void onStart() {
		Log.i("map", "onStart");
		// TODO Auto-generated method stub
		super.onStart();
		// 开启定位
		mBaiduMap.setMyLocationEnabled(true);
		if (!mLocClient.isStarted())
			mLocClient.start();
		// 开启方向传感器
		myOrientationListener.start();

	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i("map", "onPause");
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}

	@Override
	public void onClick(View view) {
		ui.setCompassPosition(new Point(mMapView.getWidth() / 2, 50));
		switch (view.getId()) {
		case R.id.bt_detLocation:
			centerToMyLocation();
			if (mLocation != null) {
				Toast.makeText(getActivity(), mLocation.getAddrStr(),
						Toast.LENGTH_SHORT).show();
			}
			// currentLocation = locationManager
			// .getLastKnownLocation(provider);
			// updateMapFromGPS(currentLocation);

			break;
		case R.id.bt_ctrlTrack:
			if (!isRecording) {
				bt_ctrlTrack.setBackgroundResource(R.drawable.stop);
				Toast.makeText(getActivity(), "开始记录轨迹...", Toast.LENGTH_SHORT)
						.show();
				isRecording = true;
				isRecodStart = true;
				// 清除所有图层
				mBaiduMap.clear();

				if (latlng != null) {
					lastLatLng = latlng;
					isRecodStart = false;
					drawMaker(latlng, R.drawable.startpoint);
				}

			} else {
				bt_ctrlTrack.setBackgroundResource(R.drawable.start);
				Toast.makeText(getActivity(), "轨迹记录已停止.", Toast.LENGTH_SHORT)
						.show();
				isRecording = false;
				isRecordEnd = true;
				if (latlng != null) {
					isRecordEnd = false;
					drawMaker(latlng, R.drawable.endpoint);
				}
			}
			break;
		case R.id.bt_mapmenu:
			popupMenu.show();

			break;
		}

	}

	private boolean canShow = true;

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			centerToMyLocation();
			// 相当于Fragment的onResume
			boolean isNetable = false;
			getActivity();
			if (mConnectivityManager != null) {
				NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {
					// 当前网络是连接的
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						// 当前所连接的网络可用
						isNetable = true;
					}
				}
			}
			if (isNetable && canShow) {
				Toast.makeText(getActivity(), "当前网络可用", Toast.LENGTH_SHORT)
						.show();
			} else if (!isNetable) {
				Toast.makeText(getActivity(), "当前网络不可用", Toast.LENGTH_SHORT)
						.show();
				canShow = true;
			}

			// AGPS:通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
			boolean agps = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			// GPS:通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
			boolean gps = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if ((gps | agps) && canShow) {
				Toast.makeText(getActivity(), "AGPS或GPS已打开", Toast.LENGTH_SHORT)
						.show();
				canShow = false;
				if (!isNetable) {
					canShow = true;
				}
			} else if (!gps && !agps) {
				Toast.makeText(getActivity(), "AGPS和GPS尚未打开，无法定位",
						Toast.LENGTH_SHORT).show();
				canShow = true;
			}
			if (mLocation != null) {
				Toast.makeText(getActivity(), mLocation.getAddrStr(),
						Toast.LENGTH_SHORT).show();
			}

		} else {
			// 相当于Fragment的onPause
			// Log.v("tag", "pause");

		}
	}

	@Override
	public void onDestroy() {
		// 退出时销毁定位
		mLocClient.stop();
		Log.i("map", "onDestroy");
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();
	}

}
