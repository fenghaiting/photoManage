package com.example.camera360.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v4.app.Fragment;
import android.util.Log;

import com.example.camera360.server.Server;
import com.example.camera360.ui.ImageGridFragment;

public class PhotoImages extends BaseImages {
	
	/**
	 * 表示缓存当前gridFragment对象
	 */
	private ImageGridFragment gridFragment = null;
	
	/**
	 * 表示缓存最后一个照片ID,为了请求一下页面数据
	 */
	private String lastId = "";

	/**
	 * 表示设置PhotoImages私有静态对象
	 */
	private static PhotoImages self = null;

	public PhotoImages(Fragment fragment) {
		super();
		self = this;
		gridFragment = (ImageGridFragment) fragment;
		this.doImageUrls();
	}

	public PhotoImages() {
		super();
		self = this;
		this.doImageUrls();
	}

	/**
	 *	@activeName getInstance 表示PhotoImages实例对象
	 *	@param 无
	 *	@return PhotoImages
	 */
	public static PhotoImages getInstance() {
		if (self == null) {
			self = new PhotoImages();
		}
		return self;
	}

	/**
	 *	@activeName doImageUrls 表示请求照片url
	 */
	public void doImageUrls() {
		List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
		paramsList.add(new BasicNameValuePair("activityId",
				"526e399c8852d6ad68680fc3"));
		paramsList.add(new BasicNameValuePair("limit", "24"));
		if (!this.lastId.equals("")) {
			paramsList.add(new BasicNameValuePair("last", this.lastId));
		}
		Server.response = "post";
		new PhotoServer(SITE_URL + "activity/picture/hot", paramsList);
	}

	/**
	 *	@activeName setImageUrls 表示设置照片url
	 *	@param String result 表示ajax请求获取数据
	 *	@return 无
	 */
	public void setImageUrls(String result) {

		boolean flag = true;

		if (!this.lastId.equals(""))
			flag = false;

		try {
			if (result.equals("")) {
				gridFragment.initializeGridView(flag);
				return;
			}

			JSONObject object = new JSONObject(result);

			if (Integer.parseInt(object.getString("status")) == HttpStatus.SC_OK) {
				JSONObject data = object.getJSONObject("data");
				JSONArray list = data.getJSONArray("list");

				int length = list.length();
				
				if(length == 0){
					this.imageLoadComplete(gridFragment.getActivity());
				}else{
					this.lastId = list.getJSONObject(length - 1).getString("id");

					HashMap<String,String> map;
					JSONObject item;
					String key;
					JSONObject uploadUser;
					
					for (int i = 0; i < length; i++) {
						map = new HashMap<String,String>();
						item = list.getJSONObject(i);
						key = item.getString("key");
						try {
							uploadUser = item.getJSONObject("uploadUser");
							map.put("face",USER_URL + uploadUser.getString("face"));
							map.put("nickname",uploadUser.getString("nickname"));
						} catch (Exception e) {
							e.printStackTrace();
						} finally{
							map.put("id",item.getString("id"));
							map.put("imageThumbUrls", BaseImages.IMAGE_URL
									+ key + "-135");
							map.put("imageUrls", BaseImages.IMAGE_URL
									+ key + "-w480");
							map.put("desc",item.getString("desc"));
							map.put("favorite",item.getString("favorite"));
							gridFragment.arrayList.add(map);
						}
					}
				}
			}else{
				this.imageLoadError(gridFragment.getActivity());
			}

			gridFragment.initializeGridView(flag);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private class PhotoServer extends Server {

		private Task task = new Task();

		public PhotoServer(String url, String params) {
			super(params);
			task.execute(url);
		}

		public PhotoServer(String url, List<NameValuePair> params) {
			super(params);
			task.execute(url);
		}

		private class Task extends NetAndroidTask {

			@Override
			protected void onPostExecute(String result) {
				Log.v("result", result);
				setImageUrls(result);
			}
		}
	}
}
