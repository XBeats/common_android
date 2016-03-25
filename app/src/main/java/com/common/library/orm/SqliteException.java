package com.common.library.orm;

public class SqliteException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	 /**
     * Constructs a new {@code SqliteException} that includes the current stack
     * trace.
     */
    public SqliteException() {
    }

    /**
     * Constructs a new {@code SqliteException} with the current stack trace
     * and the specified detail message.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    public SqliteException(String detailMessage) {
        super(detailMessage);
    }

   /**
     * Constructs a new {@code SqliteException} with the current stack trace,
     * the specified detail message and the specified cause.
     *
     * @param detailMessage
     *            the detail message for this exception.
     * @param throwable
     *            the cause of this exception.
     */
    public SqliteException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    /**
     * Constructs a new {@code SqliteException} with the current stack trace
     * and the specified cause.
     *
     * @param throwable
     *            the cause of this exception.
     */
    public SqliteException(Throwable throwable) {
        super(throwable);
    }

}
