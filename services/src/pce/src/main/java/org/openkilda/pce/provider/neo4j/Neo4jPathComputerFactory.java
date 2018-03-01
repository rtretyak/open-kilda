package org.openkilda.pce.provider.neo4j;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.GraphDatabase;
import org.openkilda.pce.provider.PathComputerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Neo4jPathComputerFactory implements PathComputerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jPathComputerFactory.class);

    private final String host;
    private final String login;
    private final String password;

    public Neo4jPathComputerFactory(String host, String login, String password) {
        this.host = Objects.requireNonNull(host);
        this.login = Objects.requireNonNull(login);
        this.password = Objects.requireNonNull(password);
    }

    @Override
    public Neo4jPathComputer getPathComputer() {
        String address = String.format("bolt://%s", host);

        LOGGER.info("NEO4J connect {} (login=\"{}\", password=\"*****\")", address, login);
        return new Neo4jPathComputer(GraphDatabase.driver(address, AuthTokens.basic(login, password)));
    }
}
