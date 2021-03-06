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
package org.openkilda.atdd.staging.tests.monkey_suite.prepare;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import cucumber.api.CucumberOptions;
import cucumber.api.java.After;
import org.junit.runner.RunWith;
import org.openkilda.atdd.staging.cucumber.CucumberWithSpringProfile;
import org.openkilda.atdd.staging.service.northbound.NorthboundService;
import org.openkilda.atdd.staging.service.traffexam.OperationalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@RunWith(CucumberWithSpringProfile.class)
@CucumberOptions(features = {"classpath:features/monkey_suite.feature"},
        glue = {"org.openkilda.atdd.staging.tests.monkey_suite.prepare", "org.openkilda.atdd.staging.steps"},
        tags = {"@Prepare"},
        plugin = {"json:target/cucumber-reports/monkey_suite_prepare_report.json"})
@ActiveProfiles("mock")
public class MonkeySuitePrepareTest {

    public static class MonkeySuitePrepareHook {

        @Autowired
        private NorthboundService northboundService;

        @After
        public void assertsAndVerifyMocks() throws OperationalException {
            verify(northboundService, times(3)).addFlow(any());
        }
    }
}
