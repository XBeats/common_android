package com.common.library.location;

public class LocationInfo implements Cloneable {
	private double latitude;
	private double longitude;
	private String city;
	private String district;
	private String province;
	private String street;
	private String addrStr;
	private long lastModifyDt;// 记录定位时间
	private boolean cachedInfo = false;

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String cityName) {
		this.city = cityName;
	}
	
	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getAddrStr() {
		return addrStr;
	}

	public void setAddrStr(String address) {
		this.addrStr = address;
	}

	public long getLastModifyDt() {
		return lastModifyDt;
	}

	public void setLastModifyDt(long lastModifyDt) {
		this.lastModifyDt = lastModifyDt;
	}
	
	public void setCachedInfo(boolean cachedInfo) {
		this.cachedInfo = cachedInfo;
	}
	
	public boolean isCachedInfo(){
		return cachedInfo;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public String toString() {
		return "latitude=" + latitude + ", longitude=" + longitude 
				+ ", city=" + city + ", district=" + district 
				+ ", province=" + province + ", street=" + street 
				+ ", addrStr=" + addrStr + ", lastModifyDt=" + lastModifyDt;
	}
}