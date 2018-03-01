/* Copyright 2017 Telstra Open Source
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.openkilda.pce.provider.neo4j;

import org.neo4j.driver.v1.Driver;
import org.openkilda.messaging.info.event.PathInfoData;
import org.openkilda.messaging.model.Flow;
import org.openkilda.messaging.model.ImmutablePair;
import org.openkilda.pce.provider.PathComputer;
import org.openkilda.pce.provider.UnroutablePathException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

class Neo4jPathComputer implements PathComputer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jPathComputer.class);

    /**
     * {@link Driver} instance.
     */
    private final Driver driver;

    /**
     * @param driver NEO4j driver(connect)
     */
    Neo4jPathComputer(Driver driver) {
        this.driver = Objects.requireNonNull(driver);
    }

    @Override
    public ImmutablePair<PathInfoData, PathInfoData> getPath(Flow flow, Strategy strategy)
            throws UnroutablePathException {
        AbstractPathQueryBuilder queryBuilder = getPathQueryBuilder(strategy)
                .srcSwitch(flow.getSourceSwitch())
                .destSwitch(flow.getDestinationSwitch())
                .bandwidth(flow.getBandwidth());
        if (!flow.isIgnoreBandwidth()) {
            queryBuilder.bandwidth(flow.getBandwidth());
        }
        if (flow.isOneSwitchFlow()) {
            queryBuilder.oneSwitchFlow();
        }

        //TODO: apply condition on ISL ports.

        return queryBuilder.buildQuery().executeForSingle().orElseThrow(() -> new UnroutablePathException(flow));
    }

    private AbstractPathQueryBuilder getPathQueryBuilder(Strategy strategy) {
        switch (strategy) {
            case HOPS:
                return new HopsPathQueryBuilder(driver);
            case COST:
                return new CostPathQueryBuilder(driver);
            default:
                throw new UnsupportedOperationException("Not implemented yet.");
        }
    }
}
