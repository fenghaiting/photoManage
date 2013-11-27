/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.camera360.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.camera360.BuildConfig;
import com.example.camera360.R;
import com.example.camera360.animate.BaseAnimate;
import com.example.camera360.provider.BaseImages;
import com.example.camera360.server.Server;
import com.example.camera360.util.ImageCache;
import com.example.camera360.util.ImageFetcher;
import com.example.camera360.util.Utils;

@SuppressLint("InlinedApi")
public class ImageDetailActivity extends FragmentActivity implements
		OnClickListener {
	private static final String IMAGE_CACHE_DIR = "images";
	
	public static final String EXTRA_IMAGE = "extra_image";
	
	public static final String IMAGE_URL = "image_url";
	
	private static final int DURATION_MILLIS = 600;

	private ArrayList<HashMap<String,String>> arrayList = null;

	private ImagePagerAdapter mAdapter;
	
	private ImageFetcher mImageFetcher;
	
	private ViewPager mPager;

	private RelativeLayout mHeaderMenuView;
	
	private int currentIndex = 0;

	private View mFooterMenuView;

	private FrameLayout mImageDetailLayout;

	private ImageView mBackMenuView;

	private ImageView mImageLike;
	
	private TextView mImageDesc;

	private LinearLayout mImageDetailView;

	private LayoutInflater mInflater;
	
	private ProgressBar mImageDetailLoad;
	
	private TextView mImageDetailText;

	private boolean animateFlag = false;

	private int status = 0;
	
	private ImageView mUserFace;
	
	private TextView mUserNickname;
	
	private BaseAnimate baseAnimate;
	
	@SuppressWarnings("unchecked")
	@TargetApi(11)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			Utils.enableStrictMode();
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_detail_pager);
		
		arrayList = (ArrayList<HashMap<String,String>>) getIntent().getSerializableExtra(IMAGE_URL);

		// Fetch screen height and width, to use as our max size when loading
		// images as this
		// activity runs full screen
		
		baseAnimate = new BaseAnimate();
		
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		final int height = displayMetrics.heightPixels;
		final int width = displayMetrics.widthPixels;

		// For this sample we'll use half of the longest width to resize our
		// images. As the
		// image scaling ensures the image is larger than this, we should be
		// left with a
		// resolution that is appropriate for both portrait and landscape. For
		// best image quality
		// we shouldn't divide by 2, but this will use more memory and require a
		// larger memory
		// cache.
		final int longest = (height > width ? height : width) / 2;

		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(
				this, IMAGE_CACHE_DIR);
		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
													// app memory

		mImageDetailLayout = (FrameLayout) findViewById(R.id.image_detail_layout);

		mImageDetailView = (LinearLayout) findViewById(R.id.image_detail);

		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(this, longest);
		mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
		mImageFetcher.setImageFadeIn(false);

		// Set up ViewPager and backing adapter
		mAdapter = new ImagePagerAdapter(getSupportFragmentManager(),
				arrayList.size());

		mInflater = LayoutInflater.from(mImageDetailLayout.getContext());

		mPager = (ViewPager) findViewById(R.id.view_pager);
		mPager.setAdapter(mAdapter);
		mPager.setPageMargin((int) getResources().getDimension(
				R.dimen.image_detail_pager_margin));
		mPager.setOffscreenPageLimit(2);

		// Set up activity to go full screen
		getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);

		// Enable some additional newer visibility and ActionBar features to
		// create a more
		// immersive photo viewing experience
		if (Utils.hasHoneycomb()) {
			final ActionBar actionBar = getActionBar();

			// Hide title text and set home as up
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setDisplayHomeAsUpEnabled(true);

			// Hide and show the ActionBar as the visibility changes
			mPager.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
				@Override
				public void onSystemUiVisibilityChange(int vis) {
					if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
						actionBar.hide();
					} else {
						actionBar.show();
					}
				}
			});

			// Start low profile mode and hide ActionBar
			mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
			actionBar.hide();
		}

		// Set the current item based on the extra passed in to this activity
		final int extraCurrentItem = getIntent().getIntExtra(EXTRA_IMAGE, -1);
		
		currentIndex = extraCurrentItem;
		
		if (extraCurrentItem != -1) {
			mPager.setCurrentItem(extraCurrentItem);
		}
		
		mPager.setOnPageChangeListener(new ImageOnPageChangeListener());

		initHeaderMenu();
		
		addFooterMenu();
		
		initImageDetail();
		
		setImageInfo();
	}
	
	private void initImageDetail(){
		mImageDetailLoad = (ProgressBar) mImageDetailView
				.findViewById(R.id.image_detail_load);
		
		mImageDetailText = (TextView) mImageDetailView
				.findViewById(R.id.image_detail_text);
	}
	
	private void doFavoriteUsers(){
		
		HashMap<String,String> map = arrayList.get(currentIndex);
		
		if(map.containsKey("favoriteInfo")){
			mImageDetailLoad.setVisibility(View.GONE);
			
			mImageDetailText.setText(map.get("favoriteInfo"));
		}else{
			mImageDetailLoad.setVisibility(View.VISIBLE);
			
			mImageDetailText.setText(R.string.image_detail);
			
			List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
			paramsList.add(new BasicNameValuePair("activityId",
					"526e399c8852d6ad68680fc3"));
			paramsList.add(new BasicNameValuePair("pid", arrayList.get(currentIndex).get("id")));
			Server.response = "post";
			
			new FavoriteUsersServer(BaseImages.SITE_URL + "activity/picture/favoriteUsers", paramsList ,currentIndex);
		}
	}
	
	private void setFavoriteUsers(String result,int index){
		if (result.equals("")) {
			showDetailView(1, 0);
			return;
		}

		try {
			JSONObject object = new JSONObject(result);

			if (Integer.parseInt(object.getString("status")) == HttpStatus.SC_OK) {
				JSONObject data = object.getJSONObject("data");
				HashMap<String,String> map = arrayList.get(index);
				String count = data.getString("favoriteCount");
				int favoriteCount = Integer.parseInt(count);
				JSONArray favoriteUsers = data.getJSONArray("favoriteUsers");
				map.put("count", count);
				
				if(favoriteCount == 0){
					showDetailView(1, 0);
				}else{
					String item = favoriteUsers.join(",").replaceAll("\"","")+"等"+favoriteCount+"人喜欢";
					
					map.put("favoriteInfo", item);
					
					if(index == currentIndex) {
						mImageDetailLoad.setVisibility(View.GONE);
						mImageDetailText.setText(item);
					}
				}
			}else{
				showDetailView(1, 0);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private class FavoriteUsersServer extends Server {

		private Tast task = new Tast();
		
		private int index = 0;

		public FavoriteUsersServer(String url, String params) {
			super(params);
			task.execute(url);
		}

		public FavoriteUsersServer(String url, List<NameValuePair> params,int i) {
			super(params);
			index = i;
			task.execute(url);
		}

		private class Tast extends NetAndroidTask {

			@Override
			protected void onPostExecute(String result) {
				Log.v("result", result);
				setFavoriteUsers(result,index);
			}
		}
	}
	
	private void slideView(final float p1, final float p2,View view,AnimationListener animationListener){
		view.setVisibility(View.VISIBLE);
		Animation animation = baseAnimate.animateTranslate(0, 0, p1, p2, DURATION_MILLIS,true);
		animation.setAnimationListener(animationListener);
		view.startAnimation(animation);
	}
	
	private void slideHeaderView(final float p1, final float p2) {
		AnimationListener animationListener = new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				int left = mHeaderMenuView.getLeft();
				int top = mHeaderMenuView.getTop() + (int) (p2);
				int width = mHeaderMenuView.getWidth();
				int height = mHeaderMenuView.getHeight();
				mHeaderMenuView.clearAnimation();
				mHeaderMenuView.layout(left, top, left + width, top + height);
				if (p2 < 0) {
					mHeaderMenuView.setVisibility(View.GONE);
				}
				checkStatus();
			}
		};
		slideView(p1, p2,mHeaderMenuView,animationListener);
	}

	private void slideFooterView(final float p1, final float p2) {
		AnimationListener animationListener = new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				int left = mFooterMenuView.getLeft();
				int top = mFooterMenuView.getTop() + (int) (p2);
				int width = mFooterMenuView.getWidth();
				int height = mFooterMenuView.getHeight();
				mFooterMenuView.clearAnimation();
				mFooterMenuView.layout(left, top, left + width, top + height);
				if (p2 > 0) {
					mFooterMenuView.setVisibility(View.GONE);
				}
				checkStatus();
			}
		};
		slideView(p1, p2,mFooterMenuView,animationListener);
	}

	private void initHeaderMenu() {
		mHeaderMenuView = (RelativeLayout) mImageDetailLayout.findViewById(R.id.image_header_menu);
		mUserFace = (ImageView) mHeaderMenuView.findViewById(R.id.user_face);
		mUserNickname = (TextView) mHeaderMenuView.findViewById(R.id.user_nickname);
		mBackMenuView = (ImageView) mHeaderMenuView
				.findViewById(R.id.image_back_botton);
		mBackMenuView.setOnClickListener(this);
		mHeaderMenuView.setOnClickListener(this);
	}

	private void addFooterMenu() {
		mFooterMenuView = mInflater.inflate(R.layout.image_footer_menu,
				mImageDetailLayout, false);
		mImageLike = (ImageView) mFooterMenuView
				.findViewById(R.id.image_like);
		mImageLike.setOnClickListener(this);
		mImageDesc = (TextView) mFooterMenuView
				.findViewById(R.id.image_desc);
		mFooterMenuView.setOnClickListener(this);
		mImageDetailLayout.addView(mFooterMenuView, 2);
	}

	private void handleHeaderMenu() {
		int top = mHeaderMenuView.getTop();
		if (top == 0) {
			slideHeaderView(top, -mHeaderMenuView.getMeasuredHeight());
		} else {
			slideHeaderView(top, 0);
		}
	}

	private void handleFooterMenu() {
		int top = mFooterMenuView.getTop();
		DisplayMetrics dm = new DisplayMetrics();
		
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		int screenHeigh = dm.heightPixels;

		int footerHeader = mFooterMenuView.getMeasuredHeight();

		int footerTop = screenHeigh - footerHeader;

		if (top == footerTop) {
			slideFooterView(0, footerHeader);
		} else {
			slideFooterView(footerHeader, 0);
		}
	}

	private void showDetailView(final float p1, final float p2) {
		AlphaAnimation alphaAnimation = new AlphaAnimation(p1, p2);
		alphaAnimation.setDuration(DURATION_MILLIS);
		alphaAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mImageDetailView.clearAnimation();
				if (p2 == 0) {
					mImageDetailView.setVisibility(View.INVISIBLE);
				}
				checkStatus();
			}
		});
		mImageDetailView.setAnimation(alphaAnimation);
		alphaAnimation.setFillAfter(true);
	}
	
	private void handleImageDetailView(){
		HashMap<String,String> map = arrayList.get(currentIndex);
		
		if(Integer.parseInt(map.get("count")) == 0) {
			checkStatus();
		}else{
			if (mImageDetailView.getVisibility() == View.INVISIBLE) {
				mImageDetailView.setVisibility(View.VISIBLE);
				showDetailView(0, 1);
			} else {
				showDetailView(1, 0);
			}
		}
	}

	private void handleMenu() {
		if (animateFlag)
			return;
		animateFlag = true;
		status = 0;
		handleHeaderMenu();
		handleFooterMenu();
		handleImageDetailView();
	}

	private void checkStatus() {
		status++;
		if (status == 3) {
			animateFlag = false;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mImageFetcher.setExitTasksEarly(false);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mImageFetcher.closeCache();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.clear_cache:
			mImageFetcher.clearCache();
			Toast.makeText(this, R.string.clear_cache_complete_toast,
					Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	/**
	 * Called by the ViewPager child fragments to load images via the one
	 * ImageFetcher
	 */
	public ImageFetcher getImageFetcher() {
		return mImageFetcher;
	}

	/**
	 * The main adapter that backs the ViewPager. A subclass of
	 * FragmentStatePagerAdapter as there could be a large number of items in
	 * the ViewPager and we don't want to retain them all in memory at once but
	 * create/destroy them on the fly.
	 */
	private class ImagePagerAdapter extends FragmentStatePagerAdapter {
		private final int mSize;

		public ImagePagerAdapter(FragmentManager fm, int size) {
			super(fm);
			mSize = size;
		}

		@Override
		public int getCount() {
			return mSize;
		}

		@Override
		public Fragment getItem(int position) {
			return ImageDetailFragment.newInstance(arrayList.get(position).get("imageUrls"));
		}
	}
	
	public class ImageOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(int arg0) {
			currentIndex = arg0;
			setImageInfo();
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}
	
	private void setImageInfo(){
		HashMap<String,String> map = new HashMap<String,String>();
		map = arrayList.get(currentIndex);
		mImageDesc.setText(map.get("desc"));
		if(map.get("favorite").equals("0")){
			mImageLike.setImageResource(R.drawable.photo_like_active);
		}else{
			mImageLike.setImageResource(R.drawable.photo_like);
		}
		
		if(map.containsKey("face")){
			mImageFetcher.loadImage(map.get("face"), mUserFace);
		}else{
			mUserFace.setImageResource(R.drawable.blank_boy);
		}
		
		mUserNickname.setText(map.get("nickname"));
		doFavoriteUsers();
	}

	/**
	 * Set on the ImageView in the ViewPager children fragments, to
	 * enable/disable low profile mode when the ImageView is touched.
	 */
	@TargetApi(11)
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.image_back_botton:
			((ImageView) v).setImageResource(R.drawable.ic_back_active);
			finish();
			break;
		case R.id.imageView:
			handleMenu();
			break;
		case R.id.image_header_menu:
		case R.id.image_footer_menu:
			break;
		case R.id.image_like:
			HashMap<String,String> map = new HashMap<String,String>();
			map = arrayList.get(currentIndex);
			int i = 0;
			if(map.get("favorite").equals("0")){
				map.put("favorite", "1");
				i = R.drawable.photo_like;
			}else{
				map.put("favorite", "0");
				i = R.drawable.photo_like_active;
			}
			((ImageView) v).setImageResource(i);
			((ImageView) v).startAnimation(baseAnimate.animateScale(0.5f,1f,0.5f, 1f, DURATION_MILLIS,true));
			break;
		default:
			final int vis = mPager.getSystemUiVisibility();
			if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
				mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			} else {
				mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
			}
			break;
		}
	}

}
