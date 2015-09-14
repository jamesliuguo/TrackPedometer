package com.zhenqianfan13354468.trackpedometer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 自定义曲线控件
 *
 */
@SuppressLint("ViewConstructor")
class ChartView extends View {

	public static final int RECT_SIZE = 10;
	private Point mSelectedPoint;

	// 图标风格
	public static enum Mstyle {
		Line, Curve
	}

	private Mstyle mstyle = Mstyle.Line;
	private Point[] mPoints = new Point[100];

	Context context;
	int bheight = 0;
	HashMap<Double, Double> map;
	ArrayList<Double> dlk;
	int totalvalue = 30;
	int pjvalue = 5;
	String xstr, ystr = "";// 横纵坐标的属性
	int margint = 15;
	int marginb = 40;
	int c = 0;
	int resid = 0;
	Boolean isylineshow;

	/**
	 * @param map
	 *            需要的数据，虽然key是double，但是只用于排序和显示，与横向距离无关
	 * @param totalvalue
	 *            Y轴的最大值
	 * @param pjvalue
	 *            Y平均值
	 * @param xstr
	 *            X轴的单位
	 * @param ystr
	 *            Y轴的单位
	 * @param isylineshow
	 *            是否显示纵向网格
	 * @return
	 */
	public void SetTuView(HashMap<Double, Double> map, int totalvalue,
			int pjvalue, String xstr, String ystr, Boolean isylineshow) {
		this.map = map;
		this.totalvalue = totalvalue;
		this.pjvalue = pjvalue;
		this.xstr = xstr;
		this.ystr = ystr;
		this.isylineshow = isylineshow;
		// 屏幕横向
		// act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	public ChartView(Context ct) {
		super(ct);
		this.context = ct;
	}

	public ChartView(Context ct, AttributeSet attrs) {
		super(ct, attrs);
		this.context = ct;
	}

	public ChartView(Context ct, AttributeSet attrs, int defStyle) {
		super(ct, attrs, defStyle);
		this.context = ct;
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		LightSensorService.canvas = canvas;
		if (c != 0)
			this.setbg(c);
		if (resid != 0)
			this.setBackgroundResource(resid);
		dlk = getintfrommap(map);
		int height = getHeight();
		if (bheight == 0)
			bheight = height - marginb;

		int width = getWidth();
		int blwidh = dip2px(context, 50);
		int pjsize = totalvalue / pjvalue;// 界面布局的尺寸的比例
		// set up paint
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.GRAY);
		paint.setStrokeWidth(1);
		paint.setStyle(Style.STROKE);
		for (int i = 0; i < pjsize + 1; i++)// 将顶点的线变为红色的 警戒线
		{
			if (i == pjsize)
				paint.setColor(Color.RED);
			canvas.drawLine(blwidh, bheight - (bheight / pjsize) * i + margint,
					width, bheight - (bheight / pjsize) * i + margint, paint);// Y坐标
			drawline(pjvalue * i + "", blwidh / 2, bheight - (bheight / pjsize)
					* i + margint, canvas, true);
		}
		ArrayList<Integer> xlist = new ArrayList<Integer>();// 记录每个x的值
		// 画直线（纵向）
		paint.setColor(Color.GRAY);
		if (dlk == null)
			return;
		for (int i = 0; i < dlk.size(); i++) {
			xlist.add(blwidh + (width - blwidh) / dlk.size() * i);
			if (isylineshow) {
				canvas.drawLine(blwidh + (width - blwidh) / dlk.size() * i,
						margint, blwidh + (width - blwidh) / dlk.size() * i,
						bheight + margint, paint);
			}
			drawline(Math.round(dlk.get(i)) + "", blwidh + (width - blwidh)
					/ dlk.size() * i, bheight + 40, canvas, false);// X坐标
		}

		// 点的操作设置
		mPoints = getpoints(dlk, map, xlist, totalvalue, bheight);

		paint.setColor(Color.GREEN);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(0);

		if (mstyle == Mstyle.Curve)
			drawscrollline(mPoints, canvas, paint);
		else
			drawline(mPoints, canvas, paint);

		paint.setColor(Color.RED);
		paint.setStyle(Style.FILL);
		for (int i = 0; i < mPoints.length; i++) {
			canvas.drawRect(pointToRect(mPoints[i]), paint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			for (int i = 0; i < mPoints.length; i++) {
				if (pointToRect(mPoints[i])
						.contains(event.getX(), event.getY())) {
					System.out.println("-yes-" + i);
					mSelectedPoint = mPoints[i];
				}
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (null != mSelectedPoint) {
				mSelectedPoint.y = (int) event.getY();
			}
			break;
		case MotionEvent.ACTION_UP:
			mSelectedPoint = null;
			break;
		default:
			break;
		}
		return true;

	}

	private RectF pointToRect(Point p) {
		return new RectF(p.x - RECT_SIZE / 2, p.y - RECT_SIZE / 2, p.x
				+ RECT_SIZE / 2, p.y + RECT_SIZE / 2);
	}

	private void drawscrollline(Point[] ps, Canvas canvas, Paint paint) {
		Point startp = new Point();
		Point endp = new Point();
		for (int i = 0; i < ps.length - 1; i++) {
			startp = ps[i];
			endp = ps[i + 1];
			int wt = (startp.x + endp.x) / 2;
			Point p3 = new Point();
			Point p4 = new Point();
			p3.y = startp.y;
			p3.x = wt;
			p4.y = endp.y;
			p4.x = wt;

			Path path = new Path();
			path.moveTo(startp.x, startp.y);
			path.cubicTo(p3.x, p3.y, p4.x, p4.y, endp.x, endp.y);
			canvas.drawPath(path, paint);

		}
	}

	private void drawline(Point[] ps, Canvas canvas, Paint paint) {
		Point startp = new Point();
		Point endp = new Point();
		for (int i = 0; i < ps.length - 1; i++) {
			startp = ps[i];
			endp = ps[i + 1];
			canvas.drawLine(startp.x, startp.y, endp.x, endp.y, paint);
		}
	}

	private Point[] getpoints(ArrayList<Double> dlk,
			HashMap<Double, Double> map, ArrayList<Integer> xlist, int max,
			int h) {
		Point[] points = new Point[dlk.size()];
		for (int i = 0; i < dlk.size(); i++) {
			int ph = h - (int) (h * (map.get(dlk.get(i)) / max));

			points[i] = new Point(xlist.get(i), ph + margint);
		}
		return points;
	}

	private void drawline(String text, int x, int y, Canvas canvas, boolean flag) {
		Paint p = new Paint();
		p.setAlpha(0x0000ff);
		p.setTextSize(20);
		String familyName = "宋体";
		Typeface font = Typeface.create(familyName, Typeface.ITALIC);
		p.setTypeface(font);
		p.setTextAlign(Paint.Align.CENTER);
		canvas.drawText(text, x, y, p);
	}

	public int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	@SuppressWarnings("rawtypes")
	public ArrayList<Double> getintfrommap(HashMap<Double, Double> map) {
		ArrayList<Double> dlk = new ArrayList<Double>();
		int position = 0;
		if (map == null)
			return null;
		Set set = map.entrySet();
		Iterator iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry mapentry = (Map.Entry) iterator.next();
			dlk.add((Double) mapentry.getKey());
		}
		for (int i = 0; i < dlk.size(); i++) {
			int j = i + 1;
			position = i;
			Double temp = dlk.get(i);
			for (; j < dlk.size(); j++) {
				if (dlk.get(j) < temp) {
					temp = dlk.get(j);
					position = j;
				}
			}

			dlk.set(position, dlk.get(i));
			dlk.set(i, temp);
		}
		return dlk;

	}

	public void setbg(int c) {
		this.setBackgroundColor(c);
	}

	public HashMap<Double, Double> getMap() {
		return map;
	}

	public void setMap(HashMap<Double, Double> map) {
		this.map = map;
	}

	public int getTotalvalue() {
		return totalvalue;
	}

	public void setTotalvalue(int totalvalue) {
		this.totalvalue = totalvalue;
	}

	public int getPjvalue() {
		return pjvalue;
	}

	public void setPjvalue(int pjvalue) {
		this.pjvalue = pjvalue;
	}

	public String getXstr() {
		return xstr;
	}

	public void setXstr(String xstr) {
		this.xstr = xstr;
	}

	public String getYstr() {
		return ystr;
	}

	public void setYstr(String ystr) {
		this.ystr = ystr;
	}

	public int getMargint() {
		return margint;
	}

	public void setMargint(int margint) {
		this.margint = margint;
	}

	public Boolean getIsylineshow() {
		return isylineshow;
	}

	public void setIsylineshow(Boolean isylineshow) {
		this.isylineshow = isylineshow;
	}

	public int getMarginb() {
		return marginb;
	}

	public void setMarginb(int marginb) {
		this.marginb = marginb;
	}

	public Mstyle getMstyle() {
		return mstyle;
	}

	public void setMstyle(Mstyle mstyle) {
		this.mstyle = mstyle;
	}

	public int getBheight() {
		return bheight;
	}

	public void setBheight(int bheight) {
		this.bheight = bheight;
	}

	public int getC() {
		return c;
	}

	public void setC(int c) {
		this.c = c;
	}

	public int getResid() {
		return resid;
	}

	public void setResid(int resid) {
		this.resid = resid;
	}

}