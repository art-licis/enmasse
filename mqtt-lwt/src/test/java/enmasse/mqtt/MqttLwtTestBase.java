/*
 * Copyright 2016 Red Hat Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package enmasse.mqtt;

import enmasse.mqtt.storage.LwtStorage;
import enmasse.mqtt.storage.impl.InMemoryLwtStorage;
import io.enmasse.amqp.DispatchRouterJ;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for unit tests
 */
public class MqttLwtTestBase {

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    protected Vertx vertx;
    protected MqttLwt lwtService;
    protected DispatchRouterJ dispatchRouterJ;
    protected LwtStorage lwtStorage;

    public static final String MESSAGING_SERVICE_HOST = "localhost";

    public static final String CERT_DIR = "src/test/resources/client-certs";

    public static final String LWT_SERVICE_ENDPOINT = "$lwt";

    /**
     * Setup the MQTT LWT test base
     *
     * @param context   test context
     * @param deploy    deploy the MQTT LWT verticle
     */
    protected void setup(TestContext context, boolean deploy) {

        // if not set before, using in memory LWT storage as default
        if (this.lwtStorage == null) {
            this.lwtStorage = new InMemoryLwtStorage();
        }

        this.vertx = Vertx.vertx();

        this.dispatchRouterJ = new DispatchRouterJ("src/test/resources/router-certs");
        this.dispatchRouterJ.addLinkRoute("$lwt", "lwt-service");
        this.deployVerticle(this.dispatchRouterJ, context);

        // create and setup MQTT LWT instance
        this.lwtService = new MqttLwt()
                .setHost(MESSAGING_SERVICE_HOST)
                .setNormalPort(dispatchRouterJ.getNormalTlsPort())
                .setRouteContainerPort(dispatchRouterJ.getRouteContainerPort())
                .setCertDir(CERT_DIR)
                .setLwtStorage(this.lwtStorage);

        if (deploy) {
            this.deploy(context);
        }
    }

    protected void deployVerticle(Verticle verticle, TestContext context) {


        Async async = context.async();

        // start and deploy components
        this.vertx.deployVerticle(verticle,
                context.asyncAssertSuccess(v -> async.complete()));

        async.awaitSuccess();
    }

    /**
     * Deploy the MQTT LWT instance
     *
     * @param context   test context
     */
    protected void deploy(TestContext context) {

        deployVerticle(this.lwtService, context);

    }

    /**
     * Teardown the MQTT LWT test base
     *
     * @param context   test context
     */
    protected void tearDown(TestContext context) {

        Async async = context.async();

        this.vertx.close(context.asyncAssertSuccess(v -> async.complete()));

        async.awaitSuccess();
    }
}
