# Trigger Jenkins

There are some important details to know when triggering Jenkins jobs with parameters and/or token.

## Using _**token**_
    Tokens need to be treated as constants so they should be surrounded with single quotes. See examples below.

## _**buildWithParameters**_ endpoint
   * Jenkins discards the body specified in notificationMessageKeyValues
   * the parameters need to be send in the URL
   * do not specify more parameters than you have in your job. This is a Jenkins security feature to hinder that someone triggers jobs that overwrite job environment variables. Your job will not be triggered otherwise.

Example below shows a subscription that triggers a parameterized Jenkins job having job token and a parameter object containing the aggregated object.
Observe that we use buildWithParameters and empty notificationMessageKeyValues.

    {
        // The name of the subscription to make it easy to search for.
        // Only numbers,letters and underscore allowed.
        "subscriptionName" : "Subscription1",

        // The name of the logged in user creating or updating the subscription
        // added by Eiffel Intelligence if LDAP is enabled. Not required. Defaults to an empty string.
        "ldapUserName" : "ABC",

        // Instructs whether the same subscription should be re-triggered
        // for new additions to the aggregated object. If this is set to
        // false, only the first time the conditions are fulfilled, a
        // notification will be triggered. No matter how many times the
        // aggregated object is updated.
        "repeat" : false,

        // Creation time in system time, added by Eiffel Intelligence.
        "created" : 1542117412833,

        "notificationMessageKeyValuesAuth" : [],
        
        // If any authentication is needed by Eiffel Intelligence to send 
        // the notification HTTP request. 
        // This authentication type is "Jenkins CSRF Protection (crumb)", 
        // in which the username and password will be Base 64 encoded. 
        // A crumb will be fetched automatically before the HTTP request 
        // is made. (Currently default in many Jenkins instances). 
        // Even if the Jenkins instance has CSRF disabled this authentication 
        // type works. BASIC_AUTH can also be used.
        "authenticationType" : "BASIC_AUTH_JENKINS_CSRF",

        // The username and password Eiffel Intelligence will use in headers 
        // of the HTTP request when sending a notification via HTTP POST.
        "userName" : "functionalUser",
        "password" : "functionalUserPassword",

        // How to notify when a subscription is fulfilled.
        "notificationType" : "REST_POST",

        // Which url to use for the HTTP POST request. This url contains 
        // parameters to trigger a specific job. 
        "notificationMeta" : "http://eiffel-jenkins1:8080/job/ei-artifact-triggered-job/buildWithParameters?token='TOKEN'&object=id",

        // Headers for the HTTP request, can be 'application/x-www-form-urlencoded' or 'application/json'.
        "restPostBodyMediaType" : "application/json",

        // The data to send with the HTTP POST request. Jenkins ignores 
        // any body sent so we leave it empty in this subscription.
        "notificationMessageKeyValues" : [
            {
            }
        ],

        // An array of one or several requirements. At least one requirement 
        // should be fulfilled to trigger this subscription. A requirement 
        // can have several conditions.
        "requirements" : [
            {
                // Array of conditions. The key in the condition object must 
                // be "jmespath". The value can be any JMESPATH expression to 
                // extract data from the aggregated object. 
                // All conditions needs to be fulfilled in order for
                // a requirement to be fulfilled.

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

The subscription below triggers the same parameterized Jenkins job but we 
now use the build endpoint and we send the parameters in a json form using REST body.

    {
        // The name of the subscription to make it easy to search for.
        // Only numbers, letters and underscore allowed.
        "subscriptionName" : "Subscription1",

        // The name of the logged in user creating or updating the subscription
        // added by Eiffel Intelligence if LDAP is enabled. Not required. Defaults to an empty string.
        "ldapUserName" : "ABC",

        // Instructs whether same subscription should be re-triggered for new additions
        // to the aggregated object. If false only first time the conditions are fulfilled
        // a notification will be triggered. No matter how many times the aggregated object
        // is updated.
        "repeat" : false,

        // Creation time in system time, added by Eiffel Intelligence.
        "created" : 1542117412833,

        // If any authentication is needed by Eiffel Intelligence to send 
        // the notification HTTP request. 
        // This authentication type is "Jenkins CSRF Protection (crumb)", 
        // in which the username and password will be Base 64 encoded. 
        // A crumb will be fetched automatically before the HTTP request 
        // is made. (Currently default in many Jenkins instances). 
        // Even if the Jenkins instance has CSRF disabled this authentication 
        // type works. BASIC_AUTH can also be used.
        "authenticationType" : "BASIC_AUTH_JENKINS_CSRF",

        // How to notify when a subscription is fulfilled.
        "notificationType" : "REST_POST",

        // The username and password Eiffel Intelligence will use in headers 
        // of the HTTP request when sending a notification via HTTP POST.
        "userName" : "functionalUser",
        "password" : "functionalUserPassword",

        // Which url to use for the HTTP POST request.
        "notificationMeta" : "http://eiffel-jenkins1:8080/job/ei-artifact-triggered-job/build?token='TOKEN'",

        // Headers for the HTTP request, can be 'application/x-www-form-urlencoded' or 'application/json'.
        "restPostBodyMediaType" : "application/json",

        // The data to send with the HTTP POST request.
        "notificationMessageKeyValues" : [
            {
                // The form value will be run through JMESPATH engine so
                // it is possible to use JMESPATH expressions to extract
                // content from the aggregated object.

                "formkey" : "json",
                "formvalue" : "{parameter: [{ name: 'object', value : to_string(@) }]}"
            }
        ],

        // An array of one or several requirements. At least one requirement 
        // should be fulfilled to trigger this subscription. A requirement 
        // can have several conditions.
        "requirements" : [
            {
                // Array of conditions. The key in the condition object must 
                // be "jmespath". The value can be any JMESPATH expression to 
                // extract data from the aggregated object. 
                // All conditions needs to be fulfilled in order for
                // a requirement to be fulfilled.

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

## Requirements and conditions

Read more on how Eiffel Intelligence groups [requirements and conditions in subscriptions](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/subscriptions.md#writing-requirements-and-conditions).

