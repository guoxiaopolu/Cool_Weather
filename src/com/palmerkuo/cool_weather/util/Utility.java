package com.palmerkuo.cool_weather.util;

import com.palmerkuo.cool_weather.model.City;
import com.palmerkuo.cool_weather.model.CoolWeatherDB;
import com.palmerkuo.cool_weather.model.County;
import com.palmerkuo.cool_weather.model.Province;

import android.text.TextUtils;

public class Utility {
	/*
	 * �����ʹ������������ص�ʡ������
	 */
	public synchronized static boolean handleProvinceResponse(CoolWeatherDB coolWeatherDB, String response) {
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					// ���������������ݴ�ŵ�Province��
					coolWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}

	/*
	 * �����ʹ������������ص��м�����
	 */
	public synchronized static boolean handleCityResponse(CoolWeatherDB coolWeatherDB, String response,
			int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0) {
				for (String p : allCities) {
					String[] array = p.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					// ���������������ݴ�ŵ�City��
					coolWeatherDB.saveCity(city);
				}
			}
			return true;
		}
		return false;
	}

	/*
	 * �����ʹ������������ص��ؼ�����
	 */
	public synchronized static boolean handleCountyResponse(CoolWeatherDB coolWeatherDB, String response, int cityId) {
		if(!TextUtils.isEmpty(response)){
			String[] allCounties=response.split(",");
			for(String p:allCounties){
				String[] array=p.split("\\|");
				County county=new County();
				county.setCountyCode(array[0]);
				county.setCountyName(array[1]);
				county.setCityId(cityId);
				//������д���County
				coolWeatherDB.savedCounty(county);
			}
			return true;
		}
		return false;
	}

	
}