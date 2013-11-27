package com.example.camera360.server;

import java.security.KeyStore;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;

public class Server {

	public static String response = "post";

	private List<NameValuePair> postParams = null;

	private String getParams = null;

	public Server(String params) {
		this.getParams = params;
	}

	public Server(List<NameValuePair> params) {
		this.postParams = params;
	}

	public static HttpClient getNewHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactoryEx sf = new SSLSocketFactoryEx(trustStore);
			sf.setHostnameVerifier(SSLSocketFactoryEx.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(
					params, registry);

			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}
	
	public class NetAndroidTask extends AsyncTask<String, Integer, String> {

		protected String handleCallback(HttpResponse httpResponse) {
			try {
				String result = EntityUtils.toString(
						httpResponse.getEntity()).replaceAll("\r", "");
				return result;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "";
		}

		@Override
		protected String doInBackground(String... params) {
			// 声明变量
			HttpGet httpGet = null;
			HttpPost httpPost = null;
			HttpResponse httpResponse = null;

			try {
				if (response.equals("get")) {
					// 第1步：创建HttpGet对象,并设置请求参数
					httpGet = new HttpGet(params[0] + Server.this.getParams);

					// 第2步：使用execute方法HTTP发送GET请求，并返回HttpResponse对象
					httpResponse = (HttpResponse) getNewHttpClient().execute(
							httpGet);
				} else {
					// 第1步：创建HttpPost对象
					httpPost = new HttpPost(params[0]);

					// 设置HTTP POST请求参数必须用NameValuePair对象
					httpPost.setEntity(new UrlEncodedFormEntity(
							Server.this.postParams, HTTP.UTF_8));

					// 第2步：使用execute方法HTTP发送POST请求，并返回HttpResponse对象
					httpResponse = (HttpResponse) getNewHttpClient().execute(
							httpPost);
				}

				return this.handleCallback(httpResponse);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			
		}
	}
}