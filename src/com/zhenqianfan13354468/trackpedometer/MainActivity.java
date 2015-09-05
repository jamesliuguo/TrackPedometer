package com.zhenqianfan13354468.trackpedometer;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
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
	private TextView mHistoryTextView;
	private TextView mStepTextView;
	private TextView mAnalyseTextView;
	private TextView mSettingTextView;

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
		mHistoryTextView = (TextView) findViewById(R.id.id_tv_history);
		mStepTextView = (TextView) findViewById(R.id.id_tv_step);
		mAnalyseTextView = (TextView) findViewById(R.id.id_tv_analyse);
		mSettingTextView = (TextView) findViewById(R.id.id_tv_setting);

		mMapTextView.setOnClickListener(this);
		mHistoryTextView.setOnClickListener(this);
		mStepTextView.setOnClickListener(this);
		mAnalyseTextView.setOnClickListener(this);
		mSettingTextView.setOnClickListener(this);

		mFragmentDatas = new ArrayList<Fragment>();

		mFragmentDatas.add(new TabFragmentMap());
		mFragmentDatas.add(new TabFragmentHistory());
		mFragmentDatas.add(new TabFragmentStep());
		mFragmentDatas.add(new TabFragmentAnalyse());
		mFragmentDatas.add(new TabFragmentSetting());

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
		mViewPager.setCurrentItem(2);

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {

				resetTextView();

				switch (position) {
				case 0:
					mMapTextView.setTextColor(Color.parseColor("#008000"));
					break;
				case 1:
					mHistoryTextView.setTextColor(Color.parseColor("#008000"));
					break;
				case 2:
					mStepTextView.setTextColor(Color.parseColor("#008000"));
					break;
				case 3:
					mAnalyseTextView.setTextColor(Color.parseColor("#008000"));
					break;
				case 4:
					mSettingTextView.setTextColor(Color.parseColor("#008000"));
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
		mHistoryTextView.setTextColor(Color.BLACK);
		mStepTextView.setTextColor(Color.BLACK);
		mSettingTextView.setTextColor(Color.BLACK);
		mAnalyseTextView.setTextColor(Color.BLACK);

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.id_tv_map:
			mViewPager.setCurrentItem(0);
			break;
		case R.id.id_tv_history:
			mViewPager.setCurrentItem(1);
			break;
		case R.id.id_tv_step:
			mViewPager.setCurrentItem(2);
			break;
		case R.id.id_tv_analyse:
			mViewPager.setCurrentItem(3);
			break;
		case R.id.id_tv_setting:
			mViewPager.setCurrentItem(4);
			break;
		}

	}
}
