package com.oversea.task.exception;

/**
 * 数据解析异常
 * Created by lemon on 2014/6/12.
 */
public class DataParseException extends RuntimeException {
  
	private static final long serialVersionUID = -2749660470061658714L;

	/**
     * Constructs an {@code DataParseException} with {@code null} as its error detail
     * message.
     */
    public DataParseException() {
        super();
    }

    public DataParseException(String message) {
        super(message);
    }

    public DataParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataParseException(Throwable cause) {
        super(cause);
    }
}
