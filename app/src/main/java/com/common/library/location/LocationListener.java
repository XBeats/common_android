package com.common.library.location;

/**
 * Locate result information will pass to caller via this.
 * 
 * @author zf08526
 * 
 */
public interface LocationListener {
	int UNKNOWN_ERRPR = 0;
	int NETWORK_ERROR = 1;
	int TIMEOUT_ERROR = 2;

	/**
	 * Location result, see {@link LocationInfo}.
	 * 
	 * @param locationInfo
	 */
	void onSuccess(LocationInfo locationInfo);

	/**
	 * Location error may be network is not available or timeout
	 * 
	 * @param errorType
	 *            maybe {@link LocationListener#NETWORK_ERROR} or
	 *            {@link LocationListener#TIMEOUT_ERROR}
	 * 
	 * @param errorDesc
	 *            error description
	 */
	void onError(int errorType, String errorDesc);
}
