<!---
   Copyright 2017-2018 Ericsson AB.
   For a full list of individual contributors, please see the commit history.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
--->

<img src="./images/eiffel-intelligence-logo.png" alt="Eiffel Intelligence" width="350"/>

[![Build Status](https://travis-ci.org/eiffel-community/eiffel-intelligence.svg?branch=master)](https://travis-ci.org/eiffel-community/eiffel-intelligence)
[![Coverage Status](https://coveralls.io/repos/github/eiffel-community/eiffel-intelligence/badge.svg?branch=master)](https://coveralls.io/github/eiffel-community/eiffel-intelligence?branch=master)
[![](https://jitpack.io/v/eiffel-community/eiffel-intelligence.svg)](https://jitpack.io/#eiffel-community/eiffel-intelligence)

# Eiffel Intelligence
Eiffel Intelligence is a real time data aggregation and analysis solution for Eiffel events. While Eiffel represents an event based architecture, Eiffel Intelligence addresses the need of stakeholders to view the current state of the system by bridging the divide from immutable events to mutable state representation. 

# About this repository
The contents of this repository are licensed under the [Apache License 2.0](./LICENSE).

To get involved, please see [Code of Conduct](./CODE_OF_CONDUCT.md) and [contribution guidelines](./CONTRIBUTING.md).

# About Eiffel
This repository forms part of the Eiffel Community. Eiffel is a protocol for technology agnostic machine-to-machine communication in continuous integration and delivery pipelines, aimed at securing scalability, flexibility and traceability. Eiffel is based on the concept of decentralized real time messaging, both to drive the continuous integration and delivery system and to document it.

Visit [Eiffel Community](https://eiffel-community.github.io) to get started and get involved.

# Documentation

1. [**Configuration**](./wiki/Configuration.md)
1. [**Running Eiffel Intelligence**](./wiki/Running-Eiffel-Intelligence.md)
    - [**Prerequisites**](./wiki/Running-Eiffel-Intelligence.md#Prerequisites)
1. [**REST API**](./wiki/REST-API.md)
    - [**Query aggregated objects**](./wiki/Query.md)
        - [**Perform query on created aggregated object**](./wiki/Query.md#Perform-query-on-created-aggregated-object)
        - [**Perform freestyle query on created aggregated object**](./wiki/Query.md#Perform-freestyle-query-on-created-aggregated-object)
        - [**Example of freestyle query that returns all aggregated objects**](./wiki/Query.md#Example-of-freestyle-query-that-returns-all-aggregated-objects)
        - [**Query an aggregated object and filter it with specific key**](./wiki/Query.md#Query-an-aggregated-object-and-filter-it-with-specific-key)
        - [**Query missed notifications**](./wiki/Query.md#Query-missed-notifications)
    - [**Running rules on objects**](./wiki/Running-rules-on-objects.md)
        - [**Test JMESPath expression on given Event**](./wiki/Running-rules-on-objects.md#Test-JMESPath-expression-on-given-Event)
        - [**Test a list of rule sets on given list of events**](./wiki/Running-rules-on-objects.md#Test-a-list-of-rule-sets-on-given-list-of-events) 
    - [**Authentication**](./wiki/Authentication.md)
        - [**Check if security is enabled**](./wiki/Authentication.md#Check-if-security-is-enabled)
        - [**Login point that returns the name of current user**](./wiki/Authentication.md#Login-point-that-returns-the-name-of-current-user)
        - [**Delete session of current user**](./wiki/Authentication.md#Delete-session-of-current-user)
        - [**Check if backend is running**](./wiki/Authentication.md#Check-if-backend-is-running)
    - [**Subscriptions**](./wiki/Subscription-API.md)
        - [**Create subscriptions**](./wiki/Subscription-API.md#Create-subscriptions)
        - [**Get subscriptions for the given names**](./wiki/Subscription-API.md#Get-subscriptions-for-the-given-names)
        - [**Update subscriptions**](./wiki/Subscription-API.md#Update-subscriptions)
        - [**Delete subscriptions for the given names**](./wiki/Subscription-API.md#Delete-subscriptions-for-the-given-names)
        - [**Get all subscriptions**](./wiki/Subscription-API.md#Get-all-subscriptions)
    - [**Download files**](./wiki/Download-Files.md)
        - [**List available files**](./wiki/Download-Files.md#List-available-files)
        - [**Download subscription template file**](./wiki/Download-Files.md#Download-subscription-template-file)
        - [**Download rules template file**](./wiki/Download-Files.md#Download-rules-template-file)
        - [**Download events template file**](./wiki/Download-Files.md#Download-events-template-file)
1. [**Rules**](./wiki/Rules.md)
    - [**Introduction**](./wiki/Rules.md#Introduction)
    - [**Rule set up**](./wiki/Rules.md#Rule-set-up)
    - [**Existing rules files**](./wiki/Rules.md#Existing-rules-files)
    - [**MergeResolverRules**](./wiki/MergeResolverRules.md)
        - [**Example 1**](./wiki/MergeResolverRules.md#Example-1)   
        - [**Example 2 - array aggregations**](./wiki/MergeResolverRules.md#Example-2---array-aggregations)  
    - [**History Rules**](./wiki/History-rules.md)
1. [**Subscriptions**](./wiki/Subscriptions.md)
    - [**REST POST notification**](./wiki/REST-POST-notification.md)
    - [**Email notification**](./wiki/Email-notification.md)
1. [**Known limitations**](./wiki/Known-limitations.md)
1. [**Step by Step Aggregation**](./wiki/Step-by-Step-Aggregation.md)
    - [**ArtifactCreatedEvent Aggregation**](./wiki/ArtifactCreatedEvent-aggregation.md)
    - [**EiffelTestCaseTriggeredEvent Aggregation**](./wiki/EiffelTestCaseTriggeredEvent-Aggregation.md)
    - [**EiffelTestCaseStartedEvent Aggregation**](./wiki/EiffelTestCaseStartedEvent-aggregation.md)
    - [**EiffelTestCaseFinishedEvent Aggregation**](./wiki/EiffelTestCaseFinishedEvent-aggregation.md)
    - [**EiffelArtifactPublishedEvent Aggregation**](./wiki/EiffelArtifactPublishedEvent-Aggregation.md)
    - [**EiffelConfidenceLevelModifiedEvent Aggregation**](./wiki/EiffelConfidenceLevelModifiedEvent-Aggregation.md)
1. [**Step by Step Subscription Notification**](./wiki/Step-by-Step-Subscription-Notification.md)


