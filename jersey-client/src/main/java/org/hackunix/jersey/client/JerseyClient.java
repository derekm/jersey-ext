package org.hackunix.jersey.client;

import static org.eclipse.microprofile.config.ConfigProvider.getConfig;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Feature;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.glassfish.jersey.jsonb.JsonBindingFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.hackunix.jaxrs.context.jsonb.JsonbContextResolver;

public abstract class JerseyClient<T> {

    private static final Logger logger = Logger.getLogger(JerseyClient.class.getName());

    private Client client;
    private WebTarget target;
    private T root;

    protected Client getClient() {
        return client;
    }

    protected abstract Class<T> getRootClass();

    protected abstract String getConfigPrefix();

    protected Logger getLogger() {
        return logger;
    }

    protected WebTarget getTarget() {
        return target;
    }

    /**
     * Main entry point to the resources.
     * @return Root resource
     */
    public T root() {
        return root;
    }

    /**
     * Instantiate by loading configuration using MP config.
     */
    public JerseyClient() {
        initializeRoot(
                getConfig().getValue(getConfigName("baseUrl"), String.class),
                getConfig().getOptionalValue(getConfigName("user"), String.class)
                        .orElse(null),
                getConfig().getOptionalValue(getConfigName("password"), String.class)
                        .orElse(""),
                getConfig().getOptionalValue(getConfigName("logging.enabled"), Boolean.class)
                        .orElse(false)
        );
    }

    /**
     * Instantiate by loading from parameters.
     * @param baseUrl Base URL of the service to hit
     * @param user User to use (null for no authentication)
     * @param password Password to use
     * @param loggingEnabled Whether to enable request/response logging at INFO level
     */
    public JerseyClient(String baseUrl, String user, String password, boolean loggingEnabled) {
        initializeRoot(baseUrl, user, password, loggingEnabled);
    }

    private String getConfigName(String suffix) {
        return getConfigPrefix() + "." + suffix;
    }

    private void initializeRoot(String baseUrl, String user, String password,
                                boolean loggingEnabled) {
        client = constructClient();

        if (user != null) {
            client.register(HttpAuthenticationFeature.basic(user, password));
        }

        if (loggingEnabled) {
            Feature feature = new LoggingFeature(getLogger(), Level.INFO,
                    LoggingFeature.Verbosity.PAYLOAD_ANY, 30000);
            client.register(feature);
        }

        target = client.target(baseUrl);
        root = WebResourceFactory.newResource(getRootClass(), target);
    }

    /**
     * Constructs JAX-RS client with appropriate serialization features.
     * @return JAX-RS client
     */
    public static Client constructClient() {
        return ClientBuilder.newBuilder()
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .property(ClientProperties.FOLLOW_REDIRECTS, true)
                .register(JsonbContextResolver.class)
                .register(JsonBindingFeature.class)
                .register(MoxyJsonFeature.class)
                .build();
    }

}
