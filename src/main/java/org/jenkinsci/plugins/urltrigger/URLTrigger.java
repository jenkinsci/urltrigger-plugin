package org.jenkinsci.plugins.urltrigger;

import antlr.ANTLRException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import hudson.DescriptorExtensionList;
import hudson.EnvVars;
import hudson.Extension;
import hudson.ProxyConfiguration;
import hudson.Util;
import hudson.console.AnnotatedLargeText;
import hudson.model.*;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.util.DescribableList;
import hudson.util.FormValidation;
import hudson.util.Secret;
import hudson.util.SequentialExecutionQueue;
import jenkins.model.*;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.net.ftp.FTPClient;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.xtriggerapi.AbstractTrigger;
import org.jenkinsci.plugins.xtriggerapi.XTriggerCause;
import org.jenkinsci.plugins.xtriggerapi.XTriggerDescriptor;
import org.jenkinsci.plugins.xtriggerapi.XTriggerException;
import org.jenkinsci.plugins.xtriggerapi.XTriggerLog;
import org.jenkinsci.plugins.urltrigger.content.URLTriggerContentType;
import org.jenkinsci.plugins.urltrigger.content.URLTriggerContentTypeDescriptor;
import org.jenkinsci.plugins.urltrigger.service.FTPResponse;
import org.jenkinsci.plugins.urltrigger.service.HTTPResponse;
import org.jenkinsci.plugins.urltrigger.service.URLResponse;
import org.jenkinsci.plugins.urltrigger.service.URLTriggerService;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


/**
 * @author Gregory Boissinot
 */
public class URLTrigger extends AbstractTrigger {

	private static final long serialVersionUID = 4770775641674010339L;

    private List<URLTriggerEntry> entries = new ArrayList<>();

    private boolean labelRestriction;
    
    private URLTriggerCause buildCause = null ;

    @DataBoundConstructor
    public URLTrigger(String cronTabSpec,
                      String triggerLabel) throws ANTLRException {
        super(cronTabSpec, triggerLabel);
    }

    // This constructor is required (probably) to maintain compatibility
    // with the old freestyle job configs.
    public URLTrigger(String cronTabSpec,
            List<URLTriggerEntry> entries,
            boolean labelRestriction,
            String triggerLabel) throws ANTLRException {
    	super(cronTabSpec, triggerLabel);
    	this.entries = entries;
    	this.labelRestriction = labelRestriction;
    }

    public String getCronTabSpec() {
    	return this.spec ;
    }
    
    @SuppressWarnings("unused")
    public List<URLTriggerEntry> getEntries() {
        return entries;
    }
    
    @DataBoundSetter
    public void setEntries(List<URLTriggerEntry> entries) {
		this.entries = entries;
	}

	@SuppressWarnings("unused")
    public boolean isLabelRestriction() {
        return labelRestriction;
    }

	@DataBoundSetter
    public void setLabelRestriction(boolean labelRestriction) {
		this.labelRestriction = labelRestriction;
	}

	@Override
    public Collection<? extends Action> getProjectActions() {

        Map<String, String> subActionTitles = null;
        for (URLTriggerEntry entry : entries) {
            String url = entry.getUrl();
            URLTriggerContentType[] urlTriggerContentTypes = entry.getContentTypes();
            if (entry.getContentTypes() != null) {
                subActionTitles = new HashMap<>(urlTriggerContentTypes.length);
                for (URLTriggerContentType fsTriggerContentFileType : urlTriggerContentTypes) {
                    if (fsTriggerContentFileType != null) {
                        Descriptor<URLTriggerContentType> descriptor = fsTriggerContentFileType.getDescriptor();
                        if (descriptor instanceof URLTriggerContentTypeDescriptor) {
                            subActionTitles.put(url, ((URLTriggerContentTypeDescriptor<?>) descriptor).getLabel());
                        }
                    }
                }
            }
        }

        /*
            [JENKINS-18683] Configuration can't be saved.

            'job' variable can be undefined during a saving action. The solution is to postpone 'job' usage until
            trigger is running. We achieved this by using the URLTriggerAction as nested class (which has access to
            class members of the URLTrigger class).
         */
        URLTriggerAction action = new InternalURLTriggerAction(this.getDescriptor().getDisplayName(), subActionTitles);
        return Collections.singleton(action);
    }

    public final class InternalURLTriggerAction extends URLTriggerAction {

        private transient String label;
        private transient Map<String, String> subActionTitle;

        public InternalURLTriggerAction(String label, Map<String, String> subActionTitle) {
            this.label = label;
            this.subActionTitle = subActionTitle;
        }

        @SuppressWarnings("unused")
        public BuildableItem getOwner() {
            return job;
        }

        @SuppressWarnings("unused")
        public String getLabel() {
            return label;
        }

        @Override
        public String getIconFileName() {
            return "clipboard.gif";
        }

        @Override
        public String getDisplayName() {
            return "URLTrigger Log";
        }

        @Override
        public String getUrlName() {
            return "urltriggerPollLog";
        }

        @SuppressWarnings("unused")
        public String getLog() throws IOException {
            return Util.loadFile(getLogFile());
        }

        @SuppressWarnings("unused")
        public Map<String, String> getSubActionTitle() {
            return subActionTitle;
        }

        @SuppressWarnings("unused")
        @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED")
        public void writeLogTo(XMLOutput out) throws IOException {
            new AnnotatedLargeText<URLTriggerAction>(getLogFile(), Charset.defaultCharset(), true, this).writeHtmlTo(0, out.asWriter());
        }

    }

    private String getURLValue(URLTriggerEntry entry, Node node , XTriggerLog log) throws XTriggerException {
        String entryURL = entry.getUrl();
        if (entryURL != null) {
            //EnvVarsResolver varsRetriever = new EnvVarsResolver();
            Map<String, String> envVars;
            //try {
            	//if( entry.isUseGlobalEnvVars() ) {
            		log.info( "Resolving environment variables using global values" );
            		envVars = new EnvVars() ;
                    Jenkins hudson = Jenkins.getInstanceOrNull();
                    if (hudson != null) {
                        DescribableList<NodeProperty<?>, NodePropertyDescriptor> globalNodeProperties = hudson.getGlobalNodeProperties();
                        if (globalNodeProperties != null) {
                            for (NodeProperty<?> nodeProperty : globalNodeProperties) {
                                if (nodeProperty != null && nodeProperty instanceof EnvironmentVariablesNodeProperty) {
                                    envVars.putAll(((EnvironmentVariablesNodeProperty) nodeProperty).getEnvVars());
                                }
                            }
                        }
                    }
            	//} else {
            	//	log.info( "Resolving environment variables using last build values" );
            	//	envVars = varsRetriever.getPollingEnvVars((AbstractProject) job, node);           		
            	//}
            //} catch (EnvInjectException e) {
            //    throw new XTriggerException(e);
            //}
            return Util.replaceMacro(entryURL, envVars);
        }
        return null;
    }


    @Override
    protected boolean checkIfModified(Node pollingNode, XTriggerLog log) throws XTriggerException {

        if (entries == null || entries.isEmpty()) {
            log.info("No URLs to poll.");
            return false;
        }

        for (URLTriggerEntry entry : entries) {
            boolean modified = checkIfModifiedEntry(entry, pollingNode, log);
            if (modified) {
            	this.buildCause = new URLTriggerCause( true ) ;
                this.buildCause.setUrlTrigger(entry.getUrl());
                return true;
            }
        }

        return false;
    }

    private boolean checkIfModifiedEntry(URLTriggerEntry entry, Node pollingNode, XTriggerLog log) throws XTriggerException {
        String resolvedURL = getURLValue(entry, pollingNode , log);
        URLTriggerResolvedEntry resolvedEntry = new URLTriggerResolvedEntry(resolvedURL, entry);

        if (!resolvedEntry.isURLTriggerValidURL())
            throw new IllegalArgumentException("Only http(s) and ftp URLs are supported. For non-http/ftp protocols, consider other XTrigger plugins");

        if( resolvedEntry.getResolvedURL().contains( "$" ) ) {
        		log.info( "URL contains unresolved environment variables." ) ;
            log.info( "Skipping URLTrigger initialization. Waiting next schedule" ) ;
            return false ;
        }
        
        if (resolvedEntry.isHttp() || resolvedEntry.isHttps()) {
            return checkIfModifiedEntryForHttpOrHttpsURL(resolvedEntry, log);
        }

        return checkIfModifiedEntryFoFTPURL(resolvedEntry, log);
    }

    private boolean checkIfModifiedEntryForHttpOrHttpsURL(URLTriggerResolvedEntry resolvedEntry, XTriggerLog log) throws XTriggerException {

        Client client = getClientObject(resolvedEntry, log);

        String url = resolvedEntry.getResolvedURL();
        Invocation.Builder webResourceBuilder = client.target(url).request();
        
        List< URLTriggerRequestHeader > requestHeaders = resolvedEntry.getEntry().getRequestHeaders() ;
        if( requestHeaders.size() > 0 ) {
        	for( URLTriggerRequestHeader requestHeader : requestHeaders ) {
                String safeValue = requestHeader.maskValue ? "<MASKED>" : requestHeader.headerValue ;
        		log.info("Adding header - " + requestHeader.headerName + ":" + safeValue) ;
        		webResourceBuilder = webResourceBuilder.header(requestHeader.headerName, requestHeader.headerValue) ;
        	}
        }
        
        log.info(String.format("Invoking the url: %n %s", url));
        Response clientResponse = webResourceBuilder.get();

        URLTriggerEntry entry = resolvedEntry.getEntry();

        if (isServiceUnavailableAndNotExpected(clientResponse, entry)
        		|| isURLNotFoundAndNotExpected( clientResponse , entry )) {
            log.info("URL to poll unavailable.");
            log.info("Skipping URLTrigger initialization. Waiting next schedule");
            return false;
        }

        URLResponse response = new HTTPResponse(clientResponse);
        URLTriggerService urlTriggerService = URLTriggerService.getInstance();
        return urlTriggerService.isSchedulingAndGetRefresh(response, entry, log);

    }

    private boolean checkIfModifiedEntryFoFTPURL(URLTriggerResolvedEntry resolvedEntry, XTriggerLog log) throws XTriggerException {
        URLResponse response;
        try {
            response = getFTPResponse(resolvedEntry);
            if (response == null) {
                return false;
            }
            log.info("FTP poll result: " + response.getEntityTagValue());
        } catch (Exception ex) {
            log.info("Failed to poll URL: " + ex);
            log.info("Skipping URLTrigger initialization. Waiting next schedule");
            return false;
        }
        URLTriggerService urlTriggerService = URLTriggerService.getInstance();
        return urlTriggerService.isSchedulingAndGetRefresh(response, resolvedEntry.getEntry(), log);

    }

    private boolean isServiceUnavailableAndNotExpected(Response clientResponse, URLTriggerEntry entry) {
        return HttpServletResponse.SC_SERVICE_UNAVAILABLE == clientResponse.getStatus()
                && entry.getStatusCode() != HttpServletResponse.SC_SERVICE_UNAVAILABLE;
    }

    private boolean isURLNotFoundAndNotExpected(Response clientResponse , URLTriggerEntry entry ) {
    		return HttpServletResponse.SC_NOT_FOUND == clientResponse.getStatus()
    				&& entry.getStatusCode() != HttpServletResponse.SC_NOT_FOUND ;
    }
    
    @Override
    public String getCause() {
        return URLTriggerCause.CAUSE;
    }
    
    @Override
    protected XTriggerCause getBuildCause() {
    	return this.buildCause ;
    }

    @Override
    protected String getName() {
        return URLTriggerCause.NAME;
    }

    private Client getClientObject(URLTriggerResolvedEntry resolvedEntry, XTriggerLog log) throws XTriggerException {

        URLTriggerEntry entry = resolvedEntry.getEntry();
        Client client = createClient(resolvedEntry.isHttps(), entry.isProxyActivated(), entry.getTimeout());
        if (isAuthBasic(entry)) {
            addBasicAuth(entry, log, client);
        }
        client.property(ClientProperties.FOLLOW_REDIRECTS, entry.isFollowRedirects());
        return client;
    }

    private Client createClient(boolean isHttps, boolean withProxy, int timeout) throws XTriggerException {
        Client client;
        if (withProxy) {
            client = createClientWithProxy(isHttps, timeout);
        } else {
            client = createClientWithoutProxy(isHttps, timeout);
        }
        return client;
    }

    private void addBasicAuth(URLTriggerEntry entry, XTriggerLog log, Client client) {
        if (log != null) {
            log.info(String.format("Using Basic Authentication with the user '%s'", entry.getUsername()));
        }
        String password = entry.getRealPassword();
        client.register(HttpAuthenticationFeature.basic(entry.getUsername(), password));
    }

    private Client createClientWithoutProxy(boolean isHttps, int timeout) throws XTriggerException {
        ClientBuilder config = ClientBuilder.newBuilder();
        if (isHttps) {
            config.sslContext(getSSLContext());
            config.hostnameVerifier(getHostnameVerifier());
        }

        /*
         * Set a connect and read timeout. If this hangs, it can actually
         * take down all of the Jenkins schedule events.
         */
        config.connectTimeout(timeout, TimeUnit.SECONDS);
        config.readTimeout(timeout, TimeUnit.SECONDS);

        return config.build();
    }

    private HostnameVerifier getHostnameVerifier() {
        return (hostname, sslSession) -> true;
    }

    private SSLContext getSSLContext() throws XTriggerException {
        javax.net.ssl.TrustManager x509 = new javax.net.ssl.X509TrustManager() {

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws java.security.cert.CertificateException {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws java.security.cert.CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        SSLContext ctx;
        try {
            ctx = SSLContext.getInstance("SSL");
            ctx.init(null, new javax.net.ssl.TrustManager[]{x509}, null);
        } catch (java.security.GeneralSecurityException ex) {
            throw new XTriggerException(ex);
        }

        return ctx;
    }


    private Client createClientWithProxy(boolean isHttps, int timeout) throws XTriggerException {
        ClientBuilder config = ClientBuilder.newBuilder();

        //-- Proxy
        Jenkins h = Jenkins.get(); // this code might run on slaves
        ProxyConfiguration p = h.proxy ;
        if (p != null) {
            config.withConfig(new ClientConfig().connectorProvider(new ApacheConnectorProvider()));
	    config.property(ClientProperties.PROXY_URI, "http://" + p.name + ":" + p.port);
            config.property(ClientProperties.PROXY_USERNAME, p.getUserName());
            String password = getProxyPasswordDecrypted(p);
            config.property(ClientProperties.PROXY_PASSWORD, Util.fixNull(password));
        }

        //-- Https
        if (isHttps) {
            config.sslContext(getSSLContext());
            config.hostnameVerifier(getHostnameVerifier());
        }

        /*
         * Set a connect and read timeout. If this hangs, it can actually
         * take down all of the Jenkins schedule events.
         */
        config.connectTimeout(timeout, TimeUnit.SECONDS);
        config.readTimeout(timeout, TimeUnit.SECONDS);

        return config.build();
    }

    private String getProxyPasswordDecrypted(ProxyConfiguration p) {
        String passwordEncrypted = p.getPassword();
        String password = null;
        if (passwordEncrypted != null) {
            Secret secret = Secret.fromString(passwordEncrypted);
            password = Secret.toString(secret);
        }
        return password;
    }

    private boolean isAuthBasic(URLTriggerEntry entry) {
        return entry.getUsername() != null;
    }

    @Override
    protected File getLogFile() {
    	if( job != null ) {
    		return new File(job.getRootDir(), "trigger-script-polling.log");
    	} else {
    		return null ;
    	}
    }

    @Override
    protected Action[] getScheduledActions(Node node, XTriggerLog log) {
        return new Action[0];
    }

    @Override
    protected boolean requiresWorkspaceForPolling() {
        return false;
    }

//    @Override
//   protected void start(Node node, BuildableItem buildableItem, boolean newInstance, XTriggerLog log) {
//    }

    
    @Override
    public URLTriggerDescriptor getDescriptor() {
        return (URLTriggerDescriptor) Jenkins.get().getDescriptorOrDie(this.getClass());
    }

    private static FTPClient getFTPClientObject(URLTriggerResolvedEntry resolvedEntry) throws URISyntaxException, IOException {
        URLTriggerEntry entry = resolvedEntry.getEntry();
        return getFTPClientObject(resolvedEntry.getResolvedURL(), entry.getUsername(), entry.getRealPassword());
    }

    private static FTPClient getFTPClientObject(String url, String basicUsername, String basicPassword) throws URISyntaxException, IOException {
        URI uri = new URI(url);
        String host = uri.getHost();
        int port = uri.getPort();
        String userInfo = uri.getUserInfo();

        FTPClient ftpClient = new FTPClient();
        if (port < 0) {
            ftpClient.connect(host);
        } else {
            ftpClient.connect(host, port);
        }

        if (userInfo != null && !userInfo.isEmpty() || basicUsername != null) {
            String user, pass;

            if (userInfo != null && !userInfo.isEmpty()) {
                int i = userInfo.indexOf(':');
                user = i < 0 ? userInfo : userInfo.substring(0, i);
                pass = i < 0 ? "" : userInfo.substring(i + 1);
            } else {
                user = basicUsername;
                pass = basicPassword;
            }

            if (!ftpClient.login(user, pass)) {
                throw new java.io.IOException("Authentification failed");
            }
        }

        return ftpClient;
    }

    private FTPResponse getFTPResponse(URLTriggerResolvedEntry resolvedEntry) throws IOException, ParseException, URISyntaxException {

        FTPClient ftpClient = getFTPClientObject(resolvedEntry);

        URI uri = new URI(resolvedEntry.getResolvedURL());
        FTPResponse response = new FTPResponse();
        String timeResponse = ftpClient.getModificationTime(uri.getPath());
        if (timeResponse == null) {
            return null;
        }
        String[] dateResponse = timeResponse.split(" "); //assume "CODE yyyyMMddhhmmss"
        if (dateResponse.length != 2) {
            throw new IOException("Illegal FTP response");
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
        response.setLastModified(format.parse(dateResponse[1]));
        response.setStatus(Integer.parseInt(dateResponse[0]));
        response.setEntityTagValue(timeResponse); // I'm not sure what else could be used

        URLTriggerEntry entry = resolvedEntry.getEntry();
        if (entry.isInspectingContent()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ftpClient.retrieveFile(uri.getPath(), baos);
            response.setContent(baos.toString("UTF-8"));
        }

        return response;
    }

    @Symbol( "URLTrigger")
    @Extension
    public static class URLTriggerDescriptor extends XTriggerDescriptor {

        private transient final SequentialExecutionQueue queue = new SequentialExecutionQueue(Executors.newSingleThreadExecutor(new ThreadFactory() {
            // TODO use NamingThreadFactory since Jenkins 1.541
            private final ThreadFactory factory = Executors.defaultThreadFactory();
            public Thread newThread(Runnable r) {
                Thread thread = factory.newThread(r);
                thread.setName("URLTrigger queue thread");
                return thread;
            }
        }));

        @Override
        public ExecutorService getExecutor() {
            return queue.getExecutors();
        }

        @Override
        public boolean isApplicable(Item item) {
            return true;
        }

        @Override
        public URLTrigger newInstance(StaplerRequest req, JSONObject formData) throws FormException {

            String cronTabSpec = formData.getString("cronTabSpec");
            boolean labelRestriction = false;
            String triggerLabel = null;
            Object labelRestrictionObject = formData.get("labelRestriction");
            if (labelRestrictionObject != null) {
                labelRestriction = true;
                triggerLabel = ((JSONObject) labelRestrictionObject).getString("triggerLabel");
            }

            Object entryObject = formData.get("urlElements");

            List<URLTriggerEntry> entries = new ArrayList<>();
            if (entryObject instanceof JSONObject) {
                entries.add(fillAndGetEntry(req, (JSONObject) entryObject));
            } else {
                JSONArray jsonArray = (JSONArray) entryObject;
                if (jsonArray != null) {
                    for (Object aJsonArray : jsonArray) {
                        entries.add(fillAndGetEntry(req, (JSONObject) aJsonArray));
                    }
                }
            }

            URLTrigger urlTrigger;
            try {
                urlTrigger = new URLTrigger(cronTabSpec, entries, labelRestriction, triggerLabel);
            } catch (ANTLRException e) {
                throw new RuntimeException(e.getMessage());
            }

            return urlTrigger;

        }

        private URLTriggerEntry fillAndGetEntry(StaplerRequest req, JSONObject entryObject) {
            URLTriggerEntry urlTriggerEntry = new URLTriggerEntry();
            urlTriggerEntry.setUrl(entryObject.getString("url"));
            urlTriggerEntry.setFollowRedirects(entryObject.getBoolean("followRedirects"));
            urlTriggerEntry.setProxyActivated(entryObject.getBoolean("proxyActivated"));
            urlTriggerEntry.setUseGlobalEnvVars(entryObject.getBoolean("useGlobalEnvVars"));
            String username = Util.fixEmpty(entryObject.getString("username"));
            if (username != null) {
                urlTriggerEntry.setUsername(username);
                Secret secret = Secret.fromString(Util.fixEmpty(entryObject.getString("password")));
                String encryptedValue = secret.getEncryptedValue();
                urlTriggerEntry.setPassword(encryptedValue);
            }

            //Process timeout
            urlTriggerEntry.setTimeout(5 * 60); // 5 minutes by default
            String timeout = entryObject.getString("timeout");
            if (timeout != null) {
                try {
                    int timeoutSeconds = Integer.parseInt(timeout);
                    urlTriggerEntry.setTimeout(timeoutSeconds);
                } catch (NumberFormatException ne) {
                    //no change default timeout
                }
            }

            //Process checkStatus
            Object checkStatusObject = entryObject.get("checkStatus");
            if (checkStatusObject != null) {
                urlTriggerEntry.setCheckStatus(true);
                try {
                    JSONObject statusJSONObject = (JSONObject) checkStatusObject;
                    urlTriggerEntry.setStatusCode(statusJSONObject.getInt("statusCode"));
                } catch (JSONException jsne) {
                    urlTriggerEntry.setStatusCode(URLTriggerEntry.DEFAULT_STATUS_CODE);
                }
            } else {
                urlTriggerEntry.setCheckStatus(false);
                urlTriggerEntry.setStatusCode(URLTriggerEntry.DEFAULT_STATUS_CODE);
            }

            //Process checkETag
            urlTriggerEntry.setCheckETag(entryObject.getBoolean("checkETag"));

            //Process checkLastModifiedDate
            urlTriggerEntry.setCheckLastModificationDate(entryObject.getBoolean("checkLastModificationDate"));
            
            //Process requestHeaders
            List< URLTriggerRequestHeader > requestHeaders = new ArrayList<>() ;
            Object requestHeaderListObject = entryObject.get("urlRequestHeaders") ;
            JSONArray requestHeaderListArray;
            if( requestHeaderListObject instanceof JSONObject ) {
                requestHeaderListArray = new JSONArray();
                requestHeaderListArray.add( requestHeaderListObject );
            } else {
            	requestHeaderListArray = (JSONArray) requestHeaderListObject ;
            }

        	if( requestHeaderListArray != null ) {
           		for( Object requestHeaderItemObject : requestHeaderListArray ) {
                   	JSONObject requestHeaderItem = (JSONObject) requestHeaderItemObject ;
                   	String headerName = Util.fixEmpty(requestHeaderItem.getString("headerName")) ;
                   	String headerValue = Util.fixEmpty(requestHeaderItem.getString("headerValue" )) ;
                   	if( headerName != null && headerValue != null ) {
           		        requestHeaders.add( new URLTriggerRequestHeader( headerName , headerValue , requestHeaderItem.getBoolean("maskValue")) ) ;
                   	}
           		}
           	}

            urlTriggerEntry.setRequestHeaders(requestHeaders);

            //Process inspectingContent
            Object inspectingContentObject = entryObject.get("inspectingContent");
            if (inspectingContentObject == null) {
                urlTriggerEntry.setInspectingContent(false);
                urlTriggerEntry.setContentTypes(new URLTriggerContentType[0]);
            } else {
                urlTriggerEntry.setInspectingContent(true);
                JSONObject inspectingContentJSONObject = entryObject.getJSONObject("inspectingContent");
                if (inspectingContentJSONObject.size() == 0) {
                    urlTriggerEntry.setInspectingContent(false);
                } else {
                    JSON contentTypesJsonElt;
                    try {
                        contentTypesJsonElt = inspectingContentJSONObject.getJSONArray("contentTypes");
                    } catch (JSONException jsone) {
                        contentTypesJsonElt = inspectingContentJSONObject.getJSONObject("contentTypes");
                    }
                    List<URLTriggerContentType> types = req.bindJSONToList(URLTriggerContentType.class, contentTypesJsonElt);
                    urlTriggerEntry.setContentTypes(types.toArray(new URLTriggerContentType[0]));
                }
            }
            return urlTriggerEntry;
        }

        @Override
        public String getDisplayName() {
            return Messages.urltrigger_displayName();
        }

        @Override
        public String getHelpFile() {
            return "/plugin/urltrigger/help.html";
        }

        public DescriptorExtensionList<URLTriggerContentType, Descriptor<URLTriggerContentType>> getListURLTriggerDescriptors() {
            return DescriptorExtensionList.createDescriptorList(Jenkins.get(), URLTriggerContentType.class);
        }

        public FormValidation doCheckURL(@QueryParameter String value) {

            if (value == null || value.trim().isEmpty()) {
                return FormValidation.error("The url field is mandatory.");
            }

            if (!value.startsWith("http") && !value.startsWith("ftp"))
                return FormValidation.error("Only http(s) and ftp URLs are supported. For non-http/ftp protocols, consider other XTrigger plugins");

            if ( value.contains( "$" ) ) {
            	return FormValidation.warning( "URL is parameterised and cannot be fully validated" ) ;
            }
            
            return FormValidation.ok();
        }

        public FormValidation doCheckTimeout(@QueryParameter String value) {

            if ((value != null) && (value.trim().length() != 0)) {
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException ne) {
                    return FormValidation.error("You must provide a timeout number (in seconds).");
                }
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckStatus(@QueryParameter String value) {

            if (value == null || value.trim().isEmpty()) {
                return FormValidation.ok();
            }
            try {
                Integer.parseInt(value);
                return FormValidation.ok();
            } catch (Exception e) {
                return FormValidation.error("You must provide a valid number status such as 200, 301, ...");
            }
        }

    }
}
