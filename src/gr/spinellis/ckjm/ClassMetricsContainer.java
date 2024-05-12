/*
 * (C) Copyright 2005 Diomidis Spinellis
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

package gr.spinellis.ckjm;

import org.apache.bcel.classfile.*;
import java.util.*;
import java.io.*;

/**
 * A container of class metrics mapping class names to their metrics.
 * This class contains the the metrics for all class's during the filter's
 * operation. Some metrics need to be updated as the program processes
 * other classes, so the class's metrics will be recovered from this
 * container to be updated.
 *
 * @version $Revision: 1.9 $
 * @author &lt;a href=&quot;http://www.spinellis.gr&quot;&gt;Diomidis Spinellis&lt;/a&gt;
 */
class ClassMetricsContainer {

    /** The map from class names to the corresponding metrics */
    private Map<String, ClassMetrics> classMetricsMap = new HashMap<>();

    /** Return a class's metrics */
    public ClassMetrics getMetrics(String className) {
        return classMetricsMap.computeIfAbsent(className, key -> new ClassMetrics());
    }

    /** Print the metrics of all the visited classes. */
    public void printMetrics(CkjmOutputHandler outputHandler) {
        classMetricsMap.entrySet().stream()
                .filter(entry -> entry.getValue().isVisited() && (MetricsFilter.includeAll() || entry.getValue().isPublic()))
                .forEach(entry -> outputHandler.handleClass(entry.getKey(), entry.getValue()));
    }
}

