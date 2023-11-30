package com.github.jdussouillez.server;

import io.quarkiverse.jooq.runtime.JooqCustomContext;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactoryOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jooq.Configuration;

@ApplicationScoped
public class JooqCustomContextFactory {

    @ConfigProperty(name = "quarkus.datasource.reactive.url")
    protected String datasourceReactiveUrl;

    @ConfigProperty(name = "quarkus.datasource.username")
    protected String datasourceUsername;

    @ConfigProperty(name = "quarkus.datasource.password")
    protected String datasourcePassword;

    @ApplicationScoped
    @Named("JooqCustomContext")
    public JooqCustomContext create() {
        return new JooqCustomContext() {
            @Override
            public void apply(final Configuration configuration) {
                var connectionFactory = ConnectionFactories.get(
                    ConnectionFactoryOptions
                        .parse(datasourceReactiveUrl)
                        .mutate()
                        .option(ConnectionFactoryOptions.USER, datasourceUsername)
                        .option(ConnectionFactoryOptions.PASSWORD, datasourcePassword)
                        .build()
                );
                var poolConfig = ConnectionPoolConfiguration.builder(connectionFactory)
                    .build();
                configuration.set(new ConnectionPool(poolConfig));
            }
        };
    }
}
