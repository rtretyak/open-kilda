package org.openkilda.pce.provider.neo4j;

import static java.util.Collections.emptyList;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Relationship;
import org.openkilda.messaging.info.event.PathInfoData;
import org.openkilda.messaging.info.event.PathNode;
import org.openkilda.messaging.model.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

abstract class AbstractPathQueryBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPathQueryBuilder.class);

    private final Driver driver;

    protected String srcSwitch = null;
    protected String destSwitch = null;
    protected Integer bandwidth = null;
    protected boolean avoidIslPorts = false;
    protected boolean oneSwitchFlow = false;

    AbstractPathQueryBuilder(Driver driver) {
        this.driver = driver;
    }

    public AbstractPathQueryBuilder srcSwitch(String srcSwitch) {
        this.srcSwitch = srcSwitch;
        return this;
    }

    public AbstractPathQueryBuilder destSwitch(String destSwitch) {
        this.destSwitch = destSwitch;
        return this;
    }

    public AbstractPathQueryBuilder bandwidth(int bandwidth) {
        this.bandwidth = bandwidth;
        return this;
    }

    public AbstractPathQueryBuilder oneSwitchFlow() {
        this.oneSwitchFlow = true;
        return this;
    }

    public AbstractPathQueryBuilder avoidIslPorts() {
        this.avoidIslPorts = true;
        return this;
    }

    public PathQuery buildQuery() {
        if (oneSwitchFlow) {
            LOGGER.info("No path computation for one-switch flow");
            return buildEmptyPathQuery();
        }

        final Statement statement = buildNeo4jStatement();

        return new PathQueryImpl(statement);
    }

    private PathQuery buildEmptyPathQuery() {
        ImmutablePair<PathInfoData, PathInfoData> emptyPath =
                new ImmutablePair<>(new PathInfoData(0, emptyList()), new PathInfoData(0, emptyList()));

        return new PathQuery() {
            @Override
            public Optional<ImmutablePair<PathInfoData, PathInfoData>> executeForSingle() {
                return Optional.of(emptyPath);
            }

            @Override
            public List<ImmutablePair<PathInfoData, PathInfoData>> execute() {
                return Collections.singletonList(emptyPath);
            }
        };
    }

    protected abstract Statement buildNeo4jStatement();

    class PathQueryImpl implements PathQuery {

        private final Statement statement;

        PathQueryImpl(Statement statement) {
            this.statement = Objects.requireNonNull(statement);
        }

        public Optional<ImmutablePair<PathInfoData, PathInfoData>> executeForSingle() {
            LOGGER.debug("Executing the query: {}", statement);

            try (Session session = driver.session()) {
                StatementResult result = session.run(statement);
                return result.hasNext() ? Optional.of(convertFlowPair(result.next())) : Optional.empty();
            }
        }

        public List<ImmutablePair<PathInfoData, PathInfoData>> execute() {
            LOGGER.debug("Executing the query: {}", statement);

            try (Session session = driver.session()) {
                StatementResult result = session.run(statement);
                return result.list().stream().map(this::convertFlowPair).collect(Collectors.toList());
            }
        }

        private ImmutablePair<PathInfoData, PathInfoData> convertFlowPair(Record record) {
            long latency = 0L;
            List<PathNode> forwardNodes = new LinkedList<>();
            List<PathNode> reverseNodes = new LinkedList<>();

            LinkedList<Relationship> isls = new LinkedList<>();
            record.get(0).asPath().relationships().forEach(isls::add);

            int seqId = 0;
            for (Relationship isl : isls) {
                latency += isl.get("latency").asLong();

                forwardNodes.add(new PathNode(isl.get("src_switch").asString(),
                        isl.get("src_port").asInt(), seqId, isl.get("latency").asLong()));
                seqId++;

                forwardNodes.add(new PathNode(isl.get("dst_switch").asString(),
                        isl.get("dst_port").asInt(), seqId, 0L));
                seqId++;
            }

            seqId = 0;
            Collections.reverse(isls);

            for (Relationship isl : isls) {
                reverseNodes.add(new PathNode(isl.get("dst_switch").asString(),
                        isl.get("dst_port").asInt(), seqId, isl.get("latency").asLong()));
                seqId++;

                reverseNodes.add(new PathNode(isl.get("src_switch").asString(),
                        isl.get("src_port").asInt(), seqId, 0L));
                seqId++;
            }

            return new ImmutablePair<>(new PathInfoData(latency, forwardNodes),
                    new PathInfoData(latency, reverseNodes));
        }
    }
}

