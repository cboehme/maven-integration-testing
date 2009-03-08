package org.apache.maven.it;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

import java.io.File;
import java.util.List;

/**
 * This is a test set for <a href="http://jira.codehaus.org/browse/MNG-3043">MNG-3043</a>.
 * 
 * @author Benjamin Bentmann
 */
public class MavenITmng3043BestEffortReactorResolutionTest
    extends AbstractMavenIntegrationTestCase
{

    public MavenITmng3043BestEffortReactorResolutionTest()
    {
        super( "[2.1.0,)" );
    }

    /**
     * Test that dependencies on attached artifacts like a test JAR or an EJB client JAR which have not been built
     * yet, i.e. in build phases prior to "package" like "test", are satisfied from the output directories of the
     * projects in the reactor. This is meant as a best effort to provide a class path for compilation or testing.
     */
    public void testitTestPhase()
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/mng-3043" );

        Verifier verifier = new Verifier( testDir.getAbsolutePath() );
        verifier.setAutoclean( false );
        verifier.deleteDirectory( "consumer-a/target" );
        verifier.deleteDirectory( "consumer-b/target" );
        verifier.deleteDirectory( "consumer-c/target" );
        verifier.deleteArtifacts( "org.apache.maven.its.mng3043" );
        verifier.setLogFileName( "log-test.txt" );
        verifier.executeGoal( "validate" );
        verifier.verifyErrorFreeLog();
        verifier.resetStreams();

        List classpath;

        classpath = verifier.loadLines( "consumer-a/target/compile.txt", "UTF-8" );
        assertContains( classpath, new String[] { "classes-test" } );
        assertNotContains( classpath, new String[] { "classes-main" } );
        classpath = verifier.loadLines( "consumer-a/target/runtime.txt", "UTF-8" );
        assertContains( classpath, new String[] { "classes-test" } );
        assertNotContains( classpath, new String[] { "classes-main" } );
        classpath = verifier.loadLines( "consumer-a/target/test.txt", "UTF-8" );
        assertContains( classpath, new String[] { "classes-test" } );
        assertNotContains( classpath, new String[] { "classes-main" } );

        classpath = verifier.loadLines( "consumer-b/target/compile.txt", "UTF-8" );
        assertContains( classpath, new String[] { "classes-main" } );
        assertNotContains( classpath, new String[] { "classes-test" } );
        classpath = verifier.loadLines( "consumer-b/target/runtime.txt", "UTF-8" );
        assertContains( classpath, new String[] { "classes-main" } );
        assertNotContains( classpath, new String[] { "classes-test" } );
        classpath = verifier.loadLines( "consumer-b/target/test.txt", "UTF-8" );
        assertContains( classpath, new String[] { "classes-main" } );
        assertNotContains( classpath, new String[] { "classes-test" } );

        classpath = verifier.loadLines( "consumer-c/target/compile.txt", "UTF-8" );
        assertContains( classpath, new String[] { "classes-main" } );
        assertContains( classpath, new String[] { "classes-test" } );
        classpath = verifier.loadLines( "consumer-c/target/runtime.txt", "UTF-8" );
        assertContains( classpath, new String[] { "classes-main" } );
        assertContains( classpath, new String[] { "classes-test" } );
        classpath = verifier.loadLines( "consumer-c/target/test.txt", "UTF-8" );
        assertContains( classpath, new String[] { "classes-main" } );
        assertContains( classpath, new String[] { "classes-test" } );
    }

    /**
     * Test that dependency resolution still uses to the actual artifact files once these have been
     * assembled/attached in the "package" phase. This ensures the class path is accurate and not locked to
     * the output directories of the best effort model from above.
     */
    public void testitPackagePhase()
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/mng-3043" );

        Verifier verifier = new Verifier( testDir.getAbsolutePath() );
        verifier.setAutoclean( false );
        verifier.deleteDirectory( "consumer-a/target" );
        verifier.deleteDirectory( "consumer-b/target" );
        verifier.deleteDirectory( "consumer-c/target" );
        verifier.deleteArtifacts( "org.apache.maven.its.mng3043" );
        verifier.setLogFileName( "log-package.txt" );
        verifier.executeGoal( "initialize" );
        verifier.verifyErrorFreeLog();
        verifier.resetStreams();

        List classpath;

        classpath = verifier.loadLines( "consumer-a/target/compile.txt", "UTF-8" );
        assertContains( classpath, new String[] { "tests.jar" } );
        assertNotContains( classpath, new String[] { "client.jar" } );
        classpath = verifier.loadLines( "consumer-a/target/runtime.txt", "UTF-8" );
        assertContains( classpath, new String[] { "tests.jar" } );
        assertNotContains( classpath, new String[] { "client.jar" } );
        classpath = verifier.loadLines( "consumer-a/target/test.txt", "UTF-8" );
        assertContains( classpath, new String[] { "tests.jar" } );
        assertNotContains( classpath, new String[] { "client.jar" } );

        classpath = verifier.loadLines( "consumer-b/target/compile.txt", "UTF-8" );
        assertContains( classpath, new String[] { "client.jar" } );
        assertNotContains( classpath, new String[] { "tests.jar" } );
        classpath = verifier.loadLines( "consumer-b/target/runtime.txt", "UTF-8" );
        assertContains( classpath, new String[] { "client.jar" } );
        assertNotContains( classpath, new String[] { "tests.jar" } );
        classpath = verifier.loadLines( "consumer-b/target/test.txt", "UTF-8" );
        assertContains( classpath, new String[] { "client.jar" } );
        assertNotContains( classpath, new String[] { "tests.jar" } );

        classpath = verifier.loadLines( "consumer-c/target/compile.txt", "UTF-8" );
        assertContains( classpath, new String[] { "client.jar" } );
        assertContains( classpath, new String[] { "tests.jar" } );
        classpath = verifier.loadLines( "consumer-c/target/runtime.txt", "UTF-8" );
        assertContains( classpath, new String[] { "client.jar" } );
        assertContains( classpath, new String[] { "tests.jar" } );
        classpath = verifier.loadLines( "consumer-c/target/test.txt", "UTF-8" );
        assertContains( classpath, new String[] { "client.jar" } );
        assertContains( classpath, new String[] { "tests.jar" } );
    }

    private void assertContains( List collection, Object[] items )
    {
        for ( int i = 0; i < items.length; i++ )
        {
            assertContains( collection, items[i] );
        }
    }

    private void assertContains( List collection, Object item )
    {
        assertTrue( item + " missing in " + collection, collection.contains( item ) );
    }

    private void assertNotContains( List collection, Object[] items )
    {
        for ( int i = 0; i < items.length; i++ )
        {
            assertNotContains( collection, items[i] );
        }
    }

    private void assertNotContains( List collection, Object item )
    {
        assertFalse( item + " present in " + collection, collection.contains( item ) );
    }

}