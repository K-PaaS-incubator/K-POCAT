package io.pocat.platform.gateway.utils.clazz;

public class InstantiationFailedException extends Exception {
    public InstantiationFailedException(String msg, Throwable e) {
        super(msg, e);
    }
}
