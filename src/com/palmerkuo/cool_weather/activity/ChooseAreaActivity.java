package com.palmerkuo.cool_weather.activity;

import java.util.ArrayList;
import java.util.List;

import com.palmerkuo.cool_weather.R;
import com.palmerkuo.cool_weather.model.City;
import com.palmerkuo.cool_weather.model.CoolWeatherDB;
import com.palmerkuo.cool_weather.model.County;
import com.palmerkuo.cool_weather.model.Province;
import com.palmerkuo.cool_weather.util.HttpCallbackListener;
import com.palmerkuo.cool_weather.util.HttpUtil;
import com.palmerkuo.cool_weather.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;

	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;

	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList = new ArrayList<String>();

	// ʡ�б�
	private List<Province> provinceList;
	// ���б�
	private List<City> cityList;
	// ���б�
	private List<County> countyList;

	// ѡ�е�ʡ
	private Province selectedProvince;
	// ѡ�е���
	private City selectedCity;
	// ѡ�е���
	private County selectedCounty;

	// ��ǰѡ�еļ���
	private int correntLevel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		titleText = (TextView) findViewById(R.id.title_text);
		listView = (ListView) findViewById(R.id.list_view);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		coolWeatherDB = CoolWeatherDB.getInstance(this);

		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (correntLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(position);
					queryCities();
				} else if (correntLevel == LEVEL_CITY) {
					selectedCity = cityList.get(position);
					queryCounties();
				}

			}
		});
		// ����ʡ������
		queryProvinces();
	}

	/*
	 * ��ѯ���е�ʡ�����ȴ����ݿ��ѯ�����ݿ�û���򵽷�������ѯ
	 */
	private void queryProvinces() {
		provinceList = coolWeatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("�й�");
			correntLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null, "province");
		}
	}

	/*
	 * ��ѯѡ�е�ʡ�����е��У����ȴ����ݿ��ѯ�����ݿ�û���򵽷�������ѯ
	 */
	private void queryCities() {
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			correntLevel = LEVEL_CITY;
		} else {
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}

	/*
	 * ��ѯѡ�е��е������أ����ȴ����ݿ��ѯ�����ݿ�û���򵽷�������ѯ
	 */
	private void queryCounties(){
		countyList=coolWeatherDB.loadCounties(selectedCity.getId());
		if(countyList.size()>0){
			dataList.clear();
			for(County county:countyList){
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			correntLevel=LEVEL_COUNTY;
		}else{
			queryFromServer(selectedCity.getCityCode(),"county");
		}
	}
	
	/*
	 * ͨ����������ݺ����ͣ��ڷ������ϲ�ѯʡ��������
	 */
	private void queryFromServer(final String code,final String type){
		String address;
		if(!TextUtils.isEmpty(code)){
			address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
		}else{
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				boolean result=false;
				if("province".equals(type)){
					result=Utility.handleProvinceResponse(coolWeatherDB, response);
				}else if("city".equals(type)){
					result=Utility.handleCityResponse(coolWeatherDB, response, selectedProvince.getId());
				}else if("county".equals(type)){
					result=Utility.handleCountyResponse(coolWeatherDB, response, selectedCounty.getId());
				}
				
				if(result){
					//ͨ��runOnUiThread()�����ص����̴߳����߼�
					runOnUiThread(new Runnable() {
						public void run() {
							closeProgressDialog();
							if("province".equals(type)){
								queryProvinces();
							}else if("city".equals(type)){
								queryCities();
							}else if("County".equals(type)){
								queryCounties();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				//ͨ��runOnUiThread()�����ص����̴߳����߼�
				runOnUiThread(new Runnable() {
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	/*
	 * ��ʾ���ȶԻ���
	 */
	private void showProgressDialog(){
		if(progressDialog==null){
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/*
	 * �رնԻ���
	 */
	private void closeProgressDialog(){
		if(progressDialog!=null){
			progressDialog.dismiss();
		}
	}

	
	/*
	 * ����back���������ݵ�ǰ�ļ������жϵ�ǰӦ����ʡ�б������б��������˳�
	 */
	@Override
	public void onBackPressed() {
		if(correntLevel==LEVEL_COUNTY){
			queryCities();
		}else if(correntLevel==LEVEL_CITY){
			queryProvinces();
		}else{
			finish();
		}
	}
	
}