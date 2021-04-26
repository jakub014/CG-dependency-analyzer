/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.fasten.core.data;

import java.util.EnumMap;
import java.util.Map;

/**
 * Builder for {@link ExtendedRevisionPythonCallGraph}.
 */
public final class ExtendedBuilderPython extends ExtendedBuilder<EnumMap<PythonScope, Map<String, PythonType>>> {
    public ExtendedBuilderPython nodeCount(final int nodeCount) {
        this.nodeCount = nodeCount;
        return this;
    }

    public ExtendedBuilderPython forge(final String forge) {
        this.forge = forge;
        return this;
    }

    public ExtendedBuilderPython product(final String product) {
        this.product = product;
        return this;
    }

    public ExtendedBuilderPython version(final String version) {
        this.version = version;
        return this;
    }

    public ExtendedBuilderPython cgGenerator(final String cgGenerator) {
        this.cgGenerator = cgGenerator;
        return this;
    }

    public ExtendedBuilderPython timestamp(final long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public ExtendedBuilderPython graph(final Graph graph) {
        this.graph = graph;
        return this;
    }

    public ExtendedBuilderPython classHierarchy(final EnumMap<PythonScope, Map<String, PythonType>> cha) {
        this.classHierarchy = cha;
        return this;
    }

    public ExtendedRevisionPythonCallGraph build() {
        return new ExtendedRevisionPythonCallGraph(this);
    }
}
