#!groovy
// Jenkins init script for CI test container.
// Sets up admin user and permissive security for automated test execution.

import jenkins.model.*
import hudson.security.*

def instance = Jenkins.getInstance()

// Create admin user
def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount("admin", "admin")
instance.setSecurityRealm(hudsonRealm)

// Allow all authenticated users full control.
// Using UNSECURED strategy for test container — all API calls are permitted
// without additional authorization checks. This matches the behavior of the
// original Bitnami Jenkins 2.164.3 image used prior to the Tomcat 11 uplift.
instance.setAuthorizationStrategy(AuthorizationStrategy.UNSECURED)

// Disable CSRF crumb requirement for API calls.
// eiffel-commons JenkinsManager does not preserve HTTP session cookies
// between crumb-fetch and API calls. Jenkins 2.x+ ties crumbs to sessions,
// causing 403 on job creation. Since this is a disposable CI test container,
// CSRF protection is not needed.
instance.setCrumbIssuer(null)

instance.save()
