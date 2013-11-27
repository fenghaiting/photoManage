package com.example.camera360.animate;

import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class BaseAnimate extends Animation{
	
	public Animation animateScale(float fromX,float toX,float fromY, float toY,long durationMillis,boolean fillAfter){
		Animation animation = new ScaleAnimation(fromX, toX, fromY, toY, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new OvershootInterpolator());
		animation.setDuration(durationMillis);
		animation.setFillAfter(fillAfter);
		return animation;
	}
	
	public Animation animateRotate(float toX, float toY,long durationMillis,boolean fillAfter){
		Animation animation = new RotateAnimation(toX, toY,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new OvershootInterpolator());
		animation.setDuration(durationMillis);
		animation.setFillAfter(fillAfter);
		return animation;
	}
	
	public Animation animateTranslate(float fromX,float toX,float fromY, float toY,long durationMillis,boolean fillAfter){
		Animation animation = new TranslateAnimation(fromX, toX, fromY, toY);
		animation.setInterpolator(new OvershootInterpolator());
		animation.setDuration(durationMillis);
		animation.setFillAfter(fillAfter);
		return animation;
	}
}
