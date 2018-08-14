package com.ericsson.ei.config;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.CookieSerializer.CookieValue;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HttpSessionIdResolver;

public final class HeaderAndCookieHttpSessionIdResolver implements HttpSessionIdResolver {

    private static final String X_AUTH_TOKEN = "X-Auth-Token";
    private static final String WRITTEN_SESSION_ID_ATTR = HeaderAndCookieHttpSessionIdResolver.class.getName()
            .concat(".WRITTEN_SESSION_ID_ATTR");

    private CookieSerializer cookieSerializer = new DefaultCookieSerializer();

    @Override
    public List<String> resolveSessionIds(HttpServletRequest request) {
        String headerValue = request.getHeader(X_AUTH_TOKEN);
        return (headerValue != null ? Collections.singletonList(headerValue)
                : this.cookieSerializer.readCookieValues(request));
    }

    @Override
    public void setSessionId(HttpServletRequest request, HttpServletResponse response, String sessionId) {
        response.setHeader(X_AUTH_TOKEN, sessionId);
        if (sessionId.equals(request.getAttribute(WRITTEN_SESSION_ID_ATTR))) {
            return;
        }
        request.setAttribute(WRITTEN_SESSION_ID_ATTR, sessionId);
        this.cookieSerializer.writeCookieValue(new CookieValue(request, response, sessionId));
    }

    @Override
    public void expireSession(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader(X_AUTH_TOKEN, "");
        this.cookieSerializer.writeCookieValue(new CookieValue(request, response, ""));
    }
}
