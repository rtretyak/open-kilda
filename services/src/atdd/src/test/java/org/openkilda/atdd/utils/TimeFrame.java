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

package org.openkilda.atdd.utils;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class TimeFrame {
    private long startTime = System.currentTimeMillis();
    private Long endTime = null;
    private Long length = null;

    public void close() {
        endTime = System.currentTimeMillis();
        length = endTime - startTime;
    }

    public String format(String format) {
        return String.format(format, lengthAsDouble());
    }

    private double lengthAsDouble() {
        double result = length.doubleValue();
        return result / 1000;
    }
}
