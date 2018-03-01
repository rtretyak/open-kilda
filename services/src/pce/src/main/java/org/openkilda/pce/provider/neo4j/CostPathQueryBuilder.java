package org.openkilda.pce.provider.neo4j;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.StringJoiner;

class CostPathQueryBuilder extends AbstractPathQueryBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CostPathQueryBuilder.class);

    CostPathQueryBuilder(Driver driver) {
        super(driver);
    }

    @Override
    protected Statement buildNeo4jStatement() {
        HashMap<String, Value> parameters = new HashMap<>();

        String subject =
                "MATCH (from:switch{name:{src_switch}}),(to:switch{name:{dst_switch}}), " +
// (crimi) - unclear how to filter the relationships dijkstra looks out based on properties.
// Probably need to re-write the function and add it to our neo4j container.
//
//                        " CALL apoc.algo.dijkstraWithDefaultWeight(from, to, 'isl', 'cost', 700)" +
//                        " YIELD path AS p, weight AS weight "
                        " p = allShortestPaths((from)-[r:isl*..100]->(to)) " +
                        " WITH REDUCE(cost = 0, rel in rels(p) | " +
                        "   cost + CASE rel.cost WHEN rel.cost = 0 THEN 700 ELSE rel.cost END) AS cost, p ";
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

        String query = String.join("\n", subject, where.toString(), "RETURN p ORDER BY cost LIMIT 1");
        return new Statement(query, Values.value(parameters));
    }
}

