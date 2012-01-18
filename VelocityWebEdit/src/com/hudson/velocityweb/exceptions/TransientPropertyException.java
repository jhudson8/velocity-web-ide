package com.hudson.velocityweb.exceptions;

public class TransientPropertyException extends HibernateSynchronizerException {

    private static final long serialVersionUID = 1L;

    public TransientPropertyException() {
        super();
    }

    public TransientPropertyException(String arg0) {
        super(arg0);
    }

    public TransientPropertyException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public TransientPropertyException(Throwable arg0) {
        super(arg0);
    }
}