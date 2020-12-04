package com.redhat.eip.integration.amqp;

import java.util.stream.IntStream;

import io.vertx.amqp.AmqpClient;
import io.vertx.amqp.AmqpClientOptions;
import io.vertx.amqp.AmqpReceiver;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class AmqpReceiverVerticle extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger(AmqpReceiverVerticle.class);

	private ConfigRetriever configRetriever;
	private AmqpClient amqpClient;
	private AmqpReceiver amqpReceiver;
	private AmqpClientOptions options;
	private JsonObject config;
	
	private String seedAddress;

	public AmqpReceiverVerticle() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void start(Promise<Void> promise) throws Exception {

		ConfigStoreOptions configStoreOptions = new ConfigStoreOptions();
		/**
		 * @author mcostell
		 * let's configure an environment variable based configuration and ensure conversion of data is string based via "raw-data" attribute. 
		 * FIXME investigate whether to have this loaded via different configuration stores as configretriever may be overloaded, i.e. configmap, or file 
		 */
		/*configStoreOptions.setFormat("properties").setType("file")
		 *		.setConfig(new JsonObject().put("path", "application.properties"));
		*/
		configStoreOptions.setType("env").setConfig(new JsonObject().put("raw-data", true));
		
		vertx.executeBlocking(confPromise -> {
			log.info("loading configuration");
			configRetriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(configStoreOptions));

			configRetriever.getConfig(ar -> {
				if (ar.failed()) {
					log.error("unable to retrieve config");
					confPromise.fail(ar.cause());
				} else {
					config = ar.result();
					log.debug("***host is " + config.getString("host"));
					options = new AmqpClientOptions().setHost(config.getString("host", "localhost"))
							.setPort(Integer.valueOf(config.getString("port", "5672"))).setUsername(config.getString("username", "user"))
							.setPassword(config.getString("secret"));
					amqpClient = AmqpClient.create(vertx, options);
					seedAddress = config.getString("seedAddress", "test");
					//IntStream.range(Integer.valueOf(config.getString("offset", "0")), Integer.valueOf(config.getString("numAddresses", "100")))
					//.forEach(i -> {
						log.info("Creating receiver for address " + seedAddress + "." );//+ i);
						
						amqpClient.createReceiver("test::test.121", done -> {
							if (done.failed()) {
								log.error("failed to create message receiver for address " + seedAddress + "." );//+ i);
								log.error(done.cause());
								confPromise.fail("failed to create message receiver");

							} else {
								log.info("successfully created receiver" + seedAddress + "." );//+ i);
								amqpReceiver = done.result();
								amqpReceiver.handler(msg -> {
									log.info("received message " + msg.bodyAsString());
									msg.accepted();
								});
							}

						});
					//});

				}
			});
			confPromise.complete();
		}, Boolean.TRUE, res -> {
			if (res.succeeded()) {
				log.info("loaded config");
				log.info("result " + res.result());
			} else {
				log.error("***ERROR: AMQP Client Did Not Start***");
				log.error(res.cause());

			}
		});
	}

}
