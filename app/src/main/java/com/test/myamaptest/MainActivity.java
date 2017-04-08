package com.test.myamaptest;

import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements LocationSource, AMapLocationListener, AMap.CancelableCallback, AMap.OnMapClickListener, GeocodeSearch.OnGeocodeSearchListener {

    MapView mMapView = null;
    AMap aMap = null;
    //标识，用于判断是否只显示一次定位信息和用户重新定位
    private boolean isFirstLoc = true;
    private Marker marker;
    private LatLng latLng;
    private String mPlaceName;
    private boolean isClickAMap = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

        // 再此之前地图已经会显示到北京了，这是获取 控制对象
        getAMapAndSetting();

         // 控件交互
        testUIAct();

         // 点标记
        markPoint();


    }

    /**
     *  点标记功能包含两大部分，一部分是点（俗称 Marker）、另一部分是浮于点上方的信息窗体（俗称 InfoWindow）。
     * */
    private void markPoint() {

//        LatLng latLng = new LatLng(39.906901,116.397972);
//        final Marker marker = aMap.addMarker(new MarkerOptions().position(latLng).title("markerTitle").snippet("DefaultMarker"));

        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(new LatLng(34.341568,108.940174));
        markerOption.title("西安市！！").snippet("西安市：34.341568, 108.940174");

        markerOption.draggable(true);//设置Marker可拖动
        markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.location_marker)));
        // 将Marker设置为贴地显示，可以双指下拉地图查看效果
        markerOption.setFlat(true);//设置marker平贴地图效果

        // 官网居然漏了这句代码....高德居然还有个 中文的 = 在文档代码里面，真尼玛坑
        marker = aMap.addMarker(markerOption);

        marker.showInfoWindow(); // 绘制 InfoWindow，另外 marker 还有点击和拖动的监听
    }

    private void initView() {
        findViewById(R.id.btn_move).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 移动地图
                moveMap();
            }
        });
    }

    /**
     *  改变地图显示的区域（即改变地图中心点）、改变地图的缩放级别、限制地图的显示范围等。
     *  是通过经纬度移动的
     * */
    //参数依次是：视角调整区域的中心点坐标、希望调整到的缩放级别、俯仰角0°~45°（垂直与地图时为0）、偏航角 0~360° (正北方为0)
    CameraUpdate mCameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(39.977290,116.337000),18,30,0));
    private void moveMap() {

         // 1: 带动画效果的移动，只能说动画时间长会看到奇异的空白地图
//        aMap.animateCamera(mCameraUpdate,2000,this);

         // 1: 单纯的移动
        aMap.moveCamera(mCameraUpdate);

        //设置希望展示的地图缩放级别,不设置不好，好像设置一次以后都是这个了,要在移动地图后调用 ？
//        mCameraUpdate = CameraUpdateFactory.zoomTo(17);
//        mCameraUpdate = CameraUpdateFactory.zoomTo(1);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(17)); // 正确的使用姿势
    }

    /**
     *  控件是指浮在地图图面上的一系列用于操作地图的组件，例如缩放按钮、指南针、定位按钮、比例尺等
     * */
    private UiSettings mUiSettings;//定义一个UiSettings对象
    private void testUIAct() {
        mUiSettings = aMap.getUiSettings();//实例化UiSettings类对象

        mUiSettings.setZoomControlsEnabled(true); // 此控件默认打开

//        mUiSettings.setZoomPosition(1000); // 设置缩放按钮的位置,这个不知道什么意思

        mUiSettings.setCompassEnabled(true); // 指南针用于向 App 端用户展示地图方向，默认不显示

        aMap.setLocationSource(this);//通过aMap对象设置定位数据源的监听

        mUiSettings.setMyLocationButtonEnabled(true); //显示默认的定位按钮

        aMap.setMyLocationEnabled(true);// 是否触发定位并显示当前位置,就是不断定位,但是这里只能为 true 才会显示定位按钮，解决方法就是设置单次定位

        mUiSettings.setScaleControlsEnabled(true);//控制比例尺控件是否显示

        mUiSettings.setLogoPosition(AMapOptions.LOGO_MARGIN_RIGHT); // 高德地图的 logo 默认在左下角显示，不可以移除，但支持调整到固定位置
    }

    /**
     * 初始化地图控制器对象
     * 后续可以进行操作
     */
    private void getAMapAndSetting() {

        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        /****************************************************************************************************************************************/
        // 设置定位监听
//        aMap.setLocationSource(this);
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false，开启会导致不断的定位
        aMap.setMyLocationEnabled(true);
        // 设置定位的类型为定位模式，有定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);

         // 自己想的，加载这里，不想去看官网的 demo，结果没有用
//        showBluePoint();

         //  //true：显示室内地图；false：不显示；
        aMap.showIndoorMap(false);

        // 设置卫星地图模式，aMap是地图控制器对象。
//        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);

        //夜景地图，aMap是地图控制器对象。
//        aMap.setMapType(AMap.MAP_TYPE_NIGHT);

        //显示实时路况图层，aMap是地图控制器对象。
//        aMap.setTrafficEnabled(true);
        
         // 点击地图的监听
        aMap.setOnMapClickListener(this);

    }

    /**
     * 现实定位蓝点
     */
    private void showBluePoint() {
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        if(null != mlocationClient){
            mlocationClient.onDestroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    OnLocationChangedListener mListener;
    AMapLocationClient mlocationClient;
    AMapLocationClientOption mLocationOption;
    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            //初始化定位
            mlocationClient = new AMapLocationClient(this);
            //初始化定位参数
            mLocationOption = new AMapLocationClientOption();
            //设置定位回调监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);

            //设置是否只定位一次,默认为false
            mLocationOption.setOnceLocation(true); // 解决一直在定位的问题?? 结果发现事实是不能解决这个问题！！！

            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();//启动定位
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
//                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
                //定位成功回调信息，设置相关消息
                aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见官方定位类型表
                aMapLocation.getLatitude();//获取纬度
                aMapLocation.getLongitude();//获取经度
                aMapLocation.getAccuracy();//获取精度信息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(aMapLocation.getTime());
                df.format(date);//定位时间
                aMapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                aMapLocation.getCountry();//国家信息
                aMapLocation.getProvince();//省信息
                aMapLocation.getCity();//城市信息
                aMapLocation.getDistrict();//城区信息
                aMapLocation.getStreet();//街道信息
                aMapLocation.getStreetNum();//街道门牌号信息
                aMapLocation.getCityCode();//城市编码
                aMapLocation.getAdCode();//地区编码

                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    //设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                    //将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude())));
                    //点击定位按钮 能够将地图的中心移动到定位点
                    mListener.onLocationChanged(aMapLocation);
                    /*//添加标记
                    MarkerOptions markerOption = new MarkerOptions();
                    markerOption.position(new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude()));
                    markerOption.title("当前位置！").snippet("xx市：xx.341568, xxx.940174");
                    markerOption.draggable(true);//设置Marker可拖动
                    markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.location_marker)));
                    // 将Marker设置为贴地显示，可以双指下拉地图查看效果
                    markerOption.setFlat(true);//设置marker平贴地图效果
                    aMap.addMarker(markerOption);*/
                    //获取定位信息
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(aMapLocation.getCountry() + "" + aMapLocation.getProvince() + "" + aMapLocation.getCity() + "" + aMapLocation.getProvince() + "" + aMapLocation.getDistrict() + "" + aMapLocation.getStreet() + "" + aMapLocation.getStreetNum());
                    Toast.makeText(getApplicationContext(), buffer.toString(), Toast.LENGTH_LONG).show();
                    isFirstLoc = false;
                }
                if (!isClickAMap) {
                    latLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()); // 点击地图保存经纬度
                    Bundle locBundle = aMapLocation.getExtras();
                    if (locBundle != null) {
                        String desc = locBundle.getString("desc"); // 获取地名
                        if (!TextUtils.isEmpty(desc)) {
                            mPlaceName = desc;
                        }
                    }

                }
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode()+ ": " + aMapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
            }
        }
    }

    /**
     *  带动画效果的移动，完成
     * */
    @Override
    public void onFinish() {

    }

    /**
     *  带动画效果的移动，取消
     * */
    @Override
    public void onCancel() {

    }

    /**
     *  点击地图的唯一回调
     *  通过经纬度检索实际位置
     * */
    @Override
    public void onMapClick(LatLng latLng) {
        if (marker != null) {
            marker.remove();
        }
        isClickAMap = true;
        this.latLng = latLng;
        GeocodeSearch geocoderSearch = new GeocodeSearch(this); // 地理检索
        LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
        
         // 反地理编码，第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火星坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP); // 估计 GeocodeSearch.AMAP 是火星 ，GeocodeSearch.GPS 是GPS原生坐标系,一般都是火星，不能想象都觉得不能用
        
        geocoderSearch.getFromLocationAsyn(query);
        geocoderSearch.setOnGeocodeSearchListener(this);
    }

    /**
     *  （反）地理编码回调1
     * */
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        if (i == 1000) { // 1000为成功，其他为失败（详细信息参见网站开发指南-实用工具-错误码对照表）
            if (regeocodeResult != null && regeocodeResult.getRegeocodeAddress() != null
                    && regeocodeResult.getRegeocodeAddress().getFormatAddress() != null) {
                try {
                    String addressName = regeocodeResult.getRegeocodeAddress().getFormatAddress();
                    if (!TextUtils.isEmpty(addressName)) {
                        mPlaceName = addressName;
                        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
                        marker = myDrawMarkerOnMap(latLng, 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     *  （反）地理编码回调2
     *  这里没有用到，但是是必须实现的方法，感觉高德这里可以优化，地理编码功能没有必要正反两个方法都必须实现
     * */
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    /**
     * 绘制标志
     */
    public Marker myDrawMarkerOnMap(LatLng latLng, int index) {
        return drawMarkerOnMap(latLng, index, R.drawable.iv_marker);
    }

    public Marker drawMarkerOnMap(LatLng latLng, int index, int pointDrawableId) {
        if (aMap != null && latLng != null) {
            MarkerOptions markerOption = new MarkerOptions();
            markerOption.position(latLng);
            markerOption.draggable(true);
            markerOption.perspective(true);
            Marker marker = aMap.addMarker(markerOption);
            marker.setIcon(imageNormal(pointDrawableId, index)); // 设置自定义的图标
            return marker;
        }
        return null;
    }

    public BitmapDescriptor imageNormal(int pointDrawableId, int index) {
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.marker_view, null);
        TextView markerView = (TextView) view.findViewById(R.id.tvMark);
        ImageView ivMarker = (ImageView) view.findViewById(R.id.ivMark);
        ivMarker.setImageResource(pointDrawableId);
        if (index > 0) {
            markerView.setText(index + "");
        }
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromView(view);
        return bitmapDescriptor;
    }
}
