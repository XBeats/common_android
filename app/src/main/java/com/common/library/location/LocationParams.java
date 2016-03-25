package com.common.library.location;

import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

/**
 * It exposes almost all setting API about location.
 * 
 * @author zf08526
 *
 */
public class LocationParams {
    // const parameters
    private static final long DEFAULT_LOCATION_TIMEOUT = 10 * 1000;
    private final int DEFAULT_SCAN_SPAN = 1000 * 5;// 定位间隔
    private final int DEFAULT_CACHE_EXPIRED_TIME = 1000 * 60 * 2;

    /**
     * Options from baidu.
     */
    private LocationClientOption clientOption;

    /**
     * Locate timeout.
     */
    private long timeout = DEFAULT_LOCATION_TIMEOUT;

    /**
     * The expired time of cache.
     */
    private int cacheExpiredTime = DEFAULT_CACHE_EXPIRED_TIME;

    /**
     * If set true stop location service after locate successfully
     */
    private boolean stopLocationAutomaticly = true;

    /**
     * If set true even the location cache exists, new location job will be
     * started, otherwise location cache will returned instead.
     */
    private boolean networkSensitive = true;

    private LocationParams() {
        this.clientOption = new LocationClientOption();
        initOptions();
    }

    public static LocationParams getDefault() {
        return new LocationParams();
    }

    private void initOptions() {
        clientOption.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
        clientOption.setOpenGps(true); // 打开gps
        clientOption.setCoorType("bd09ll"); // 设置坐标类型
        clientOption.setServiceName("tongcheng");
        clientOption.setAddrType("all");// 返回的定位结果包含地址信息
        clientOption.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        clientOption.setScanSpan(DEFAULT_SCAN_SPAN);// 设置发起定位请求的间隔时间为5000ms
        clientOption.setPriority(LocationClientOption.GpsFirst); // 不设置，默认是gps优先
        clientOption.disableCache(true); // 禁止启用缓存定位
    }

    public long getTimeout() {
        return timeout;
    }

    /**
     * Time unit is second.
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public LocationClientOption getClientOption() {
        return clientOption;
    }

    public long getCacheExpiration() {
        return cacheExpiredTime;
    }

    public void setCacheExpiration(int expiration) {
        this.cacheExpiredTime = expiration;
    }

    public boolean isStopLocationAutomaticly() {
        return stopLocationAutomaticly;
    }

    public void setStopLocationAutomaticly(boolean stopLocationAutomaticly) {
        this.stopLocationAutomaticly = stopLocationAutomaticly;
    }

    public boolean isNetworkSensitive() {
        return networkSensitive;
    }

    public void setNetworkSensitive(boolean networkSensitive) {
        this.networkSensitive = networkSensitive;
    }

}
