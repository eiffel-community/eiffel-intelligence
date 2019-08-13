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
Eiffel Intelligence is a real time data aggregation and analysis solution
for Eiffel events. While Eiffel represents an event based architecture,
Eiffel Intelligence addresses the need of stakeholders to view the current
state of the system by bridging the divide from immutable events to mutable
state representation.

Eiffel Intelligence consists of two components: the Eiffel Intelligence
back-end (this repository) and the [Eiffel Intelligence front-end](https://github.com/eiffel-community/eiffel-intelligence-frontend),
which is a graphical user interface.

## What is Eiffel Intelligence?
More information about what Eiffel Intelligence is and what it can be used for [can be read here](https://github.com/eiffel-community/eiffel-intelligence/blob/master/wiki/markdown/index.md).

# About this repository
The contents of this repository are licensed under the [Apache License 2.0](./LICENSE).

To get involved, please see [Code of Conduct](./CODE_OF_CONDUCT.md) and [contribution guidelines](./CONTRIBUTING.md).

# About Eiffel
This repository forms part of the Eiffel Community. Eiffel is a protocol for technology agnostic machine-to-machine communication in continuous integration and delivery pipelines, aimed at securing scalability, flexibility and traceability. Eiffel is based on the concept of decentralized real time messaging, both to drive the continuous integration and delivery system and to document it.

Visit [Eiffel Community](https://eiffel-community.github.io) to get started and get involved.

# Documentation

1. [**Running Eiffel Intelligence**](wiki/markdown/running-eiffel-intelligence.md)
    - [**Prerequisites**](wiki/markdown/running-eiffel-intelligence.md#Prerequisites)
    - [**Configuration**](wiki/markdown/configuration.md)
    - [**Run in Docker**](wiki/markdown/docker.md)
1. [**Understanding subscriptions**](wiki/markdown/subscriptions.md)
    - [**REST POST notification**](wiki/markdown/subscription-with-REST-POST-notification.md)
    - [**HTTP POST notification to trigger parameterized Jenkins jobs**](wiki/markdown/triggering-jenkins-jobs.md)
    - [**Email notification**](wiki/markdown/subscription-with-email-notification.md)
    - [**Step by Step Subscription Notification**](wiki/markdown/step-by-step-subscription-notification.md)
1. [**Understanding rules**](wiki/markdown/rules.md)
    - [**What is JMESPath?**](wiki/markdown/rules.md#What-is-JMESPath?)
    - [**Rule set up**](wiki/markdown/rules.md#Rule-set-up)
    - [**MergeResolverRules**](wiki/markdown/merge-resolver-rules.md)
    - [**History Rules**](wiki/markdown/history-rules.md)
    - [**Existing rules files**](wiki/markdown/existing-rules-files.md)
    - [**Mapping Rules To Aggregations**](wiki/markdown/mapping-rules-to-aggregations.md)
1. [**Step by Step Aggregation on Eiffel events**](wiki/markdown/step-by-step-aggregation.md)
    - [**ArtifactCreatedEvent Aggregation**](wiki/markdown/artifact-created-event-aggregation.md)
    - [**TestCaseTriggeredEvent Aggregation**](wiki/markdown/test-case-triggered-event-aggregation.md)
    - [**TestCaseStartedEvent Aggregation**](wiki/markdown/test-case-started-event-aggregation.md)
    - [**TestCaseFinishedEvent Aggregation**](wiki/markdown/test-case-finished-event-aggregation.md)
    - [**ArtifactPublishedEvent Aggregation**](wiki/markdown/artifact-published-event-aggregation.md)
    - [**ConfidenceLevelModifiedEvent Aggregation**](wiki/markdown/confidence-level-modified-event-aggregation.md)
1. [**REST API**](wiki/markdown/REST-API.md)
    - [**Query aggregated objects**](wiki/markdown/query.md)
    - [**Running rules on objects**](wiki/markdown/running-rules-on-objects.md)
    - [**Authentication**](wiki/markdown/authentication.md)
    - [**Subscriptions**](wiki/markdown/subscription-API.md)
    - [**Download files**](wiki/markdown/download-files.md)
1. [**Compatibility**](wiki/markdown/compatibility.md)
1. [**Known limitations**](wiki/markdown/known-limitations.md)

**Eiffel Intelligence documentation** [**can be found here**](https://eiffel-community.github.io/eiffel-intelligence/)
