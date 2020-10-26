package com.redhat.eip.integration.amqp;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class AmqpSenderApplication {

	private static final Logger log = LoggerFactory.getLogger(AmqpSenderApplication.class); 
	
	public static void main(String[] args) {

		Vertx vertx = Vertx.factory.vertx();
		vertx.deployVerticle(new AmqpSenderVerticle(), stringAsyncResult -> {
			if (stringAsyncResult.succeeded()) {
				System.out.println("AmqpSenderVerticle deployment complete");
			}else {
				System.out.println("AMQPSenderVerticle deployment failed");
				log.error(stringAsyncResult.cause());
				log.error(stringAsyncResult.toString());
				
			}
			
		});
	}

}
