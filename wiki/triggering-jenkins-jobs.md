# Trigger Jenkins

On this page we only explain the subscription fields which are relevant to trigger 
Jenkins jobs. For explanations of the other fields in the subscriptions, see [REST POST subscription](subscription-with-REST-POST-notification.md).

There are some important details to know when triggering Jenkins jobs with parameters and/or token.

## Using _**token**_
    Tokens need to be treated as constants so they should be surrounded with single quotes. See examples below.

## _**buildWithParameters**_ Endpoint
   * Jenkins discards the body specified in notificationMessageKeyValues
   * the parameters need to be send in the URL
   * do not specify more parameters than you have in your job. This is a Jenkins security feature to hinder that someone triggers jobs that overwrite job environment variables. Your job will not be triggered otherwise.

Example below shows a subscription that triggers a parameterized Jenkins 
job having job token and a parameter object containing the aggregated object.
Observe that we use buildWithParameters and empty notificationMessageKeyValues.

### authenticationType
If any authentication is needed by Eiffel Intelligence to send the notification 
HTTP request. This authentication type is "Jenkins CSRF Protection (crumb)", 
in which the username and password will be Base 64 encoded. A crumb will be 
fetched automatically before the HTTP request is made. (Currently default in 
many Jenkins instances). Even if the Jenkins instance has CSRF disabled this 
authentication type works. BASIC_AUTH can also be used.

### userName & password
The username and password Eiffel Intelligence will use in headers of the HTTP 
request when sending a notification via HTTP POST.

### notificationMeta
Which url to use for the HTTP POST request. This url contains parameters to 
trigger a specific Jenkins job. 

### restPostBodyMediaType
Headers for the HTTP request, can be 'application/x-www-form-urlencoded' or 
'application/json'. 

### notificationMessageKeyValues
The data to send with the HTTP POST request. Jenkins ignores any body sent 
so we leave it empty in the first subscription.

For the second subscription example we use 'application/json'. The form key 
should be left empty. The form value will be run through JMESPath engine so
it is possible to use JMESPath expressions to extract content from the 
aggregated object. The form value can only be one JSON object.

    {
        "subscriptionName" : "Subscription1",
        "ldapUserName" : "ABC",
        "repeat" : false,
        "created" : 1542117412833,

        "authenticationType" : "BASIC_AUTH_JENKINS_CSRF",
        "userName" : "functionalUser",
        "password" : "functionalUserPassword",

        "notificationType" : "REST_POST",
        "notificationMeta" : "http://eiffel-jenkins1:8080/job/ei-artifact-triggered-job/buildWithParameters?token='TOKEN'&object=id",

        "restPostBodyMediaType" : "application/json",
        "notificationMessageKeyValues" : [
            {
            }
        ],
        "requirements" : [
            {
                "conditions" : [
                    {
                        "jmespath" : "identity=='pkg:maven/com.othercompany.library/artifact-name@1.0.0'"
                    }
                ]
            }
        ]
    }

## _**build**_ endpoint
   * the parameters should be specified in notificationMessageKeyValues. The same rule of not adding more parameters than the job is configured with applies here. Your job will not be triggered otherwise.
   * no job parameters in the URL

The subscription below triggers the same parameterized Jenkins job but we now 
use the build endpoint and we send the parameters in a json form using REST body.

    {
        "subscriptionName" : "Subscription1",
        "ldapUserName" : "ABC",
        "repeat" : false,
        "created" : 1542117412833,

        "authenticationType" : "BASIC_AUTH_JENKINS_CSRF",
        "userName" : "functionalUser",
        "password" : "functionalUserPassword",

        "notificationType" : "REST_POST",
        "notificationMeta" : "http://eiffel-jenkins1:8080/job/ei-artifact-triggered-job/build?token='TOKEN'",

        "restPostBodyMediaType" : "application/json",
        "notificationMessageKeyValues" : [
            {
                "formkey" : "",
                "formvalue" : "{parameter: [{ name: 'object', value : to_string(@) }]}"
            }
        ],
        "requirements" : [
            {
                "conditions" : [
                    {
                        "jmespath" : "identity=='pkg:maven/com.othercompany.library/artifact-name@1.0.0'"
                    },
                    {
                        "jmespath" : "confidenceLevels[?name=='my_confidence_level']"
                    }
                ]
            }
        ]
    }

## Requirements and Conditions
Read more on how Eiffel Intelligence groups [requirements and conditions in subscriptions](subscriptions.md#writing-requirements-and-conditions).
