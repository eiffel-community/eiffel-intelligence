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
    private static final String WRITTEN_SESSION_ID_ATTR = getSessionIdAttributeName();

    private CookieSerializer cookieSerializer = new DefaultCookieSerializer();

    @Override
    public List<String> resolveSessionIds(HttpServletRequest request) {
        List<String> sessionIds;
        String headerValue = request.getHeader(X_AUTH_TOKEN);
        if (headerValue == null) {
            sessionIds = this.cookieSerializer.readCookieValues(request);
        } else {
            sessionIds = Collections.singletonList(headerValue);
        }

        return sessionIds;
    }

    @Override
    public void setSessionId(HttpServletRequest request, HttpServletResponse response,
            String sessionId) {
        response.setHeader(X_AUTH_TOKEN, sessionId);

        if (isRequestSessionIdSet(request, sessionId)) {
            return;
        }
        request.setAttribute(WRITTEN_SESSION_ID_ATTR, sessionId);
        CookieValue cookieValue = new CookieValue(request, response, sessionId);
        this.cookieSerializer.writeCookieValue(cookieValue);
    }

    @Override
    public void expireSession(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader(X_AUTH_TOKEN, "");
        CookieValue cookieValue = new CookieValue(request, response, "");
        this.cookieSerializer.writeCookieValue(cookieValue);
    }

    private static String getSessionIdAttributeName() {
        final String className = HeaderAndCookieHttpSessionIdResolver.class.getName();
        final String sessionIdAttributeName = className.concat(".WRITTEN_SESSION_ID_ATTR");
        return sessionIdAttributeName;
    }

    private boolean isRequestSessionIdSet(HttpServletRequest request, String sessionId) {
        String requestSessionId = request.getAttribute(WRITTEN_SESSION_ID_ATTR).toString();
        boolean isRequestSessionIdSet = sessionId.equals(requestSessionId);
        return isRequestSessionIdSet;
    }
}
