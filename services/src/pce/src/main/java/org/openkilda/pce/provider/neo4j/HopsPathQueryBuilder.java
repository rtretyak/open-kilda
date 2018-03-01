package org.openkilda.pce.provider.neo4j;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.StringJoiner;

class HopsPathQueryBuilder extends AbstractPathQueryBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(HopsPathQueryBuilder.class);

    HopsPathQueryBuilder(Driver driver) {
        super(driver);
    }

    @Override
    protected Statement buildNeo4jStatement() {
        HashMap<String, Value> parameters = new HashMap<>();

        String subject =
                "MATCH (a:switch{name:{src_switch}}),(b:switch{name:{dst_switch}}), " +
                        "p = shortestPath((a)-[r:isl*..100]->(b))";
        parameters.put("src_switch", Values.value(srcSwitch));
        parameters.put("dst_switch", Values.value(destSwitch));

        StringJoiner where = new StringJoiner("\n    AND ", "where ", "");
        where.add("ALL(x in nodes(p) WHERE x.state = 'active')");

        if (bandwidth == null) {
            where.add("ALL(y in r WHERE y.status = 'active')");
        } else {
            where.add("ALL(y in r WHERE y.status = 'active' AND y.available_bandwidth >= {bandwidth})");
            parameters.put("bandwidth", Values.value(bandwidth));
        }

        if (avoidIslPorts) {
            //TODO: apply condition on ISL ports.
        }

        String query = String.join("\n", subject, where.toString(), "RETURN p");
        return new Statement(query, Values.value(parameters));
    }
}
