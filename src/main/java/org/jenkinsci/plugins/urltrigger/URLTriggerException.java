package org.jenkinsci.plugins.urltrigger;

/**
 * @author Gregory Boissinot
 */
public class URLTriggerException extends Exception {

    public URLTriggerException() {
    }

    public URLTriggerException(String s) {
        super(s);
    }

    public URLTriggerException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public URLTriggerException(Throwable throwable) {
        super(throwable);
    }
}
