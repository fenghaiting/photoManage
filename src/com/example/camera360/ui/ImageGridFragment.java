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

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.camera360.BuildConfig;
import com.example.camera360.R;
import com.example.camera360.main.MainActivity;
import com.example.camera360.provider.HotImages;
import com.example.camera360.provider.LatestImages;
import com.example.camera360.provider.PhotoImages;
import com.example.camera360.util.ImageCache.ImageCacheParams;
import com.example.camera360.util.ImageFetcher;
import com.example.camera360.util.RefreshView;
import com.example.camera360.util.RefreshView.OnFooterRefreshListener;
import com.example.camera360.util.RefreshView.OnHeaderRefreshListener;
import com.example.camera360.util.Utils;

/**
 * The main fragment that powers the ImageGridActivity screen. Fairly straight
 * forward GridView implementation with the key addition being the ImageWorker
 * class w/ImageCache to load children asynchronously, keeping the UI nice and
 * smooth and caching thumbnails for quick retrieval. The cache is retained over
 * configuration changes like orientation change so the images are populated
 * quickly if, for example, the user rotates the device.
 */
public class ImageGridFragment extends Fragment implements
		AdapterView.OnItemClickListener, OnHeaderRefreshListener,
		OnFooterRefreshListener {
	public static final String TAG = "imageGridFragment";
	public static final String IMAGE_CACHE_DIR = "thumbs";

	public int mImageThumbSize;

	public int mImageThumbSpacing;

	public ImageAdapter mAdapter;

	public ImageFetcher mImageFetcher;

	public ArrayList<HashMap<String,String>> arrayList;
	
	private FragmentActivity activity;

	private RefreshView gridView;
	
	/**
	 * footer progress bar
	 */
	private FrameLayout mFrameLayout;
	
	public ImageGridFragment(){
		this.arrayList = new ArrayList<HashMap<String,String>>();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

		activity = getActivity();

		mImageThumbSize = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_spacing);

		mAdapter = new ImageAdapter(activity);

		ImageCacheParams cacheParams = new ImageCacheParams(activity,
				IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
													// app memory

		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(activity, mImageThumbSize);
		mImageFetcher.setLoadingImage(R.drawable.empty_photo);
		mImageFetcher.addImageCache(activity.getSupportFragmentManager(),
				cacheParams);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		gridView = (RefreshView) inflater.inflate(R.layout.image_grid_fragment,
				container, false);
		
		mFrameLayout = (FrameLayout) gridView.findViewById(R.id.frame_layout_load);
		
		mFrameLayout.setVisibility(View.VISIBLE);

		if (arrayList.size() != 0) {
			initializeGridView(true);
		}

		return gridView;
	}

	public void initializeGridView(boolean flag) {
		
		mFrameLayout.setVisibility(View.GONE);
		
		final GridView mGridView = (GridView) gridView
				.findViewById(R.id.gridView);
		
		if(flag){
			mGridView.setOnItemClickListener(this);
			mGridView.setAdapter(mAdapter);
			mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
				@Override
				public void onScrollStateChanged(AbsListView absListView,
						int scrollState) {
					// Pause fetcher to ensure smoother scrolling when flinging
					if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
						mImageFetcher.setPauseWork(true);
					} else {
						mImageFetcher.setPauseWork(false);
					}
				}

				@Override
				public void onScroll(AbsListView absListView, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
				}
			});

			// This listener is used to get the final width of the GridView and then
			// calculate the
			// number of columns and the width of each column. The width of each
			// column is variable
			// as the GridView has stretchMode=columnWidth. The column width is used
			// to set the height
			// of each view so we get nice square thumbnails.
			mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
					new ViewTreeObserver.OnGlobalLayoutListener() {
						@Override
						public void onGlobalLayout() {
							if (mAdapter.getNumColumns() == 0) {
								final int numColumns = (int) Math.floor(mGridView
										.getWidth()
										/ (mImageThumbSize + mImageThumbSpacing));
								if (numColumns > 0) {
									final int columnWidth = (mGridView.getWidth() / numColumns)
											- mImageThumbSpacing;
									mAdapter.setNumColumns(numColumns);
									mAdapter.setItemHeight(columnWidth);
									if (BuildConfig.DEBUG) {
										Log.d(TAG,
												"onCreateView - numColumns set to "
														+ numColumns);
									}
								}
							}
						}
					});

			gridView.setOnHeaderRefreshListener(this);

			gridView.setOnFooterRefreshListener(this);
		}else{
			gridView.onFooterRefreshComplete();
			
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onFooterRefresh(RefreshView view) {
		switch (MainActivity.getInstance().currIndex) {
			case 0:
				HotImages.getInstance().doImageUrls();
				break;
			case 1:
				LatestImages.getInstance().doImageUrls();
				break;
			case 2:
				PhotoImages.getInstance().doImageUrls();
				break;
			}
	}

	@Override
	public void onHeaderRefresh(RefreshView view) {
		gridView.postDelayed(new Runnable() {
			@Override
			public void run() {
				int desc = 0;
				switch (MainActivity.getInstance().currIndex) {
				case 0:
					desc = R.string.hot_photo_complete;
					break;
				case 1:
					desc = R.string.lastest_photo_complete;
					break;
				case 2:
					desc = R.string.gallery_photo_complete;
					break;
				}
				Toast.makeText(activity, desc,
						Toast.LENGTH_SHORT).show();
				gridView.onHeaderRefreshComplete();
			}
		}, 1000);

	}

	@Override
	public void onResume() {
		super.onResume();
		mImageFetcher.setExitTasksEarly(false);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();
		mImageFetcher.setPauseWork(false);
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mImageFetcher.closeCache();
	}

	@TargetApi(16)
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		final Intent i = new Intent(activity, ImageDetailActivity.class);
		i.putExtra(ImageDetailActivity.EXTRA_IMAGE, (int) id);
		i.putExtra(ImageDetailActivity.IMAGE_URL, arrayList);
		if (Utils.hasJellyBean()) {
			// makeThumbnailScaleUpAnimation() looks kind of ugly here as the
			// loading spinner may
			// show plus the thumbnail image in GridView is cropped. so using
			// makeScaleUpAnimation() instead.
			ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v,
					0, 0, v.getWidth(), v.getHeight());
			activity.startActivity(i, options.toBundle());
		} else {
			startActivity(i);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.clear_cache:
			mImageFetcher.clearCache();
			Toast.makeText(activity, R.string.clear_cache_complete_toast,
					Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * The main adapter that backs the GridView. This is fairly standard except
	 * the number of columns in the GridView is used to create a fake top row of
	 * empty views as we use a transparent ActionBar and don't want the real top
	 * row of images to start off covered by it.
	 */
	private class ImageAdapter extends BaseAdapter {

		private final Context mContext;
		private int mItemHeight = 0;
		private int mNumColumns = 0;
		private int mActionBarHeight = 0;
		private GridView.LayoutParams mImageViewLayoutParams;

		public ImageAdapter(Context context) {
			super();
			mContext = context;
			mImageViewLayoutParams = new GridView.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			// Calculate ActionBar height
			TypedValue tv = new TypedValue();
			if (context.getTheme().resolveAttribute(
					android.R.attr.actionBarSize, tv, true)) {
				mActionBarHeight = TypedValue.complexToDimensionPixelSize(
						tv.data, context.getResources().getDisplayMetrics());
			}
		}

		@Override
		public int getCount() {
			// Size + number of columns for top empty row
			return arrayList.size() + mNumColumns;
		}

		@Override
		public Object getItem(int position) {
			return position < mNumColumns ? null : arrayList.get(position
					- mNumColumns).get("imageThumbUrls");
		}

		@Override
		public long getItemId(int position) {
			return position < mNumColumns ? 0 : position - mNumColumns;
		}

		@Override
		public int getViewTypeCount() {
			// Two types of views, the normal ImageView and the top row of empty
			// views
			return 2;
		}

		@Override
		public int getItemViewType(int position) {
			return (position < mNumColumns) ? 1 : 0;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup container) {
			// First check if this is the top row
			if (position < mNumColumns) {
				if (convertView == null) {
					convertView = new View(mContext);
				}
				// Set empty view with height of ActionBar
				convertView.setLayoutParams(new AbsListView.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, mActionBarHeight));
				return convertView;
			}

			// Now handle the main ImageView thumbnails
			ImageView imageView;
			if (convertView == null) { // if it's not recycled, instantiate and
										// initialize
				imageView = new RecyclingImageView(mContext);
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setLayoutParams(mImageViewLayoutParams);
			} else { // Otherwise re-use the converted view
				imageView = (ImageView) convertView;
			}

			// Check the height matches our calculated column width
			if (imageView.getLayoutParams().height != mItemHeight) {
				imageView.setLayoutParams(mImageViewLayoutParams);
			}

			// Finally load the image asynchronously into the ImageView, this
			// also takes care of
			// setting a placeholder image while the background thread runs
			HashMap<String,String> map = arrayList.get(position - mNumColumns);
			mImageFetcher.loadImage(map.get("imageThumbUrls"),
					imageView);
			return imageView;
		}

		/**
		 * Sets the item height. Useful for when we know the column width so the
		 * height can be set to match.
		 * 
		 * @param height
		 */
		public void setItemHeight(int height) {
			if (height == mItemHeight) {
				return;
			}
			mItemHeight = height;
			mImageViewLayoutParams = new GridView.LayoutParams(
					LayoutParams.MATCH_PARENT, mItemHeight);
			mImageFetcher.setImageSize(height);
			notifyDataSetChanged();
		}

		public void setNumColumns(int numColumns) {
			mNumColumns = numColumns;
		}
		
		public int getNumColumns() {
			return mNumColumns;
		}
	}
}
