package org.hackunix.jersey.test;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.hackunix.jersey.client.JerseyClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Before;
import org.junit.runner.RunWith;

import ws.ament.hammock.test.support.EnableRandomWebServerPort;

/**
 * A test that instantiates a JAX-RS proxy client from a resource interface.
 * <p>
 * Usage:
 * <pre>
 * public class EndpointTest extends {@code JerseyProxyTest<ClientResource>} {
 *   public EndpointTest() {
 *     super(ClientResource.class);
 *   }
 *   ...
 * }
 * </pre>
 *
 * @author Derek Moore <a href="mailto:dpmoore@acm.org">dpmoore@acm.org</a>
 *
 * @param <T> resource interface of JAX-RS proxy client to instantiate
 */
@RunWith(Arquillian.class)
@EnableRandomWebServerPort
public abstract class JerseyProxyTest<T> {

    @ArquillianResource
    private URI uri;

    /**
     * The interface type of the proxy client to generate.
     */
    protected Class<T> proxyType;

    /**
     * The generated instance of the proxy client. Use this in your tests
     * to talk to your service endpoints.
     */
    protected T proxy;

    protected Client client;

    /**
     * Create a web server test with a proxy client.
     * @param proxyType resource interface type for self-generating client
     */
    public JerseyProxyTest(Class<T> proxyType) {
            this.proxyType = proxyType;

            client = JerseyClient.constructClient();
    }

    @Before
    public void setup() {
            proxy = WebResourceFactory.newResource(proxyType, target());
    }

    public WebTarget target() {
            return client.target(uri);
    }

}
