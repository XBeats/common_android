package com.common.library.contacts;

public class ContactInfo {
	private String displayName;
	private String phoneNumber;
	private boolean hasPhoneNumber;

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public boolean hasPhoneNumber() {
		return hasPhoneNumber;
	}

	public void setHasPhoneNumber(boolean hasPhoneNumber) {
		this.hasPhoneNumber = hasPhoneNumber;
	}
	
	@Override
	public String toString() {
		return "displayName: " + displayName + ", phoneNumber: " + phoneNumber + ", hasPhoneNumber: " + hasPhoneNumber;
	}
}