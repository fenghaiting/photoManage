package com.example.camera360.animate;

import android.R.anim;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.camera360.R;

public class AnimButtons extends LinearLayout{

	private final int BUTTON_WIDTH = 50;		//图片宽高
	private final int RADIUS = 180;				//半径
	private final int MAX_TIME_SPENT = 600;		//最长动画耗时
	private final int MIN_TIME_SPENT = 200;		//最短动画耗时
	private final int ANIM_ROTATE_TIME = 300;	//图标旋转的时间
	private final int ANIM_SCALE_TIME = 400;	//图标放大的时间
	
	private Context context;
	private int leftMargin = 0,bottomMargin = 0,intervalTimeSpent;	//每相邻2个的时间间隔
	private Button[] btns;
	private Button btn_menu;
	private RelativeLayout.LayoutParams params;
	private boolean isOpen = false;									//是否菜单打开状态
	private float angle;											//每个按钮之间的夹角
	
	public AnimButtons(Context context) {
		super(context);
		this.context=context;
	}
	public AnimButtons(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context=context;
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		View view=LayoutInflater.from(context).inflate(R.layout.anim_buttons, this);
		
		initButtons(view);
		
	}

	private void initButtons(View view){
		//6个按钮，具体视情况而定
		btns=new Button[6];
		
		btns[0] = (Button) view.findViewById(R.id.btn_sina);
		btns[1] = (Button) view.findViewById(R.id.btn_qzone);
		btns[2] = (Button) view.findViewById(R.id.btn_qq);
		btns[3] = (Button) view.findViewById(R.id.btn_renren);
		btns[4] = (Button) view.findViewById(R.id.btn_facebook);
		btns[5] = (Button) view.findViewById(R.id.btn_twitter);
		
		leftMargin=((RelativeLayout.LayoutParams)(btn_menu.getLayoutParams())).leftMargin;
		bottomMargin=((RelativeLayout.LayoutParams)(btn_menu.getLayoutParams())).bottomMargin;
		
		for(int i=0;i<btns.length;i++){
			btns[i].setLayoutParams(btn_menu.getLayoutParams());//初始化的时候按钮都重合
			btns[i].setTag(String.valueOf(i));
			btns[i].setOnClickListener(clickListener);
		}
		
		intervalTimeSpent=(MAX_TIME_SPENT-MIN_TIME_SPENT)/btns.length;//20
		angle=(float)Math.PI/(2*(btns.length-1));
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		final int bottomMargins = this.getMeasuredHeight()-BUTTON_WIDTH-bottomMargin;
		btn_menu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(isOpen){
					isOpen = false;
					btn_menu.startAnimation(animRotate(0,360));
					for(int i=0;i<btns.length;i++){
						float xLenth=(float)(RADIUS*Math.sin(i*angle));
						float yLenth=(float)(RADIUS*Math.cos(i*angle));
						btns[i].startAnimation(animTranslate(-xLenth, yLenth, leftMargin, bottomMargins, btns[i], MAX_TIME_SPENT-i*intervalTimeSpent));
					}
				}else{
					btn_menu.startAnimation(animRotate(360,0));
					isOpen = true;
					for(int i=0;i<btns.length;i++){
						float xLenth=(float)(RADIUS*Math.sin(i*angle));
						float yLenth=(float)(RADIUS*Math.cos(i*angle));
						btns[i].startAnimation(animTranslate(xLenth, -yLenth, leftMargin+(int)xLenth, bottomMargins - (int)yLenth, btns[i], MIN_TIME_SPENT+i*intervalTimeSpent));
					}
				}
			}
		});
		
	}
	
	private Animation animScale(float toX, float toY){
		Animation animation = new ScaleAnimation(1.0f, toX, 1.0f, toY, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(context, anim.accelerate_decelerate_interpolator);
		animation.setDuration(ANIM_SCALE_TIME);
		animation.setFillAfter(false);
		return animation;
	}
	
	private Animation animRotate(float toX, float toY){
		Animation animation = new RotateAnimation(toX, toY,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(ANIM_ROTATE_TIME);
		animation.setFillAfter(true);
		return animation;
	}
	
	private Animation animTranslate(float toX, float toY, final int lastX, final int lastY,
			final Button button, long durationMillis){
		Animation animation = new TranslateAnimation(0, toX, 0, toY);				
		animation.setAnimationListener(new AnimationListener(){
						
			@Override
			public void onAnimationStart(Animation animation){
								
			}
						
			@Override
			public void onAnimationRepeat(Animation animation) {
							
			}
						
			@Override
			public void onAnimationEnd(Animation animation){
				params = new RelativeLayout.LayoutParams(0, 0);
				params.height = BUTTON_WIDTH;
				params.width = BUTTON_WIDTH;											
				params.setMargins(lastX, lastY, 0, 0);
				button.setLayoutParams(params);
				button.clearAnimation();
						
			}
		});
		animation.setDuration(durationMillis);
		return animation;
	}
	
	View.OnClickListener clickListener = new View.OnClickListener(){

		@Override
		public void onClick(View v) {
			int selectedItem=Integer.parseInt((String)v.getTag());
			for(int i=0;i<btns.length;i++){
				if(i==selectedItem){
					btns[i].startAnimation(animScale(1.5f, 1.5f));
				}else{
					btns[i].startAnimation(animScale(0.5f, 0.5f));
				}
			}
			if(onButtonClickListener!=null){
				onButtonClickListener.onButtonClick(v, selectedItem);
			}
		}
		
	};
	
	public boolean isOpen(){
		return isOpen;
	}
	
	private OnButtonClickListener onButtonClickListener;
	public interface OnButtonClickListener{
		void onButtonClick(View v,int id);
	}
	public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener){
		this.onButtonClickListener=onButtonClickListener;
	}
}
