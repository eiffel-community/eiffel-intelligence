# Authentication

|Method|Endpoint         |Authentication|
|------|-----------------|--------------|
|GET   |/auth            |no            |
|GET   |/auth/login      |yes           |
|GET   |/auth/logout     |no            |
|GET   |/auth/checkStatus|yes           |

Authentication is not a requirement but can be turned on and off in the application properties file with the '_ldap.enabled_' property.

## Check if security is enabled

Returns the json variable '_security_' that is set to either true or false 

    GET /auth

Curl command

    curl -X GET -H "Content-type: application/json" http://<host>:8090/auth

Example of response body

    {"security":true}

## Login point that returns the name of current user

Upon the first time valid credentials are used the response will contain an 'X-Auth-Token' header that must be saved and attached to any following calls to endpoints that require authentication. If the same client connection is used then session cookie is used to authenticate and 'X-Auth-Token' would not be needed in that case.

    GET /auth/login

Curl command

    curl -X GET -H "Content-type: application/json" -u <username>:<password> http://<host>:8090/auth/login

Example of full response

````
< HTTP/1.1 200 
< X-Content-Type-Options: nosniff
< X-XSS-Protection: 1; mode=block
< Cache-Control: no-cache, no-store, max-age=0, must-revalidate
< Pragma: no-cache
< Expires: 0
< X-Frame-Options: DENY
< X-Auth-Token: 791541ad-67aa-4630-9698-66638e6987c2
< Set-Cookie: SESSION=NzkxNTQxYWQtNjdhYS00NjMwLTk2OTgtNjY2MzhlNjk4N2My; Path=/; HttpOnly
< Content-Type: application/json;charset=UTF-8
< Content-Length: 16
< Date: Wed, 15 Aug 2018 10:58:23 GMT
< 
* Connection #0 to host localhost left intact
{"user":"myuser"}
````

## Delete session of current user

Removes the current session bound to the client. If 'X-Auth-Token' is used to authenticate then the same token needs to be attached when calling the logout endpoint.

    GET /auth/logout

Curl command with session cookie

    curl -X GET -H "Content-type: application/json" http://<host>:8090/auth/logout

Curl command with token

    curl -X GET -H "Content-type: application/json" -H "X-Auth-Token: <uuid>" http://<host>:8090/auth/logout

## Check if backend is running

An arbitrary endpoint to check if backend is up and running

    GET /auth/checkStatus

Curl command

    curl -X GET -H "Content-type: application/json" http://<host>:8090/auth/checkStatus

Example of response body

    Backend server is up and running
