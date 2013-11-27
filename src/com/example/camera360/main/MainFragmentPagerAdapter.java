package com.example.camera360.main;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.camera360.provider.HotImages;
import com.example.camera360.provider.LatestImages;
import com.example.camera360.provider.PhotoImages;

public class MainFragmentPagerAdapter extends FragmentPagerAdapter {
	private ArrayList<Fragment> fragmentsList;
	
	public HotImages hotImages = null;
	
	public LatestImages latestImages = null;
	
	public PhotoImages photoImages = null;

	public MainFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	public MainFragmentPagerAdapter(FragmentManager fm,
			ArrayList<Fragment> fragments) {
		super(fm);
		this.fragmentsList = fragments;
	}

	@Override
	public int getCount() {
		return fragmentsList.size();
	}

	@Override
	public Fragment getItem(int arg0) {
		
		Fragment fragment = fragmentsList.get(arg0);
		
		switch(arg0){
			case 0:
				new HotImages(fragment);
				break;
			case 1:
				new LatestImages(fragment);
				break;
			case 2:
				new PhotoImages(fragment);
				break;
		}
		return fragment;
	}

	@Override
	public int getItemPosition(Object object) {
		return super.getItemPosition(object);
	}

}