package com.china.http.client.v2;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 自定义http重试策略 <br>
 *
 * @author gan.wang@rootcloud.com<br>
 * @version 1.0<br>
 * @date 2021/08/28 <br>
 */
public class SelfHttpRequestRetryHandler implements HttpRequestRetryHandler {

    /**
     * the number of times a method will be retried
     */
    private final int retryCount;

    /**
     * 默认重试次数
     */
    public static final Integer DEFAULT_RETRY_COUNT = 3;

    /**
     * Whether or not methods that have successfully sent their request will be retried
     */
    private final boolean requestSentRetryEnabled;

    private final Set<Class<? extends IOException>> nonRetriableClasses;

    public SelfHttpRequestRetryHandler(final int retryCount) {
        this(retryCount, false);
    }

    public SelfHttpRequestRetryHandler(final int retryCount, final boolean requestSentRetryEnabled) {
        this(retryCount, requestSentRetryEnabled, Arrays.asList(
                InterruptedIOException.class,
                UnknownHostException.class,
                ConnectException.class,
                SSLException.class));
    }

    protected SelfHttpRequestRetryHandler(
            final int retryCount,
            final boolean requestSentRetryEnabled,
            final Collection<Class<? extends IOException>> clazzes) {
        super();
        this.retryCount = retryCount;
        this.requestSentRetryEnabled = requestSentRetryEnabled;
        this.nonRetriableClasses = new HashSet<>();
        this.nonRetriableClasses.addAll(clazzes);
    }

    public SelfHttpRequestRetryHandler() {
        this(DEFAULT_RETRY_COUNT, false);
    }


    @Override
    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
        Args.notNull(exception, "Exception parameter");
        Args.notNull(context, "HTTP context");

        if (executionCount > this.retryCount) {
            // Do not retry if over max retry count
            throw new RuntimeException("httpRequest arrive max retry count");
        }
        if (this.nonRetriableClasses.contains(exception.getClass())) {
            return false;
        }
        for (final Class<? extends IOException> rejectException : this.nonRetriableClasses) {
            if (rejectException.isInstance(exception)) {
                return false;
            }
        }
        final HttpClientContext clientContext = HttpClientContext.adapt(context);
        final HttpRequest request = clientContext.getRequest();

        if (handleAsIdempotent(request)) {
            // Retry if the request is considered idempotent
            return true;
        }

        // Retry if the request has not been sent fully or
        // if it's OK to retry methods that have been sent
        return !clientContext.isRequestSent() || this.requestSentRetryEnabled;
        // otherwise do not retry
    }

    protected boolean handleAsIdempotent(final HttpRequest request) {
        return !(request instanceof HttpEntityEnclosingRequest);
    }
}
