# Authentication

|Method|Endpoint                   |Authentication|
|------|---------------------------|--------------|
|GET   |/authentication            |no            |
|GET   |/authentication/login      |yes           |
|GET   |/authentication/logout     |no            |

Authentication is not a requirement but can be turned on and off in the
application properties file with the '_ldap.enabled_' property.

If Eiffel Intelligence has LDAP enabled it is possible to limit access
to subscriptions. If **user A** creates a subscription then **user B**
can not edit or delete that subscription. It is not possible to change
ownership of a subscription once it is made. When LDAP is activated any
guest users, i.e. not logged in, can only **view** but not edit or delete
the subscriptions.

When Eiffel Intelligence is configured without LDAP the subscriptions are
available to anyone to view, edit and delete.

## Check if Security is Enabled

Returns the json variable '_security_' that is set to either true or false

    GET /authentication

Curl command

    curl -X GET -H "Content-type: application/json" http://<host>:8090/authentication

Example of response body

    {"security":true}

## Login Endpoint Returns the Name of Current User

Upon the first time valid credentials are used the response will contain an
'X-Auth-Token' header that must be saved and attached to any following calls to
endpoints that require authentication. If the same client connection is used
then session cookie is used to authenticate and 'X-Auth-Token' would not be
needed in that case.

    GET /authentication/login

Curl command

    curl -X GET -H "Content-type: application/json" -u <username>:<password> http://<host>:8090/authentication/login

Example of full response

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

## Delete Session of Current User

Removes the current session bound to the client. If 'X-Auth-Token' is used to
authenticate then the same token needs to be attached when calling the logout
endpoint.

    GET /authentication/logout

Curl command with session cookie

    curl -X GET -H "Content-type: application/json" http://<host>:8090/authentication/logout

Curl command with token

    curl -X GET -H "Content-type: application/json" -H "X-Auth-Token: <uuid>" http://<host>:8090/authentication/logout
