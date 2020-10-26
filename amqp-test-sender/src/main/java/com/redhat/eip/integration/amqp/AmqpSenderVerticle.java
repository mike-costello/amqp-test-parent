package com.redhat.eip.integration.amqp;

import java.util.stream.IntStream;

import io.vertx.amqp.AmqpClient;
import io.vertx.amqp.AmqpClientOptions;
import io.vertx.amqp.AmqpConnection;
import io.vertx.amqp.AmqpMessage;
import io.vertx.amqp.AmqpSender;
import io.vertx.amqp.AmqpSenderOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class AmqpSenderVerticle extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger(AmqpSenderVerticle.class);

	private AmqpClient amqpClient;
	private AmqpSender amqpSender;
	private AmqpConnection amqpConnection;

	@Override
	public void start(Promise<Void> startPromise) throws Exception {

		/**
		 * @author mcostell FIXME mcostell this needs to be configurable
		 */
		AmqpClientOptions options = new AmqpClientOptions().setHost("localhost").setPort(5672).setUsername("user")
				.setPassword("secret");

		amqpClient = AmqpClient.create(vertx, options);
		/**
		 * @author mcostell FIXME do something more robust to send messages
		 */
		AmqpSenderOptions amqpOptions = new AmqpSenderOptions();
		log.info("calling create sender");
		/**
		 * @author mcostell FIXME this likely needs to be something like a list of
		 *         addresses as we plan to have k8s jobs run multiple replicas for now
		 *         let's just hard code a value
		 */
		IntStream.range(0, 3).forEach(j -> {
			vertx.executeBlocking(senderPromise -> amqpClient.createSender("test", amqpOptions, done -> {
				if (done.failed()) {
					log.error("sender create failed for test-queue");
					startPromise.fail("unable to create a sender");
				} else {
					log.info("created amqp sender for address test-queue");
					amqpSender = done.result();
					/**
					 * @author mcostell FIXME this likely needs to do something more meaningful in
					 *         regards to message payload additionally this likely needs to be
					 *         configured differently
					 */
					IntStream.range(0, 1000).forEach( i -> {
						log.debug("remaining credits " + amqpSender.remainingCredits());
						amqpSender.send(AmqpMessage.create().withBody("test " + i).build());
						log.info("message sent " + i);
						
					});

				}
				senderPromise.complete();
			}), res -> {
				log.info(res.result());

			});
		});
		startPromise.complete();

	}

	/**
	 * @author mcostell clean up relevant resources in case of a graceful shutdown
	 */
	@Override
	public void stop() throws Exception {
		if (amqpSender != null) {
			amqpSender.close(null);
			amqpSender = null;
		} else {
			amqpSender = null;
		}
	}
}
