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
        "subscriptionName" : "Subscription1",

        // the name of the user who created the subscription
        // defaults to an empty string if LDAP is disabled
        "ldapUserName" : "ABC",

        // instructs whether same subscription should be re-triggered for new additions
        // to the aggregated object. If false only first time the conditions are fulfilled
        // a notification will be triggered. No matter how many times the aggregated object
        // is updated.
        "repeat" : false,
        "notificationMessageKeyValuesAuth" : [],
        "created" : 1542117412833,

        // how to notify when a subscription is triggered
        "notificationType" : "REST_POST",
        "authenticationType" : "BASIC_AUTH",

        // the username and password to insert in headers of the POST request when sending
        // a notification via REST POST
        "userName" : "functionalUser",
        "password" : "functionalUserPassword",

        // which url to use for the HTTP POST request
        "notificationMeta" : "http://eiffel-jenkins1:8080/job/ei-artifact-triggered-job/buildWithParameters?token='TOKEN'&object=id",

        // headers for the HTTP request, can be 'application/x-www-form-urlencoded' or 'application/json'
        "restPostBodyMediaType" : "application/json",

        // the data to send with the HTTP POST request
        "notificationMessageKeyValues" : [
            {                
            }
        ],

        // An array of requirements. At least one requirement should be fulfilled to
        // trigger this subscription.
        "requirements" : [
            {
                // Array of conditions. Here we use JMESPATH condition based on content in
                // aggregated object. All conditions needs to be fulfilled in order for
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
   * the parameters should be specified in notificationMessageKeyValues. Also here not more parameters than the job is configured with. Your job will not be triggered otherwise.
   * no job parameters in the URL
    
    The subscription below triggers the same parameterized Jenkins job but we now use build endpoint and we send the parameter in a json form using REST body.
    
    {
        "subscriptionName" : "Subscription1",

        // the name of the user who created the subscription
        // defaults to an empty string if LDAP is disabled
        "ldapUserName" : "ABC",

        // instructs whether same subscription should be re-triggered for new additions
        // to the aggregated object. If false only first time the conditions are fulfilled
        // a notification will be triggered. No matter how many times the aggregated object
        // is updated.
        "repeat" : false,
        "notificationMessageKeyValuesAuth" : [],
        "created" : 1542117412833,

        // how to notify when a subscription is triggered
        "notificationType" : "REST_POST",
        "authenticationType" : "BASIC_AUTH",

        // the username and password to insert in headers of the POST request when sending
        // a notification via REST POST
        "userName" : "functionalUser",
        "password" : "functionalUserPassword",

        // which url to use for the HTTP POST request
        "notificationMeta" : "http://eiffel-jenkins1:8080/job/ei-artifact-triggered-job/build?token='TOKEN'",

        // headers for the HTTP request, can be 'application/x-www-form-urlencoded' or 'application/json'
        "restPostBodyMediaType" : "application/json",

        // the data to send with the HTTP POST request
        "notificationMessageKeyValues" : [
            {
                // form value will be run through JMESPATH engine to extract
                // content from aggregated object.

                "formkey" : "json",
                "formvalue" : "{parameter: [{ name: 'object', value : to_string(@) }]}"
            }
        ],

        // An array of requirements. At least one requirement should be fulfilled to
        // trigger this subscription.
        "requirements" : [
            {
                // Array of conditions. Here we use JMESPATH condition based on content in
                // aggregated object. All conditions needs to be fulfilled in order for
                // a requirement to be fulfilled.

                "conditions" : [
                    {
                        "jmespath" : "identity=='pkg:maven/com.othercompany.library/artifact-name@1.0.0'"
                    }
                ]
            }

        ]
    }
