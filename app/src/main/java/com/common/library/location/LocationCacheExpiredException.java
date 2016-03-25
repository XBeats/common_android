package com.common.library.location;

/**
 * A exception notify user that cache is expired or not exist.
 * 
 * @author zf08526
 * 
 */
public class LocationCacheExpiredException extends Exception {
	private static final long serialVersionUID = 3507149800690316733L;

	/**
	 * Constructs a new {@code LocationCacheExpiredException} that includes the
	 * current stack trace.
	 */
	public LocationCacheExpiredException() {
	}

	/**
	 * Constructs a new {@code LocationCacheExpiredException} with the current
	 * stack trace and the specified detail message.
	 * 
	 * @param detailMessage
	 *            the detail message for this exception.
	 */
	public LocationCacheExpiredException(String detailMessage) {
		super(detailMessage);
	}

	/**
	 * Constructs a new {@code LocationCacheExpiredException} with the current
	 * stack trace, the specified detail message and the specified cause.
	 * 
	 * @param detailMessage
	 *            the detail message for this exception.
	 * @param throwable
	 *            the cause of this exception.
	 */
	public LocationCacheExpiredException(String detailMessage,
			Throwable throwable) {
		super(detailMessage, throwable);
	}

	/**
	 * Constructs a new {@code LocationCacheExpiredException} with the current
	 * stack trace and the specified cause.
	 * 
	 * @param throwable
	 *            the cause of this exception.
	 */
	public LocationCacheExpiredException(Throwable throwable) {
		super(throwable);
	}
}
