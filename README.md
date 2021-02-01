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

[![Sandbox badge](https://img.shields.io/badge/Stage-Sandbox-yellow)](https://github.com/eiffel-community/community/blob/master/PROJECT_LIFECYCLE.md#stage-sandbox)
[![Build Status](https://travis-ci.org/eiffel-community/eiffel-intelligence.svg?branch=master)](https://travis-ci.org/eiffel-community/eiffel-intelligence)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/8600a861b2aa4770901d12a45ace3535)](https://www.codacy.com/app/eiffel-intelligence-maintainers/eiffel-intelligence?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=eiffel-community/eiffel-intelligence&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/8600a861b2aa4770901d12a45ace3535)](https://www.codacy.com/app/eiffel-intelligence-maintainers/eiffel-intelligence?utm_source=github.com&utm_medium=referral&utm_content=eiffel-community/eiffel-intelligence&utm_campaign=Badge_Coverage)
[![](https://jitpack.io/v/eiffel-community/eiffel-intelligence.svg)](https://jitpack.io/#eiffel-community/eiffel-intelligence)

# Eiffel Intelligence
Eiffel Intelligence is a real time data aggregation and analysis solution
for Eiffel events. While Eiffel represents an event based architecture,
Eiffel Intelligence addresses the need of stakeholders to view the current
state of the system by bridging the divide from immutable events to mutable
state representation. More information can be found in the below documentation links.

Eiffel Intelligence consists of two components: the Eiffel Intelligence
back-end (this repository) and the [Eiffel Intelligence front-end](https://github.com/eiffel-community/eiffel-intelligence-frontend),
which is a graphical user interface.

## About this repository
The contents of this repository are licensed under the [Apache License 2.0](./LICENSE).

To get involved, please see [Code of Conduct](https://github.com/eiffel-community/.github/blob/master/CODE_OF_CONDUCT.md) and [contribution guidelines](https://github.com/eiffel-community/.github/blob/master/CONTRIBUTING.md).

## About Eiffel
This repository forms part of the Eiffel Community. Eiffel is a protocol for technology agnostic machine-to-machine communication in continuous integration and delivery pipelines, aimed at securing scalability, flexibility and traceability. Eiffel is based on the concept of decentralized real time messaging, both to drive the continuous integration and delivery system and to document it.

Visit [Eiffel Community](https://eiffel-community.github.io) to get started and get involved.

## Documentation

1. [**What is Eiffel Intelligence?**](wiki/index.md)
2. [**Running Eiffel Intelligence**](wiki/running-eiffel-intelligence.md)
   - [**Prerequisites**](wiki/running-eiffel-intelligence.md#Prerequisites)
   - [**Configuration**](wiki/configuration.md)
   - [**Run in Docker**](wiki/docker.md)
3. [**Understanding subscriptions**](wiki/subscriptions.md)
   - [**REST POST notification**](wiki/subscription-with-REST-POST-notification.md)
   - [**HTTP POST notification to trigger parameterized Jenkins jobs**](wiki/triggering-jenkins-jobs.md)
   - [**Email notification**](wiki/subscription-with-email-notification.md)
   - [**Step by Step Subscription Notification**](wiki/step-by-step-subscription-notification.md)
4. [**Understanding rules**](wiki/rules.md)
   - [**What is JMESPath?**](wiki/rules.md#What-is-JMESPath?)
   - [**Rule set up**](wiki/rules.md#Rule-set-up)
   - [**MergeResolverRules**](wiki/merge-resolver-rules.md)
   - [**History Rules**](wiki/history-rules.md)
   - [**Example rules**](wiki/example-rules.md)
   - [**Mapping Rules To Aggregations**](wiki/mapping-rules-to-aggregations.md)
5. [**Step by Step Aggregation on Eiffel events**](wiki/step-by-step-aggregation.md)
   - [**ArtifactCreatedEvent Aggregation**](wiki/artifact-created-event-aggregation.md)
   - [**TestCaseTriggeredEvent Aggregation**](wiki/test-case-triggered-event-aggregation.md)
   - [**TestCaseStartedEvent Aggregation**](wiki/test-case-started-event-aggregation.md)
   - [**TestCaseFinishedEvent Aggregation**](wiki/test-case-finished-event-aggregation.md)
   - [**ArtifactPublishedEvent Aggregation**](wiki/artifact-published-event-aggregation.md)
   - [**ConfidenceLevelModifiedEvent Aggregation**](wiki/confidence-level-modified-event-aggregation.md)
6. [**REST API**](wiki/REST-API.md)
7. [**Compatibility**](wiki/compatibility.md)
8. [**Known limitations**](wiki/known-limitations.md)
