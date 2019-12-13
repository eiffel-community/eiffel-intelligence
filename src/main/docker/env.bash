#!/usr/bin/env bash

export MONGODB_PORT=27017
export RABBITMQ_AMQP_PORT=5672
export RABBITMQ_WEB_PORT=15672
export EIFFEL_ER_PORT=8084
export JENKINS_PORT=8082
export MAIL_SMTP_PORT=1025
export MAIL_WEB_PORT=8025
export EI_BACKEND_PORT=8090
export ELASTICSEARCH_PORT1=8026
export ELASTICSEARCH_PORT2=8027
export KIBANA_PORT=8085
export MONSTACHE_PORT=8086


export MONGODB_IMAGE="mongo:latest"
export RABBITMQ_IMAGE="bitnami/rabbitmq:3.8-debian-9"
export EIFFEL_ER_IMAGE="eiffelericsson/eiffel-er:0.0.67"
export JENKINS_IMAGE="bitnami/jenkins:2.138.3"
export MAILSERVER_IMAGE="mailhog/mailhog"
export EI_BACKEND_IMAGE="eiffelericsson/eiffel-intelligence-backend:1.0.1"
export ELASTICSEARCH_IMAGE="docker.elastic.co/elasticsearch/elasticsearch:6.2.4"
export KIBANA_IMAGE="docker.elastic.co/kibana/kibana:6.2.4"
export MONSTACHE_IMAGE="rwynn/monstache:4.15.1"
