# AMQP-TEST-PARENT

This repo constitutes a test harness for executing tests across an AMQP graph. The bits and bytes are 
instrumented as an executable fat jar containing a vertx based implementation of an AMQP receiver and 
emitter. 

This test is instrumented via k8s jobs, and relies on base Red Hat jdk11 images. 

To run this test in a meaningful way, commands may be issued like: 

```
for i in {0..100..10}; do  oc process -f ./amqp-test-receiver/openshift/amqp-test-receiver-template.yaml -p OFFSET=${i} -p NUM_ADDRESSES=100 -p LOAD_TEST_DURATION=600 -p NUM_LOAD_TEST_RUNS=1 -p NUM_REPLICAS=1 | oc create -f - ; done
for i in {0..100..10}; do  oc process -f ./amqp-test-sender/openshift/amqp-test-sender-template.yaml -p OFFSET=${i} -p NUM_ADDRESSES=10 -p LOAD_TEST_DURATION=600 -p NUM_LOAD_TEST_RUNS=1 -p NUM_REPLICAS=1 | oc create -f - ; done
```

