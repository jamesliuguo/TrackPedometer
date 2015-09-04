package com.zhenqianfan13354468.trackpedometer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class TabFragmentStep extends Fragment implements OnClickListener {
	
	private View view;
	private Thread thread;
	
	private TextView tvSteps;
	private Button btStart, btPause;
	
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			tvSteps.setText(""+AccelerometerSensorListener.CURRENT_SETP);
		};
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.view = inflater.inflate(R.layout.tab_fragment_step, container, false);
		
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
		tvSteps = (TextView)view.findViewById(R.id.steps);
		btStart = (Button)view.findViewById(R.id.id_bt_start);
		btPause = (Button)view.findViewById(R.id.id_bt_pause);
		btStart.setOnClickListener(this);
		btPause.setOnClickListener(this);
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			// 相当于Fragment的onResume
			//Log.v("tag", "resume");
			// Toast.makeText(getActivity(), "resume", Toast.LENGTH_SHORT).show();
		} else {
			// 相当于Fragment的onPause
			//Log.v("tag", "pause");

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
		Intent intent = new Intent(getActivity(), AccelerometerSensorService.class);
		
		switch(view.getId()) {
		case R.id.id_bt_start:
			getActivity().startService(intent);

			break;
		case R.id.id_bt_pause:
			getActivity().stopService(intent);
			break;
		}
	}

}
