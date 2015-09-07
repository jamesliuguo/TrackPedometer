package com.zhenqianfan13354468.trackpedometer;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements OnClickListener {

	private ViewPager mViewPager;
	private FragmentPagerAdapter mFragmentAdapter;
	private List<Fragment> mFragmentDatas;

	public int mCurrentPageIndex;

	private TextView mMapTextView;

	private TextView mStepTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		initView();
	}

	private void initView() {
		mViewPager = (ViewPager) findViewById(R.id.id_viewpager);

		mMapTextView = (TextView) findViewById(R.id.id_tv_map);
		mStepTextView = (TextView) findViewById(R.id.id_tv_step);

		mMapTextView.setOnClickListener(this);
		mStepTextView.setOnClickListener(this);

		mFragmentDatas = new ArrayList<Fragment>();

		mFragmentDatas.add(new TabFragmentMap());
		mFragmentDatas.add(new TabFragmentStep());

		mFragmentAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
			@Override
			public int getCount() {
				return mFragmentDatas.size();
			}

			@Override
			public Fragment getItem(int arg0) {
				return mFragmentDatas.get(arg0);
			}
		};

		mViewPager.setAdapter(mFragmentAdapter);
		mViewPager.setCurrentItem(1);

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {

				resetTextView();

				switch (position) {
				case 0:
					mMapTextView.setTextColor(Color.parseColor("#008000"));
					break;
				case 1:
					mStepTextView.setTextColor(Color.parseColor("#008000"));
					break;

				}

				mCurrentPageIndex = position;
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}
		});

	}

	protected void resetTextView() {
		mMapTextView.setTextColor(Color.BLACK);
		mStepTextView.setTextColor(Color.BLACK);

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.id_tv_map:
			mViewPager.setCurrentItem(0);
			break;
		case R.id.id_tv_step:
			mViewPager.setCurrentItem(2);
			break;
		}

	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK: {
			if (event.isLongPress()) // 没用
			{
				this.stopService(getIntent());
				System.exit(0);
				return true;
			} else {
				moveTaskToBack(false);
				boolean flag = false;
				return flag;
			}
		}
		}
		return super.dispatchKeyEvent(event);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		int group = 1;
		menu.add(group, 1, 1, "后台运行");
		menu.add(group, 2, 2, "退出应用");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			moveTaskToBack(false);
			break;
		case 2:
			android.os.Process.killProcess(android.os.Process.myPid()); // 获取PID
			System.exit(0); // 常规java、c#的标准退出法，返回值为0代表正常退出
			break;
		default:
			break;
		}
		return true;
	}
}
