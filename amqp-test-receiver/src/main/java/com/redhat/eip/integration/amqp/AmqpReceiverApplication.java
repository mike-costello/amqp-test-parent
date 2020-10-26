package com.redhat.eip.integration.amqp;



import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class AmqpReceiverApplication {

	private static final Logger log = LoggerFactory.getLogger(AmqpReceiverApplication.class); 
	
	public static void main(String[] args) throws Exception {
		Vertx vertx = Vertx.factory.vertx();
		vertx.deployVerticle(new AmqpReceiverVerticle(), stringAsyncResult -> {
			if (stringAsyncResult.succeeded()) {
				System.out.println("AmqpReceiverVerticle deployment complete");
			}else {
				System.out.println("AMQPReceiverVerticale deployment failed");
			}
			
		});

	}

}
