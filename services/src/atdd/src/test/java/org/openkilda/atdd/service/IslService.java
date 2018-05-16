/* Copyright 2018 Telstra Open Source
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

package org.openkilda.atdd.service;

import org.openkilda.LinksUtils;
import org.openkilda.messaging.info.event.IslInfoData;
import org.openkilda.messaging.info.event.PathNode;
import org.openkilda.messaging.model.NetworkEndpoint;

import java.util.ArrayList;
import java.util.List;

public class IslService extends AbstractService {
    /**
     * fetchIsl.
     */
    public List<IslInfoData> fetchIsl(NetworkEndpoint source, NetworkEndpoint dest) {
        ArrayList<PathNode> path = new ArrayList<>(2);
        path.add(new PathNode(source.getSwitchDpId(), source.getPortId(), 0));
        path.add(new PathNode(dest.getSwitchDpId(), dest.getPortId(), 1));

        ArrayList<IslInfoData> match = new ArrayList<>(1);
        for (IslInfoData isl : fetchAllIsl()) {
            if (!path.equals(isl.getPath())) {
                continue;
            }
            match.add(isl);
        }

        return match;
    }

    public List<IslInfoData> fetchAllIsl() {
        return LinksUtils.dumpLinks();
    }
}
