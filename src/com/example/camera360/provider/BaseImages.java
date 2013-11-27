package com.example.camera360.provider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import com.example.camera360.R;

/**
 * 照片基类
 */
@SuppressLint("NewApi")
public class BaseImages {

	/**
	 * 表示当前的主机域名
	 */
	public final static String SITE_URL = "https://cloud.camera360.com/";
	
	/**
	 * 表示当前的用户域名
	 */
	public final static String USER_URL = "https://dn-c360.qbox.me/";

	/**
	 * 表示当前的图片域名
	 */
	public final static String IMAGE_URL = "http://cloud-activity.qiniudn.com/";
	
	/**
	 *	Description : imageLoadComplete 照片加载完成后的提示消息
	 *	@param Context context 表示当前context
	 *	@return 无
	 */
	public void imageLoadComplete(Context context){
		Toast.makeText(context, R.string.image_load_complete,
				Toast.LENGTH_SHORT).show();
	}
	
	/**
	 *	Description : imageLoadError 照片加载失败后的提示消息
	 *	@param Context context 表示当前context
	 *	@return 无
	 */
	public void imageLoadError(Context context){
		Toast.makeText(context, R.string.image_load_error,
				Toast.LENGTH_SHORT).show();
	}
	
}