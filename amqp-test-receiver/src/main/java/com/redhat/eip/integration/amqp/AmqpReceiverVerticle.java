package com.redhat.eip.integration.amqp;

import java.util.stream.IntStream;

import io.vertx.amqp.AmqpClient;
import io.vertx.amqp.AmqpClientOptions;
import io.vertx.amqp.AmqpConnection;
import io.vertx.amqp.AmqpReceiver;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class AmqpReceiverVerticle extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger(AmqpReceiverVerticle.class);

	private AmqpClient amqpClient;
	private AmqpReceiver amqpReceiver;
	private AmqpConnection amqpConnection;

	public AmqpReceiverVerticle() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void start(Promise<Void> promise) throws Exception {

		AmqpClientOptions options = new AmqpClientOptions().setHost("localhost").setPort(5672).setUsername("user")
				.setPassword("secret");

		amqpClient = AmqpClient.create(vertx, options);
		log.info("create amqp receiver");
		IntStream.range(0, 3).forEach(i -> {
			vertx.executeBlocking(receiverPromise -> amqpClient.createReceiver("test", done -> {
				if (done.failed()) {
					log.error("failed to create message receiver");
					promise.fail("failed to create message receiver");

				} else {
					log.info("successfully created receiver");
					amqpReceiver = done.result();
					amqpReceiver.handler(msg -> {
						log.info("received message " + msg.bodyAsString());
						msg.accepted();
					});
				}
				receiverPromise.complete();
			}), res -> {
				log.info("receiving messages");
			});
		});
	}

	@Override
	public void stop(Promise<Void> promise) throws Exception {
		/**
		 * @author mcostell FIXME implement this
		 */
	}

}
