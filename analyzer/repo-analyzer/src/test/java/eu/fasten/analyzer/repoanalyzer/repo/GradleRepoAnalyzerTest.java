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

package eu.fasten.analyzer.repoanalyzer.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GradleRepoAnalyzerTest {

    private static RepoAnalyzer analyzer;
    private static String root;

    @BeforeAll
    static void setUp() {
        root = new File(Objects.requireNonNull(GradleRepoAnalyzerTest.class.getClassLoader()
                .getResource("simpleGradleRepo")).getFile()).getAbsolutePath();
        var repoAnalyzerFactory = new RepoAnalyzerFactory();
        analyzer = repoAnalyzerFactory.getAnalyzer(root);
    }

    @Test
    void analyzerTypeCheck() {
        assertTrue(analyzer instanceof GradleRepoAnalyzer);
    }

    @Test
    void getPathToSourcesRoot() throws IOException, DocumentException {
        assertEquals(Path.of(root, "src/main/java"), analyzer.getPathToSourcesRoot(Path.of(root)));
    }

    @Test
    void getPathToTestsRoot() throws IOException, DocumentException {
        assertEquals(Path.of(root, "src/test/java"), analyzer.getPathToTestsRoot(Path.of(root)));
    }

    @Test
    void extractModuleRoots() throws IOException, DocumentException {
        var roots = Set.of(Path.of(root), Path.of(root, "subprojects/module1"),
                Path.of(root, "subprojects/module2"));
        assertEquals(roots, analyzer.extractModuleRoots(Path.of(root)));
    }

    @Test
    void gradleKotlin() {
        root = new File(Objects.requireNonNull(GradleRepoAnalyzerTest.class.getClassLoader()
                .getResource("simpleGradleKotlinRepo")).getFile()).getAbsolutePath();
        var repoAnalyzerFactory = new RepoAnalyzerFactory();
        analyzer = repoAnalyzerFactory.getAnalyzer(root);
    }
}