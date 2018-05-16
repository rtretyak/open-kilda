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

package org.openkilda.atdd.share;

import org.openkilda.LinksUtils;
import org.openkilda.atdd.service.SwitchService;
import org.openkilda.messaging.info.event.IslChangeType;
import org.openkilda.messaging.info.event.IslInfoData;
import org.openkilda.messaging.info.event.PathNode;

import cucumber.api.java.en.When;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TopologyChange {
    private SwitchService switchService = new SwitchService();

    @When("^make topology change - disable port (\\S+)-(\\d+)$")
    public void disablePort(String switchId, int portNumber) {
        String switchName = switchIdToSwitchName(switchId);
        Assert.assertTrue(switchService.portDown(switchName, portNumber));
    }

    @When("^make topology change - enable port (\\S+)-(\\d+)$")
    public void enablePort(String switchId, int portNumber) {
        String switchName = switchIdToSwitchName(switchId);
        Assert.assertTrue(switchService.portUp(switchName, portNumber));
    }

    @When("^wait topology change - isl (\\S+)-(\\d+) ==> (\\S+)-(\\d+) is missing$")
    public void waitIslMissing(String srcSwitch, int srcPort, String dstSwitch, int dstPort) {
        List<PathNode> path = makeIslPath(srcSwitch, srcPort, dstSwitch, dstPort);
        Assert.assertTrue(waitIsl(path, false));
    }

    @When("^wait topology change - isl (\\S+)-(\\d+) ==> (\\S+)-(\\d+) is available$")
    public void waitIslAvailable(String srcSwitch, int srcPort, String dstSwitch, int dstPort) {
        List<PathNode> path = makeIslPath(srcSwitch, srcPort, dstSwitch, dstPort);
        Assert.assertTrue(waitIsl(path, true));
    }

    private boolean waitIsl(List<PathNode> path, boolean desiredAvailability) {
        boolean reachGoal = false;
        long now = System.currentTimeMillis();
        long timeoutAt = now + TimeUnit.SECONDS.toMillis(30);
        do {
            if (desiredAvailability == checkIsIslAlive(path)) {
                reachGoal = true;
                break;
            }

            System.out.println(String.format("== ISL == %s is alive", path));
            sleep(2500);
            now = System.currentTimeMillis();
        } while (now < timeoutAt);

        return reachGoal;
    }

    private boolean checkIsIslAlive(List<PathNode> islPath) {
        boolean foundAliveIsl = false;
        List<IslInfoData> allLinks = LinksUtils.dumpLinks();
        for (IslInfoData isl : allLinks) {
            if (!IslChangeType.DISCOVERED.equals(isl.getState())) {
                continue;
            }
            if (!islPath.equals(isl.getPath())) {
                continue;
            }

            foundAliveIsl = true;
            break;
        }

        return foundAliveIsl;
    }

    private List<PathNode> makeIslPath(String srcSwitch, int srcPort, String dstSwitch, int dstPort) {
        ArrayList<PathNode> path = new ArrayList<>(2);
        path.add(new PathNode(srcSwitch, srcPort, 0));
        path.add(new PathNode(dstSwitch, dstPort, 1));

        return path;
    }

    private String switchIdToSwitchName(String switchId) {
        long dpid = Long.valueOf(switchId.replaceAll(":", ""), 16);
        dpid &= 0xffff;
        return String.format("%08d", dpid);
    }

    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            // this is normal;
        }
    }
}
