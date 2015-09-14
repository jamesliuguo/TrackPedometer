package com.zhenqianfan13354468.trackpedometer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.log4j.Level;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.util.Base64;
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
import com.baidu.mapapi.map.BaiduMap.SnapshotReadyCallback;
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
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.baidu.mapapi.utils.DistanceUtil;
import com.zhenqianfan13354468.trackpedometer.MyOrientationListener.OnOrientationListener;

import de.mindpipe.android.logging.log4j.LogConfigurator;

/**
 * 地图的Fragment
 *
 */

public class TabFragmentMap extends Fragment implements OnClickListener,
		OnGetGeoCoderResultListener {
	private static final String TAG = TabFragmentMap.class.getSimpleName();

	// 百度地图SDK相关
	private View view;
	private static MapView mMapView = null;
	private static BaiduMap mBaiduMap;
	private UiSettings ui;
	private GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用

	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	boolean isFirstLoc = true;// 是否首次定位
	private LocationMode mCurrentMode;
	private MyOrientationListener myOrientationListener;// 使用方向传感器校正方向
	private float mCurrentX;
	private static LocationManager locationManager;
	private static ConnectivityManager mConnectivityManager;
	private String provider;
	private BDLocation curBDLocation = null; // 百度定位获得的地址
	private BDLocation curGPSLocation = null;// 自带GPS传感器获得的地址

	private static final int FROM_GPS = 0; // 地址从自带GPS传感器获得
	private final static int FROM_BD_GPS = 1;// 地址从百度通过GPS获得
	private final static int FROM_BD_NETWORK = 2;// 地址从百度通过网络GPS获得
	private final static int FROM_BD_OTHERS = 3;// 地址从百度通过离线定位或服务端网络定位等获得，偏差较大

	private final static double DISTANCE_MAX_LIMIT = 10000;// 最大容忍定位偏差，
															// 用于普通定位，防止突然暴走
	private final static double DISTANCE_MID_LIMIT = 2000;// 适中容忍定位偏差 ，用于画轨迹上限
	private final static double DISTANCE_MIN_LIMIT = 200;// 最小容忍定位偏差 ，用于立即画轨迹下限
	private double dynamidDistanceLimit = 1000;// 动态适中容忍定位偏差
	private double lastDistance = 100;// 上一次允许的两点distance
	private double alpha = 0.8; // a 暂取 0.8吧
	// 权重公式：dynamidDistanceLimit = alpha * lastDistance + (1-alpha) * dynamidDistanceLimit
	private double disGPS;
	private double disBD;
	private float accuracy;
	private int UPDATE_TIME = 3000;

	// 当前优化修正后的位置对应的BDLocation
	private BDLocation mBDLocation;

	// 控件
	private PopupMenu popupMenu;
	private Button bt_detLocation;
	public static Button bt_ctrlTrack;
	private Button bt_mapMenu;
	Menu popMenu;

	// 轨迹相关
	public static boolean isRecording = false;
	boolean isRecodStart = false;
	boolean isRecordEnd = false;
	private LatLng lastLatLng;
	private LatLng nowLatLng;
	private LatLng latlng;

	// 日志记录
	private Logger logger;

	//网络和GPS状态
	private Thread thread;
	private boolean network = false;
	private boolean agps = false;
	private boolean gps = false;

	
	/**
	 * 处理自带GPS传感器和百度SDK分别得到的位置坐标，进行优化修正
	 */
	private Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			double dis;
			switch (msg.what) {
			case FROM_GPS:
				dis = getDistance(new LatLng(curGPSLocation.getLatitude(),
						curGPSLocation.getLongitude()), latlng);
				if (disGPS > DISTANCE_MAX_LIMIT) {
					return;
				}
				disGPS = dis;
				break;
			case FROM_BD_GPS:
				// mBDLocation = curBDLocation;
				dis = getDistance(new LatLng(curBDLocation.getLatitude(),
						curBDLocation.getLongitude()), latlng);
				disBD = dis;
				break;
			case FROM_BD_NETWORK:
				// mBDLocation = curBDLocation;
				dis = getDistance(new LatLng(curBDLocation.getLatitude(),
						curBDLocation.getLongitude()), latlng);
				disBD = dis;
				break;
			case FROM_BD_OTHERS:
				// mBDLocation = curBDLocation;
				// 等若放弃
				if (mBDLocation == null) {
					mBDLocation = curBDLocation;
				}
				break;
			default:
				break;
			}

			if (disBD == 0) {
				if (disGPS < DISTANCE_MIN_LIMIT) {
					mBDLocation = curGPSLocation;
				}
			} else if (disGPS == 0) {
				if (disBD < DISTANCE_MIN_LIMIT) {
					mBDLocation = curBDLocation;
				}
			} else {
				if (disBD <= disGPS) {
					mBDLocation = curBDLocation;
				} else {
					mBDLocation = curGPSLocation;
				}
			}
			if (mBDLocation == null) {
				if (curBDLocation == null) {
					mBDLocation = curGPSLocation;
				}
				else if (curGPSLocation == null) {
					mBDLocation = curBDLocation;
				}
				else if (curBDLocation == null && curBDLocation == null) {
					Toast.makeText(getActivity(), "暂时无法定位，请检查网络设置。", Toast.LENGTH_SHORT);
				}
				else if (disBD <= disGPS) {
					mBDLocation = curBDLocation;
				} else {
					mBDLocation = curGPSLocation;
				}
			}

			accuracy = mBDLocation.getRadius();
			
			latlng = new LatLng(mBDLocation.getLatitude(),
					mBDLocation.getLongitude());
			if (isRecording) {
				if (isRecodStart) {
					lastLatLng = latlng;
					isRecodStart = false;
					drawMaker(latlng, R.drawable.startpoint);
					// centerToMyLocation();
					modifyLocMarker(LocationMode.FOLLOWING);
					setLocationData(mBDLocation);
				} else {
					// 过滤不合理坐标!!!!!!!!!!!，同时采取权重公式来补偿早期的定位误差。
					if (filter()) {
						lastLatLng = nowLatLng;
						nowLatLng = latlng;

						centerToMyLocation();
						drawPolyline(lastLatLng, nowLatLng);
					}

				}
			}
		};
	};

	
	/**
	 * 过滤不合理坐标!!!!!!!!!!!，同时采取权重公式来补偿早期的定位误差。
	 */
	protected boolean filter() {
		double dis = getDistance(lastLatLng, nowLatLng);
		if (dis <= DISTANCE_MIN_LIMIT) {
			return true;
		}
		if (dis >= DISTANCE_MID_LIMIT) {
			return false;
		}
		dynamidDistanceLimit = alpha * lastDistance + (1 - alpha)
				* dynamidDistanceLimit;
		if (dis <= dynamidDistanceLimit) {
			lastDistance = dis;
			return true;
		}

		return false;
	}

	// 计算距离 m, 若错误返回-1
	private double getDistance(LatLng latlng1, LatLng latLng2) {
		// 测距
		return DistanceUtil.getDistance(latlng1, latLng2);
	}

	public void configLog() {
		final LogConfigurator logConfigurator = new LogConfigurator();

		logConfigurator.setFileName(Environment.getExternalStorageDirectory()
				+ File.separator + "map.log");
		// Set the root log level
		logConfigurator.setRootLevel(Level.DEBUG);
		// Set log level of a specific logger
		logConfigurator.setLevel("org.apache", Level.ERROR);
		logConfigurator.configure();

		logger = Logger.getLogger(TAG);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		configLog();
		logger.info("onCreate");


		mConnectivityManager = (ConnectivityManager) getActivity()
				.getSystemService(FragmentActivity.CONNECTIVITY_SERVICE);
		locationManager = (LocationManager) getActivity().getSystemService(
				FragmentActivity.LOCATION_SERVICE);

		initGPS();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.tab_fragment_map, container, false);
		Log.i(TAG, "onCreatview");
		logger.info("onCreatview");

		initView();
		initMap();
		initLocation();

		mMonitorThread();

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i(TAG, "onStart");

		// 开启定位
		mBaiduMap.setMyLocationEnabled(true);
		if (!mLocClient.isStarted()) {
			mLocClient.start();// //定位SDK
								// start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request
		}
		// 开启方向传感器
		myOrientationListener.start();
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, "onPause");
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}

	@Override
	public void onResume() {
		Log.i(TAG, "onResume");
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
	}

	
	/**
	 * 初始化GPS
	 */
	private void initGPS() {

		// // 从GPS获取最近的定位信息，缓存数据
		provider = LocationManager.GPS_PROVIDER;
		Location location;
		location = locationManager.getLastKnownLocation(provider);
		if (location != null && location.getLatitude() != 0
				&& location.getLongitude() != 0) {
			// handler.sendEmptyMessage(FROM_GPS);
			// 首先要有数据
			mBDLocation = new BDLocation();
			Location2BDLocation(location, curGPSLocation);
			mBDLocation = curGPSLocation;
		}

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
						// updateMapFromGPS(locationManager.getLastKnownLocation(provider));
						Location2BDLocation(locationManager
								.getLastKnownLocation(provider), curGPSLocation);
						
					}

					@Override
					public void onProviderDisabled(String provider) {
						// TODO Auto-generated method stub
						updateMapFromGPS(null);
					}

					@Override
					public void onLocationChanged(Location location) {
						// TODO Auto-generated method stub
						// updateMapFromGPS(location);
						Location2BDLocation(location, curGPSLocation);
						if (curGPSLocation != null
								&& curGPSLocation.getLatitude() != 0
								&& curGPSLocation.getLongitude() != 0) {
							handler.sendEmptyMessage(FROM_GPS);
						}
						Log.i(TAG, "GPS：(" + location.getLatitude() + ","
								+ getLoaderManager() + ")");
					}
				});
	}

	
	/**
	 * @param location
	 * @param bdLocation
	 * 普通location转百度location
	 */
	private void Location2BDLocation(Location location, BDLocation bdLocation) {
		if (location == null || bdLocation == null) {
			return;
		}
		
		// 将GPS设备采集的原始GPS坐标转换成百度坐标    
		LatLng sourceLatLng = new LatLng(location.getLatitude(), location.getLongitude());
		CoordinateConverter converter  = new CoordinateConverter();    
		converter.from(CoordType.GPS);    
		// sourceLatLng待转换坐标    
		converter.coord(sourceLatLng);    
		LatLng desLatLng = converter.convert(); 
		
		bdLocation.setLatitude(desLatLng.latitude);
		bdLocation.setLongitude(desLatLng.longitude);
		
		
		
		bdLocation.setSpeed(location.getSpeed());
		bdLocation.setAltitude(location.getAltitude());
		bdLocation.setDirection(location.getBearing());
	}

	/**
	 * 用纠偏url转换原始坐标为百度坐标
	 * @throws IOException 
	 * From http://www.cnblogs.com/zhaohuionly/p/3142623.html
	 */
	public void correct_from_url(Location bdloc) throws IOException {
		String str = String.format("http://api.map.baidu.com/ag/coord/convert?from=0&to=4&x=%f&y=%f", bdloc.getLatitude(), bdloc.getLongitude());
		URL url = new URL(str);
        URLConnection connection = url.openConnection();
        
        connection.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(connection
                .getOutputStream(), "utf-8");
        out.flush();
        out.close();
        
        String sCurrentLine;
        String sTotalString;
        sCurrentLine = "";
        sTotalString = "";
        InputStream l_urlStream;
        l_urlStream = connection.getInputStream();
        BufferedReader l_reader = new BufferedReader(new InputStreamReader(
                l_urlStream));
        while ((sCurrentLine = l_reader.readLine()) != null) {
            if (!sCurrentLine.equals(""))
                sTotalString += sCurrentLine;
        }
        String mapX="", mapY="";
        System.out.println(sTotalString);
        sTotalString = sTotalString.substring(1, sTotalString.length()-1);
        String[] results = sTotalString.split("\\,");
        if (results.length == 3){
            if (results[0].split("\\:")[1].equals("0")){
                mapX = results[1].split("\\:")[1];
                mapY = results[2].split("\\:")[1];
                mapX = mapX.substring(1, mapX.length()-1);
                mapY = mapY.substring(1, mapY.length()-1);
                mapX = new String(Base64.decode(mapX, Base64.DEFAULT));
                mapY = new String(Base64.decode(mapY, Base64.DEFAULT));
                
                bdloc.setLatitude(Double.parseDouble(mapX));
                bdloc.setLongitude(Double.parseDouble(mapY));
                //Toast.makeText(getActivity(), "坐标已修正", Toast.LENGTH_SHORT).show();
            }
        }
	}
	
	/**
	 * 初始化地图
	 */
	private void initMap() {
		mMapView = (MapView) view.findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		ui = mBaiduMap.getUiSettings();

		// BaiduMapOptions options = new BaiduMapOptions();
		// options.compassEnabled(false); // 不允许指南针
		// options.zoomControlsEnabled(false); // 不显示缩放按钮
		// options.scaleControlEnabled(false); // 不显示比例尺

		mMapView.removeViewAt(1); // 去掉百度logo
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(17.0f);// 17 100m
		mBaiduMap.setMapStatus(msu);
		modifyLocMarker(LocationMode.NORMAL); // 普通模式

		// 初始化搜索模块，注册事件监听
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);

	}

	// 看着玩的
	protected void updateMapFromGPS(Location location) {
		if (location != null) {
			// 纬度
			double latitude = location.getLatitude();
			// 经度
			double longitude = location.getLongitude();

			LatLng latLng = new LatLng((int) (latitude * 1E6),
					(int) (longitude * 1E6));
			MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
			mBaiduMap.animateMapStatus(msu); // 移到中心，没设置定位图标

		}
	}

	
	/**
	 * 初始化定位
	 */
	private void initLocation() {

		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		// 定位初始化
		mLocClient = new LocationClient(getActivity().getApplicationContext());
		mLocClient.registerLocationListener(myListener);

		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置百度经纬度坐标系格式
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息 反编译获得具体位置，只有网络定位才可以
		option.setScanSpan(UPDATE_TIME);// 设置发起定位请求的间隔时间为1000ms
		// option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
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

	
	/**
	 * 初始化菜单控件和子菜单
	 */
	private void initView() {
		bt_detLocation = (Button) view.findViewById(R.id.bt_detLocation);
		bt_detLocation.setOnClickListener(this);
		bt_ctrlTrack = (Button) view.findViewById(R.id.bt_ctrlTrack);
		bt_ctrlTrack.setOnClickListener(this);
		bt_mapMenu = (Button) view.findViewById(R.id.bt_mapmenu);
		bt_mapMenu.setOnClickListener(this);
		
		// 左上角菜单
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
				case R.id.it_screenshot:
					// 截图并保存到SD卡
					mBaiduMap.snapshot(new SnapshotReadyCallback() {
						@SuppressLint("SdCardPath")
						@Override
						public void onSnapshotReady(Bitmap snapshot) {
							FileOutputStream out;
							try {
								File file = new File("/mnt/sdcard/轨迹.png");
								out = new FileOutputStream(file);
								if (snapshot.compress(
										Bitmap.CompressFormat.PNG, 100, out)) {
									out.flush();
									out.close();
								}
								Toast.makeText(getActivity(),
										"截图成功，截图保存在：" + file.toString(),
										Toast.LENGTH_SHORT).show();
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}

						}
					});
					Toast.makeText(getActivity(), "正在截取轨迹图...",
							Toast.LENGTH_SHORT).show();

					break;
				case R.id.it_clearMaket:
					mBaiduMap.clear();
					break;
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
				case R.id.it_setGps:
					// 转到手机设置界面，用户设置GPS
					Intent intent = new Intent(
							Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					startActivityForResult(intent, 0); // 设置完成后返回到原来的界面
					break;
				case R.id.it_back:
					getActivity().moveTaskToBack(false);
					break;
				case R.id.it_exit:
					android.os.Process.killProcess(android.os.Process.myPid()); // 获取PID
					System.exit(0); // 常规java、c#的标准退出法，返回值为0代表正常退出
					break;
				}

				return false;
			}
		});
	}

	
	/**
	 * @param locationMode
	 * 修改地图模式和定位图标
	 */
	private void modifyLocMarker(LocationMode locationMode) {
		// 修改为自定义marker图标
		// BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
		// .fromResource(R.drawable.mylocation);
		mCurrentMode = locationMode;
		mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
				mCurrentMode, true, null));
	}
	
	// /**
	// * 用经验参数修正百度坐标
	// */
	// public void correct(BDLocation bdloc){
	// bdloc.setLatitude(bdloc.getLatitude()-0.01185);
	// bdloc.setLongitude(bdloc.getLongitude()-0.00328);
	// }

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;
			if (location.getLatitude() == 0.0 && location.getLongitude() == 0.0) {
				// mLocClient.requestLocation();//请求定位
				// mLocClient.requestOfflineLocation();//请求一次离线地址
				return;
			}
			
			// 更新当前位置经纬度
			curBDLocation = location;
			
//			correct(curBDLocation);
			
			
			double mLatitude = location.getLatitude();
			double mLongtitude = location.getLongitude();

			if (isFirstLoc) {

				isFirstLoc = false;
				
				latlng = new LatLng(location.getLatitude(), location.getLongitude());
				nowLatLng = latlng;
				
				// 默认承认百度地图的第一次定位
				mBDLocation = location;
				// setLocationData(location); // 更新定位数据，设置定位点
				centerToMyLocation();
			}

			if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
				Log.i(TAG, "BD-GPS: (" + mLatitude + "," + mLongtitude + ")");
				handler.sendEmptyMessage(FROM_BD_GPS);

			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
				Log.i(TAG, "BD-Net: (" + mLatitude + "," + mLongtitude + ")");
				handler.sendEmptyMessage(FROM_BD_NETWORK);
			} else {
				Log.i(TAG, "BD-None: (" + mLatitude + "," + mLongtitude + ")");
				handler.sendEmptyMessage(FROM_BD_OTHERS);
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	
	/**
	 * 设置定位点并移到中心
	 */
	private void centerToMyLocation() {
		if (mBDLocation == null
				|| (mBDLocation.getLatitude() == 0 && mBDLocation
						.getLongitude() == 0)) {
			Toast.makeText(getActivity(), "暂无法定位，请检查网络设置。", Toast.LENGTH_SHORT).show();
			return;
		}
		setLocationData(mBDLocation);

		double mLatitude = mBDLocation.getLatitude();
		double mLongtitude = mBDLocation.getLongitude();
		if (mLatitude == 0 && mLongtitude == 0) {
			return;
		}
		LatLng latLng1 = new LatLng(mLatitude, mLongtitude);
		MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng1);
		mBaiduMap.animateMapStatus(msu);// 设置中心点

	}

	


	/**
	 * @param location
	 * 更新定位数据，设置定位点
	 */
	private void setLocationData(BDLocation location) {
		MyLocationData locData = new MyLocationData.Builder()//
				.direction(mCurrentX)//
				.accuracy(location.getRadius())//
				.latitude(location.getLatitude())//
				.longitude(location.getLongitude())//
				.build();
		mBaiduMap.setMyLocationData(locData);// 更新定位点数据!!!!!!!!!!!!!!

		mMapView.refreshDrawableState();// 刷新

	}

	
	/**
	 * @param latlng1
	 * @param latlng2
	 * 画线
	 */
	public void drawPolyline(LatLng latlng1, LatLng latlng2) {

		List<LatLng> points = new ArrayList<LatLng>();
		points.add(latlng1);
		points.add(latlng2);
		OverlayOptions polyline = new PolylineOptions().width(4)
				.color(0xAAFF0000).points(points);
		mBaiduMap.addOverlay(polyline);
	}

	
	/**
	 * @param latlng
	 * @param id_source
	 * 画Marker图标
	 */
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
	
	public static boolean showflag = true;

	@Override
	public void onClick(View view) {
		ui.setCompassPosition(new Point(mMapView.getWidth() / 2, 50));

		switch (view.getId()) {
		case R.id.bt_detLocation:
			mLocClient.requestLocation();// 请求定位
			centerToMyLocation();
			if (mBDLocation != null
					&& (mBDLocation.getLatitude() != 0 && mBDLocation
							.getLongitude() != 0)) {
				if (mBDLocation.getAddrStr() != ""
						&& mBDLocation.getAddrStr() != ""
						&& mBDLocation.getAddrStr() != null) {// 百度定位地址有物理地址
					Toast.makeText(getActivity(), mBDLocation.getAddrStr(),
							Toast.LENGTH_SHORT).show();// 显示物理位置，要改下
				} else {// 纯GPS定位地址反查物理地址
					LatLng ptCenter = new LatLng(mBDLocation.getLatitude(),
							mBDLocation.getLongitude());
					// 反Geo搜索
					mSearch.reverseGeoCode(new ReverseGeoCodeOption()
							.location(ptCenter));
				}

			} else {
				Toast.makeText(getActivity(), "暂无法定位，请检查网络设置。",
						Toast.LENGTH_SHORT).show();
			}
			if (!network) {
				Toast.makeText(getActivity(), "当前网络不可用，请检查网络状态",
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.bt_ctrlTrack:
			if (!isRecording) {
				bt_ctrlTrack.setBackgroundResource(R.drawable.stop);
				if (showflag) {
					Toast.makeText(getActivity(), "开始记录轨迹...", Toast.LENGTH_LONG)
					.show();
				}
				isRecording = true;
				isRecodStart = true;
				// 清除所有图层
				mBaiduMap.clear();
				// TODO
				if (latlng != null) {
					lastLatLng = latlng;
					isRecodStart = false;
					centerToMyLocation();
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
				
				if (TabFragmentStep.isOpenMap) {
					TabFragmentStep.isOpenMap = false;
				}
			}
			break;
		case R.id.bt_mapmenu:
			popupMenu.show();
			break;
		}

	}

	private boolean firstVis = true;

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);

		if (isVisibleToUser) {
			// 相当于Fragment的onResume
			Log.i(TAG, "visible");
			
			if (firstVis) {
				firstVis = false;
				if (network) {
					Toast.makeText(getActivity(), "当前网络可用", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getActivity(), "当前网络不可用，请检查网络状态",
							Toast.LENGTH_SHORT).show();
				}
				if (gps) {
					Toast.makeText(getActivity(), "GPS已打开", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getActivity(), "GPS已关闭，请开启GPS",
							Toast.LENGTH_SHORT).show();
				}

			} else {
				if (!network) {
					Toast.makeText(getActivity(), "当前网络不可用，请检查网络状态",
							Toast.LENGTH_SHORT).show();
				}

				if (!gps) {
					Toast.makeText(getActivity(), "GPS已关闭，请开启GPS",
							Toast.LENGTH_SHORT).show();
				}
			}

		} else {
			// 相当于Fragment的onPause
			// Log.v("tag", "pause");

		}

	}

	
	/**
	 * 监测网络和GPS的子线程
	 */
	private void mMonitorThread() {
		if (thread == null) {
			thread = new Thread(new Runnable() {
				public void run() {
					Log.i(TAG, "subThread run");
					while (true) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						monitorNetworkAndGPS();
					}
				}
			});
			thread.start();
		}

	}

	
	/**
	 * 监测网络和GPS是否可用
	 */
	private void monitorNetworkAndGPS() {
//		Log.i(TAG, "monitoring network");
		if (mConnectivityManager != null) {
			NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
			if (info != null && info.isConnected()) {
				// 当前网络是连接的
				if (info.getState() == NetworkInfo.State.CONNECTED) {
					// 当前所连接的网络可用
					network = true;
				} else {
					network = false;
				}
			}
		}

		// AGPS:通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
		if (locationManager != null) {
			agps = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			// GPS:通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
			gps = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
		}

	}

	@Override
	public void onDestroy() {
		// 退出时销毁定位
		mLocClient.stop();
		Log.i(TAG, "onDestroy");
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(getActivity(), "抱歉，当前地理位置无法识别", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		Toast.makeText(getActivity(), result.getAddress(), Toast.LENGTH_LONG)
				.show();

	}

}
