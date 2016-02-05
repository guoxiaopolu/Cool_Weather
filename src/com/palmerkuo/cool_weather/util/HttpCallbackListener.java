package com.palmerkuo.cool_weather.util;

public interface HttpCallbackListener {
	void onFinish(String response);
	void onError(Exception e);	
}
