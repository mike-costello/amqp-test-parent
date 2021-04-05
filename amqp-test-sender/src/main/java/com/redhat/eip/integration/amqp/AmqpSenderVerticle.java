package com.redhat.eip.integration.amqp;

import java.util.stream.IntStream;

import io.vertx.amqp.AmqpClient;
import io.vertx.amqp.AmqpClientOptions;
import io.vertx.amqp.AmqpConnection;
import io.vertx.amqp.AmqpMessage;
import io.vertx.amqp.AmqpSender;
import io.vertx.amqp.AmqpSenderOptions;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class AmqpSenderVerticle extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger(AmqpSenderVerticle.class);

	private AmqpClient amqpClient;
	private AmqpSender amqpSender;
	private AmqpConnection amqpConnection;
	private JsonObject config;

	private String seedAddress;

	private ConfigRetriever configRetriever;

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		
		ConfigStoreOptions configStoreOptions = new ConfigStoreOptions();
		/**
		 * @author mcostell
		 * let's configure an environment variable based configuration and ensure conversion of data is string based via "raw-data" attribute. 
		 * FIXME investigate whether to have this loaded via different configuration stores as configretriever may be overloaded, i.e. configmap, or file 
		 */
		configStoreOptions.setType("env").setConfig(new JsonObject().put("raw-data", true));
		configRetriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(configStoreOptions));
		configRetriever.getConfig(ar -> {
			if (ar.failed()) {
				log.error("unable to retrieve config");
				startPromise.fail(ar.cause());
			} else {
				config = ar.result();
			}
		});
		/**
		 * @author mcostell FIXME mcostell this needs to be configurable
		 */
		AmqpClientOptions options = new AmqpClientOptions().setHost(config.getString("host", "localhost"))
				.setPort(Integer.valueOf(config.getString("port", "5672")))
				.setUsername(config.getString("user","guest"))
				.setPassword(config.getString("password","test"));

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
		seedAddress = config.getString("seedAddress", "test");
		
		IntStream.range(Integer.valueOf(config.getString("offset","0")),
				Integer.valueOf(config.getString("offset","0")) + Integer.valueOf(config.getString("numAddresses","100"))).forEach(j -> {
			vertx.executeBlocking(senderPromise -> amqpClient.createSender(new StringBuilder().append(seedAddress).append(j).toString(), 
					amqpOptions, done -> {
				if (done.failed()) {
					log.error("sender create failed for queue: " + seedAddress + "" + j);
					//startPromise.fail("unable to create a sender");
					log.error(done.cause());
				} else {
					log.info("created amqp sender for address " + seedAddress + "" + j);
					amqpSender = done.result();
					/**
					 * @author mcostell FIXME this likely needs to do something more meaningful in
					 *         regards to message payload additionally this likely needs to be
					 *         configured differently
					 */
					IntStream.range(0, Integer.valueOf(config.getString("numMessages", "100"))).forEach( i -> {
						log.debug("remaining credits " + amqpSender.remainingCredits());
						amqpSender.send(AmqpMessage.create().withBody("testme.crazy." + i).build());
						log.info("message sent " + i + " to address " + seedAddress + j);
						
					});

				}
				senderPromise.complete();
			}), res -> {
				log.info("completed sender");

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
