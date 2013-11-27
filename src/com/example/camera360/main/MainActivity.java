package com.example.camera360.main;

import java.util.ArrayList;

import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.camera360.R;
import com.example.camera360.ui.ImageGridFragment;

public class MainActivity extends FragmentActivity {
	
	/**
	 * 表示tab字体变化的颜色(包括红色和黑色定义)
	 */
	private static final int YELLOW_COLOR = Color.rgb(255,224,73), WHITE_COLOR = Color
			.rgb(255,255,255);
	
	/**
	 * 表示tab水平线的动画时间
	 */
	private static final int CURSOR_ANIMATION_TIME = 350;
	
	/**
	 * 表示MainActivity实例对象
	 */
	private static MainActivity self = null;

	/**
	 * 表示页卡内容
	 */
	private ViewPager viewPager;
	
	/**
	 * 表示开机动画视图
	 */
	private ImageView imageAnimate = null;

	/**
	 * 表示Tab页面列表数组
	 */
	private ArrayList<Fragment> fragmentsList;

	/**
	 * 表示TextView的tab
	 */
	private TextView tvTabHot, tvTabLatest, tvTabGallery;

	/**
	 * 表示TextView的tab水平线
	 */
	private View cursor;

	/**
	 * 表示tab头的宽度
	 */
	private int tabWidth = 0;

	/**
	 * 表示tab头的宽度减去动画图片的宽度再除以2(保证动画图片相对tab头居中)
	 */
	private int offsetX = 0;

	/**
	 * 表示开机逐帧动画对象
	 */
	private AnimationDrawable animationDrawable = null;

	/**
	 * 表示当前的渲染tab标签
	 */
	private TextView current = null;
	
	private View mHeaderMenuView;
	
	private ImageView mBackMenuView;
	
	private LayoutInflater mInflater;
	
	private LinearLayout mMainLayout;
	
	/**
	 * 表示当前的页卡编号
	 */
	public int currIndex = 0;
	
	public MainActivity() {
		self = this;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		// Set up activity to go full screen
		getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);
		addHeaderMenu();
		initTextView();
		initViewPager();
		initCursorView();
		initAnimate();
	}
	
	/**
	 *	@activeName getInstance 表示MainActivity实例对象
	 *	@param 无
	 *	@return MainActivity
	 */
	public static MainActivity getInstance() {
		if (self == null) {
			self = new MainActivity();
		}
		return self;
	}
	
	private void addHeaderMenu() {
		mMainLayout = (LinearLayout) findViewById(R.id.main_view);
		
		mInflater = LayoutInflater.from(mMainLayout.getContext());
		
		mHeaderMenuView = mInflater.inflate(R.layout.image_header_menu, mMainLayout, false);
		
		mBackMenuView = (ImageView) mHeaderMenuView
				.findViewById(R.id.image_back_botton);
		
		mBackMenuView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				((ImageView) v).setImageResource(R.drawable.ic_back_active);
				finish();
			}
		});
	}
	
	/**
	 * 初始化开机视图
	 */
	private void initAnimate() {
		
		imageAnimate = (ImageView) findViewById(R.id.imageAnimate);

		imageAnimate.setBackgroundResource(R.drawable.image_animate_list);

		animationDrawable = (AnimationDrawable) imageAnimate.getBackground();

		OnPreDrawListener onPreDrawListener = new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				animationDrawable.start();
				return true;
			}
		};
		
		imageAnimate.getViewTreeObserver().addOnPreDrawListener(onPreDrawListener);
		
		int duration = 500;
		
        for(int i = 0;i < animationDrawable.getNumberOfFrames();i++){
            duration += animationDrawable.getDuration(i);
        }
        
		Handler handler = new Handler();
		
        handler.postDelayed(new Runnable() {
            public void run() {
            	imageAnimate.setVisibility(View.GONE);
            	mMainLayout.addView(mHeaderMenuView, 0);
            }
        }, duration);
	}
	
	/**
	 * 初始化tab视图
	 */
	private void initTextView() {
		tvTabHot = (TextView) findViewById(R.id.hot_tab);
		tvTabLatest = (TextView) findViewById(R.id.latest_tab);
		tvTabGallery = (TextView) findViewById(R.id.gallery_tab);

		tvTabHot.setOnClickListener(new MainOnClickListener(0));
		tvTabLatest.setOnClickListener(new MainOnClickListener(1));
		tvTabGallery.setOnClickListener(new MainOnClickListener(2));

		this.setCursorColor();
	}
	
	/**
	 * 初始化主页面视图
	 */
	private void initViewPager() {
		viewPager = (ViewPager) findViewById(R.id.view_pager);
		fragmentsList = new ArrayList<Fragment>();

		Fragment hotfragment = new ImageGridFragment();
		Fragment latestFragment = new ImageGridFragment();
		Fragment galleryFragment = new ImageGridFragment();

		fragmentsList.add(hotfragment);
		fragmentsList.add(latestFragment);
		fragmentsList.add(galleryFragment);

		viewPager.setAdapter(new MainFragmentPagerAdapter(
				getSupportFragmentManager(), fragmentsList));
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new MainOnPageChangeListener());
	}

	/**
	 * 初始化水平线视图
	 */
	private void initCursorView() {

		cursor = (View) findViewById(R.id.cursor);

		DisplayMetrics dm = getResources().getDisplayMetrics();

		int screenW = dm.widthPixels;// 获取分辨率宽度

		tabWidth = screenW / fragmentsList.size();

		android.view.ViewGroup.LayoutParams param = cursor.getLayoutParams();

		param.width = tabWidth;

		cursor.setLayoutParams(param);
	}

	/**
	 * 设置cursor颜色
	 */
	private void setCursorColor() {

		TextView target = null;

		switch (currIndex) {
		case 0:
			target = tvTabHot;
			break;
		case 1:
			target = tvTabLatest;
			break;
		case 2:
			target = tvTabGallery;
			break;
		}

		if (this.current != null) {
			this.current.setTextColor(WHITE_COLOR);
		}

		target.setTextColor(YELLOW_COLOR);

		this.current = target;
	}

	public class MainOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MainOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			viewPager.setCurrentItem(index);
		}
	};

	public class MainOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(int arg0) {
			Animation cursorAnimation = new TranslateAnimation(tabWidth
					* currIndex + offsetX, tabWidth * arg0 + offsetX, 0, 0);
			currIndex = arg0;
			// true:动画停留结束位置,默认false:动画还原开始位置
			cursorAnimation.setFillAfter(true);
			
			cursorAnimation.setDuration(CURSOR_ANIMATION_TIME);
			
			cursor.startAnimation(cursorAnimation);

			MainActivity.this.setCursorColor();
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}
}