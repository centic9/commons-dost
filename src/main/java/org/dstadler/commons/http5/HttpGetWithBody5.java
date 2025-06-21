package org.dstadler.commons.http5;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

import java.net.URI;

/**
 * HTTP GET method which allows to set a body as part of the request.
 * <p>
 * The HTTP GET method is defined in section 9.3 of
 * <a href="http://www.ietf.org/rfc/rfc2616.txt">RFC2616</a>:
 * </p>
 * <blockquote>
 * The GET method means retrieve whatever information (in the form of an
 * entity) is identified by the Request-URI. If the Request-URI refers
 * to a data-producing process, it is the produced data which shall be
 * returned as the entity in the response and not the source text of the
 * process, unless that text happens to be the output of the process.
 * </blockquote>
 */
public class HttpGetWithBody5 extends HttpUriRequestBase {

    public HttpGetWithBody5(String uri) {
        super(HttpGet.METHOD_NAME, URI.create(uri));
    }
}
