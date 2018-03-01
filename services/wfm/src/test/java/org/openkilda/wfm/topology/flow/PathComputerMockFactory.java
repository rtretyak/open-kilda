package org.openkilda.wfm.topology.flow;

import org.openkilda.pce.provider.PathComputer;
import org.openkilda.pce.provider.PathComputerFactory;

public class PathComputerMockFactory implements PathComputerFactory {
    @Override
    public PathComputer getPathComputer() {
        return new PathComputerMock();
    }
}
