package com.nmotion.android.network;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

import com.nmotion.android.App;
import com.nmotion.android.core.PreferencesManager;
import com.nmotion.android.fakeHttps.FakeSocketFactory;
import com.nmotion.android.utils.Config;
import com.nmotion.android.utils.Logger;
import com.nmotion.android.utils.Utils;

public class HTTPHelper {
        String lang;
	private HttpClient _httpClient;
	Context context;
	
	public HTTPHelper(Context context) {
	        this.context=context;
		_httpClient = createHttpClient();		
	}

	public HttpClient getClient() {
		return _httpClient;
	}

	public void shutdownHttpClient() {
		if (_httpClient != null && _httpClient.getConnectionManager() != null) {
			_httpClient.getConnectionManager().shutdown();
		}
	}
	
	private String getDeviceInfo(){
	    try {
                return  "Android | Nmotion version "+context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName+" | "+Build.MANUFACTURER+" "+Build.MODEL;
            } catch (NameNotFoundException e) {            
                e.printStackTrace();
                return "Android "+Build.MANUFACTURER+" "+Build.MODEL;
            }
	}

	private HttpClient createHttpClient() {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
		HttpProtocolParams.setUseExpectContinue(params, false);
		HttpConnectionParams.setConnectionTimeout(params, Config.CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, Config.CONNECTION_TIMEOUT);
		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schReg.register(new Scheme("https", new FakeSocketFactory(), 443));
		ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(params, schReg);
		return new DefaultHttpClient(connectionManager, params);
	}

	public JSONObject sendRequestGET(String uri, String header, boolean isFacebook) throws NetworkException {
		try {
			Logger.verbose(String.format("Service request: %s", uri));
			HttpGet httpGet = new HttpGet(uri.replace(" ", "%20"));
			if (App.getInstance().getNetworkService().isLoggedIn() || header != null) {
				if (isFacebook) {
					httpGet.addHeader("Auth", "FacebookToken " + header);
				} else {
					httpGet.addHeader("Auth", "NmotionToken " + header);
				}
			} else {
				httpGet.addHeader("Auth", "DeviceToken " + App.getInstance().getDeviceId());
			}
			httpGet.addHeader("Content-Type", "application/json");
			httpGet.addHeader("Accept-Language", generateLangHeader());
			//httpGet.addHeader("User-Agent", getDeviceInfo());
			HttpResponse response;
			Logger.verbose("SEND REQUEST===============");
			for (int i = 0; i < httpGet.getAllHeaders().length; i++) {
                            System.out.println(httpGet.getAllHeaders()[i].toString());
                        }
			
			response = _httpClient.execute(httpGet);
			Logger.verbose(String.format("Service response code: %s", response.getStatusLine().getStatusCode()));
			System.out.println("reason = "+response.getStatusLine().getReasonPhrase());
			if (response.getStatusLine().getStatusCode()==NetworkException.HTTP_CODE_UPDATE){
			    throw new NetworkException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
			}
			HttpEntity entity = response.getEntity();
			if (entity != null) {
			    System.out.println(entity.toString());
				InputStream stream = entity.getContent();
				String result = Utils.convertStreamToString(stream);
				stream.close();
				Logger.verbose(result);
				JSONObject jsonObject = new JSONObject(result);
				NetworkException.isNetworkError(jsonObject);
				// if (jsonObject.optInt("logined", -1) == 0 && isLoggedIn()) {
				// logOut();
				// }
				return jsonObject;
			}
		} catch (NetworkException e) {
			Logger.warning("Network error");
			throw e;
		} catch (ClientProtocolException e) {
			Logger.warning("HTTP error");
			return null;
		} catch (IOException e) {
			Logger.warning("IO error");
			e.printStackTrace();
		} catch (JSONException e) {
		    e.printStackTrace();
			Logger.warning("JSON fault ");
		} catch (Exception e) {
			Logger.warning("Download fault ");
			e.printStackTrace();
		}
		throw new NetworkException();
	}

	public JSONObject sendRequestPOST(String uri, OperationParams params, JSONObject object, String header, boolean isFacebookAuth) throws NetworkException {
		return sendRequestPOST(uri, params, object, header, isFacebookAuth, null);
	}

	public JSONObject sendRequestPOST(String uri, String header, boolean isFacebookAuth) throws NetworkException {
		return sendRequestPOST(uri, null, null, header, isFacebookAuth, null);
	}

	public JSONObject sendRequestPATCH(String uri, OperationParams params, JSONObject object, String header, boolean isFacebookAuth) throws NetworkException {
		ArrayList<NameValuePair> headers = new ArrayList<NameValuePair>(1);
		headers.add(new BasicNameValuePair("X-HTTP-Method-Override", "PATCH"));
		return sendRequestPOST(uri, params, object, header, isFacebookAuth, headers);
	}

	public JSONObject sendRequestLINK(String uri, String header, boolean isFacebookAuth, ArrayList<String> links) throws NetworkException {
		ArrayList<NameValuePair> headers = new ArrayList<NameValuePair>(1);
		headers.add(new BasicNameValuePair("X-HTTP-Method-Override", "LINK"));
		for (String link : links) {
			headers.add(new BasicNameValuePair("Link", link));
		}
		return sendRequestPOST(uri, null, null, header, isFacebookAuth, headers);
	}

	public JSONObject sendRequestUNLINK(String uri, String header, boolean isFacebookAuth, ArrayList<String> links) throws NetworkException {
		ArrayList<NameValuePair> headers = new ArrayList<NameValuePair>(1);
		headers.add(new BasicNameValuePair("X-HTTP-Method-Override", "UNLINK"));
		for (String link : links) {
			headers.add(new BasicNameValuePair("Link", link));
		}
		return sendRequestPOST(uri, null, null, header, isFacebookAuth, headers);
	}
	
	private String generateLangHeader(){
	    lang = App.getInstance().getPreferencesManager().contains(PreferencesManager.LANGUAGE) ? App.getInstance().getPreferencesManager().getString(PreferencesManager.LANGUAGE, "da") : Locale.getDefault().getLanguage();
	    return lang+";q=1;"+(lang.equals("en") ? "da;q=0.5" : "en;q=0.5");
	    
	}

	public JSONObject sendRequestPOST(String uri, OperationParams params, JSONObject object, String header, boolean isFacebookAuth, ArrayList<NameValuePair> additionalHeaders)
			throws NetworkException {
		ArrayList<NameValuePair> values = params == null ? new ArrayList<NameValuePair>() : params.generateOperationValues();
		Logger.verbose(String.format("Service request: %s", uri + (params == null ? "" : params.toString())));
		HttpPost httpPost = new HttpPost(uri);
		if (App.getInstance().getNetworkService().isLoggedIn() && header != null) {
			if (isFacebookAuth) {
				httpPost.addHeader("Auth", "FacebookToken " + header);
			} else {
				httpPost.addHeader("Auth", "NmotionToken " + header);
			}
		} else {
			httpPost.addHeader("Auth", "DeviceToken " + App.getInstance().getDeviceId());
		}

		if (additionalHeaders != null) {
			for (NameValuePair nameValuePair : additionalHeaders) {
				httpPost.addHeader(nameValuePair.getName(), nameValuePair.getValue());
			}
		}		
                httpPost.addHeader("Accept-Language", generateLangHeader());
		httpPost.addHeader("Content-Type", "application/json");
		httpPost.addHeader("User-Agent", getDeviceInfo());
		UrlEncodedFormEntity formEntity;
		int httpCode = -1;
		try {
			if (object != null) {
				httpPost.setEntity(new StringEntity(object.toString()));
			} else {
				formEntity = new UrlEncodedFormEntity(values, HTTP.UTF_8);
				httpPost.setEntity(formEntity);
			}
			HttpResponse response;
			System.out.println("SEND REQUEST===============");
			for (int i = 0; i < httpPost.getAllHeaders().length; i++) {
                            System.out.println(httpPost.getAllHeaders()[i].toString());
                        }
			if (object!=null)
                            Logger.verbose(object.toString());

			response = _httpClient.execute(httpPost);
			Logger.verbose(String.format("Service response code: %s", response.getStatusLine()));
	                if (response.getStatusLine().getStatusCode()==NetworkException.HTTP_CODE_UPDATE){
	                    throw new NetworkException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
	                }/*else if(response.getStatusLine().getStatusCode()==NetworkException.HTTP_CODE_PRECONDITION_FAILED){
	                    throw new NetworkException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
	                }*/
			HttpEntity entity = response.getEntity();
			httpCode = response.getStatusLine().getStatusCode();
			if (entity != null) {
				InputStream stream = entity.getContent();
				String result = Utils.convertStreamToString(stream);
				stream.close();
				Logger.verbose(result);
				JSONObject jsonObject = new JSONObject(result);
				NetworkException.checkResponse(httpCode, jsonObject);
	                        if (httpCode==NetworkException.HTTP_CODE_PRECONDITION_FAILED)
	                            throw new NetworkException(httpCode, jsonObject);
				// /////////////in case if session dies
				// if (jsonObject.optInt("logined", -1) == 0 && isLoggedIn()) {
				// logOut();
				// }
				// ///////////////////////////////////
				return jsonObject;
			}
		} catch (NetworkException e) {
			Logger.warning("Network error");
			throw e;
		} catch (ClientProtocolException e) {
			Logger.warning("HTTP error");
			return null;
		} catch (IOException e) {
			Logger.warning("IO error");
		} catch (JSONException e) {
			Logger.warning("JSON fault ");
		} catch (Exception e) {
			Logger.warning("Download fault ");
			e.printStackTrace();
		}
		throw new NetworkException(httpCode, new JSONObject());
	}

	public JSONObject sendRequestPUT(String uri, OperationParams params, JSONObject object, String header, boolean isFacebook) throws NetworkException {
		ArrayList<NameValuePair> values = params == null ? new ArrayList<NameValuePair>() : params.generateOperationValues();
		Logger.verbose(String.format("Service request: %s", uri + (params == null ? "" : params.toString())));
		HttpPut httpPut = new HttpPut(uri);
		if (App.getInstance().getNetworkService().isLoggedIn() && header != null) {
			if (isFacebook) {
				httpPut.addHeader("Auth", "FacebookToken " + header);
			} else {
				httpPut.addHeader("Auth", "NmotionToken " + header);
			}
		} else {
			httpPut.addHeader("Auth", "DeviceToken " + App.getInstance().getDeviceId());
		}
		httpPut.addHeader("Accept-Language", generateLangHeader());
		httpPut.addHeader("Content-Type", "application/json");
		httpPut.addHeader("User-Agent", getDeviceInfo());		
		UrlEncodedFormEntity formEntity;
		int httpCode = -1;
		try {
			if (object != null) {
				httpPut.setEntity(new StringEntity(object.toString()));
			} else {
				formEntity = new UrlEncodedFormEntity(values, HTTP.UTF_8);
				httpPut.setEntity(formEntity);
			}
			HttpResponse response;
			Logger.verbose("SEND REQUEST===============");
			for (int i = 0; i < httpPut.getAllHeaders().length; i++) {
                            System.out.println(httpPut.getAllHeaders()[i].toString());
                        }
			//Logger.verbose(httpPut.getAllHeaders().toString());
			if (object!=null)
			    Logger.verbose(object.toString());
			response = _httpClient.execute(httpPut);			

			Logger.verbose(String.format("Service response code: %s", response.getStatusLine()));
	                if (response.getStatusLine().getStatusCode()==NetworkException.HTTP_CODE_UPDATE/* || response.getStatusLine().getStatusCode()==NetworkException.HTTP_CODE_PRECONDITION_FAILED*/){
	                    throw new NetworkException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
	                }
			HttpEntity entity = response.getEntity();
			httpCode = response.getStatusLine().getStatusCode();
			if (entity != null) {
				InputStream stream = entity.getContent();
				String result = Utils.convertStreamToString(stream);
				stream.close();
				Logger.verbose(result);
				JSONObject jsonObject = new JSONObject(result);
				NetworkException.isNetworkError(jsonObject);
				if (httpCode==NetworkException.HTTP_CODE_PRECONDITION_FAILED)
				    throw new NetworkException(httpCode, jsonObject);
				// /////////////in case if session dies
				// if (jsonObject.optInt("logined", -1) == 0 && isLoggedIn()) {
				// logOut();
				// }
				// ///////////////////////////////////
				return jsonObject;
			}
		} catch (NetworkException e) {
			Logger.warning("Network error");
			throw e;
		} catch (ClientProtocolException e) {
			Logger.warning("HTTP error");
			return null;
		} catch (IOException e) {
			Logger.warning("IO error");
		} catch (JSONException e) {
			Logger.warning("JSON fault ");
		} catch (Exception e) {
			Logger.warning("Download fault ");
			e.printStackTrace();
		}
		throw new NetworkException(httpCode, new JSONObject());
	}

}
