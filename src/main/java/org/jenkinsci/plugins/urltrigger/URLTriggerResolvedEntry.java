package org.jenkinsci.plugins.urltrigger;

/**
 * @author Gregory Boissinot
 */
public class URLTriggerResolvedEntry {

    private String resolvedURL;

    private URLTriggerEntry entry;

    public URLTriggerResolvedEntry(String resolvedURL, URLTriggerEntry entry) {
        if (resolvedURL == null) {
            throw new NullPointerException("A resolved URL is required.");
        }
        this.resolvedURL = resolvedURL;

        if (entry == null) {
            throw new NullPointerException("An entry object is required.");
        }
        this.entry = entry;
    }

    public String getResolvedURL() {
        return resolvedURL;
    }

    public URLTriggerEntry getEntry() {
        return entry;
    }

    public boolean isURLTriggerValidURL() {
        return isHttp()
                || isHttps()
                || isFtp();
    }

    public boolean isHttp() {
        return resolvedURL.startsWith("http");
    }

    public boolean isHttps() {
        return resolvedURL.startsWith("https");
    }

    public boolean isFtp() {
        return resolvedURL.startsWith("ftp");
    }
}
