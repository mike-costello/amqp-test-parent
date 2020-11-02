# AMQP-TEST-PARENT

This repo constitutes a test harness for executing tests across an AMQP graph. The bits and bytes are 
instrumented as an executable fat jar containing a vertx based implementation of an AMQP receiver and 
emitter. 

This test is instrumented via k8s jobs, and relies on base Red Hat jdk11 images. 
