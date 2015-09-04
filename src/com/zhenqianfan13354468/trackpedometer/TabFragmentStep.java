package com.zhenqianfan13354468.trackpedometer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class TabFragmentStep extends Fragment implements OnClickListener,
		OnChronometerTickListener {

	private View view;
	private Thread thread;

	private TextView tvPercent;
	private ProgressBar pbPercent;
	private TextView tvGoal;
	private TextView tvSteps;
	private Button btReset;
	// private TextView tvPasstime;
	private Chronometer cmPasstime;
	private Button btControl;
	private TextView tvCalorie;
	private TextView tvDistance;
	private TextView tvSpeed;

	private int steps;
	private int seconds;

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			steps = AccelerometerSensorListener.CURRENT_SETP;
			float percent = steps * 100 / pbPercent.getMax();
			tvPercent.setText(String.valueOf(percent) + "%");
			pbPercent.setProgress(steps);
			tvSteps.setText("" + steps);

		};
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.view = inflater.inflate(R.layout.tab_fragment_step, container,
				false);

		mSubThread();

		initView();

		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	private void mSubThread() {
		if (thread == null) {
			thread = new Thread(new Runnable() {
				public void run() {
					while (true) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (AccelerometerSensorService.isRun) {
							Message msg = new Message();
							handler.sendMessage(msg);
						}
					}
				}
			});
			thread.start();
		}

	}

	private void initView() {
		tvPercent = (TextView) view.findViewById(R.id.tv_percent);
		pbPercent = (ProgressBar) view.findViewById(R.id.pb_percent);
		tvGoal = (TextView) view.findViewById(R.id.tv_goal);
		tvSteps = (TextView) view.findViewById(R.id.tv_steps);
		btReset = (Button) view.findViewById(R.id.bt_reset);
		// tvPasstime = (TextView) view.findViewById(R.id.tv_passtime);
		cmPasstime = (Chronometer) view.findViewById(R.id.cm_passtime);
		btControl = (Button) view.findViewById(R.id.bt_control);
		tvCalorie = (TextView) view.findViewById(R.id.tv_calorie);
		tvDistance = (TextView) view.findViewById(R.id.tv_distance);
		tvSpeed = (TextView) view.findViewById(R.id.tv_speed);
		tvGoal.setOnClickListener(this);
		btReset.setOnClickListener(this);
		btControl.setOnClickListener(this);
		pbPercent.setMax(10000);
		cmPasstime.setOnChronometerTickListener(this);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			// 相当于Fragment的onResume
			// Log.v("tag", "resume");
			// Toast.makeText(getActivity(), "resume",
			// Toast.LENGTH_SHORT).show();
		} else {
			// 相当于Fragment的onPause
			// Log.v("tag", "pause");

		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(getActivity(),
				AccelerometerSensorService.class);

		switch (view.getId()) {
		case R.id.tv_goal:
			final EditText editText = new EditText(getActivity());
			new AlertDialog.Builder(getActivity())
					.setTitle("请输入")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setView(editText)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									tvGoal.setText(editText.getText()
											.toString().trim());
									pbPercent.setMax(Integer.parseInt(editText
											.getText().toString().trim()));
								}
							}).setNegativeButton("取消", null).show();
			break;
		case R.id.bt_reset:
			reset();
			break;
		case R.id.bt_control:
			if (btControl.getText().equals("开始")) {
				getActivity().startService(intent);
				cmPasstime.setBase(SystemClock.elapsedRealtime());
				cmPasstime.start();
				btControl.setText("暂停");
			} else if (btControl.getText().equals("暂停")) {
				getActivity().stopService(intent);
				cmPasstime.stop();
				btControl.setText("继续");
			} else if (btControl.getText().equals("继续")) {
				getActivity().startService(intent);
				cmPasstime.start();
				btControl.setText("暂停");
			}

			break;
		}
	}

	private void reset() {
		Intent intent = new Intent(getActivity(),
				AccelerometerSensorService.class);
		getActivity().stopService(intent);
		AccelerometerSensorListener.reset();
		steps = 0;

		tvPercent.setText("0");
		pbPercent.setProgress(0);
		tvGoal.setText("10000");
		tvSteps.setText("0");
		// tvPasstime.setText("00:00:00");
		cmPasstime.setBase(SystemClock.elapsedRealtime());
		cmPasstime.stop();
		btControl.setText("开始");
		tvCalorie.setText("0");
		tvDistance.setText("0");
		tvSpeed.setText("0");

	}

	@Override
	public void onChronometerTick(Chronometer arg0) {
		// TODO Auto-generated method stub
		seconds++;
		cmPasstime.setText(formatseconds());
	}

	public String formatseconds() {
		String hh = seconds / 3600 > 9 ? seconds / 3600 + "" : "0" + seconds / 3600;
		String mm = (seconds % 3600) / 60 > 9 ? (seconds % 3600) / 60 + "" : "0"
				+ (seconds % 3600) / 60;
		String ss = (seconds % 3600) % 60 > 9 ? (seconds % 3600) % 60 + "" : "0"
				+ (seconds % 3600) % 60;

		return hh + ":" + mm + ":" + ss;
	}
}
