package com.hendwick.aargvark;

/**
 * Created by Martin Wickham on 1/27/2015.
 */
public class AargvarkException extends Exception {

    public AargvarkException() {
        super();
    }
    public AargvarkException(String msg) {
        super(msg);
    }
    public AargvarkException(String msg, Throwable cause) { super(msg, cause); }
    public AargvarkException(Throwable cause) {
        super(cause);
    }
}
