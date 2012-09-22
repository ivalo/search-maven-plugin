/*
 * Copyright 2011 Markku Saarela 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. 
 * See the License for the specific language governing permissions and limitations under the License. 
 */
package jivalo.plugin.search;

import java.io.File;
import java.net.URI;
import java.util.LinkedList;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

/**
 * @author Markku Saarela
 * 
 */
public class FileSearchMojoTest extends AbstractMojoTestCase
{

    private FileSearchMojo mojo;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception
    {
        // required for mojo lookups to work
        super.setUp();
        File testPom = new File( getBasedir(), "src/test/resources/unit/plugin-config.xml" );
        mojo = (FileSearchMojo) lookupMojo( "file", testPom );
    }

    /**
     * jivalo-client-1.0.jar->/fi/jivalo/client/BaseBusinessDelegate.class
     * 
     * @throws Exception
     */
    public void testFoundFromJarNotStrict() throws Exception
    {
        File searchDir = new File( getBasedir() );
        URI searchUri = searchDir.toURI();
        StringBuilder sb = new StringBuilder( searchUri.toString() );
        sb.append( "src/test-resources/" );
        LinkedList< String > uris = new LinkedList< String >();
        uris.add( sb.toString() );
        setVariableValueToObject( mojo, "directoryUris", uris );
        setVariableValueToObject( mojo, "fileNameToSearch", "BaseBusinessDelegate.class" );
        setVariableValueToObject( mojo, "strictName", false );
        setVariableValueToObject( mojo, "searchSubDirectories", true );
        mojo.execute();
    }

    /**
     * jackrabbit-jca-2.5.1.rar->slf4j-api-1.6.4.jar
     * 
     * @throws Exception
     */
    public void testFoundFileFromInsideRar() throws Exception
    {
        File searchDir = new File( getBasedir() );
        URI searchUri = searchDir.toURI();
        StringBuilder sb = new StringBuilder( searchUri.toString() );
        sb.append( "src/test-resources/" );

        System.out.println( sb.toString() );
        LinkedList< String > uris = new LinkedList< String >();
        uris.add( sb.toString() );
        setVariableValueToObject( mojo, "directoryUris", uris );
        setVariableValueToObject( mojo, "fileNameToSearch", "slf4j-api-1.6.4.jar" );
        setVariableValueToObject( mojo, "strictName", false );
        setVariableValueToObject( mojo, "searchSubDirectories", true );
        mojo.execute();
    }

    /**
     * jackrabbit-jca-2.5.1.rar->slf4j-api-1.6.4.jar
     * 
     * @throws Exception
     */
    public void testFoundFileFromJarInsideRar() throws Exception
    {
        File searchDir = new File( getBasedir() );
        URI searchUri = searchDir.toURI();
        StringBuilder sb = new StringBuilder( searchUri.toString() );
        sb.append( "src/test-resources/" );
        LinkedList< String > uris = new LinkedList< String >();
        uris.add( sb.toString() );
        setVariableValueToObject( mojo, "directoryUris", uris );
        setVariableValueToObject( mojo, "fileNameToSearch", "InteractionImpl.class" );
        setVariableValueToObject( mojo, "strictName", false );
        setVariableValueToObject( mojo, "searchSubDirectories", true );
        mojo.execute();
    }

    /**
     * Find file jackrabbit-jca-2.5.1.rar
     * 
     * @throws Exception
     */
    public void testFoundFile() throws Exception
    {
        File searchDir = new File( getBasedir() );
        URI searchUri = searchDir.toURI();
        StringBuilder sb = new StringBuilder( searchUri.toString() );
        sb.append( "src/test-resources/" );
        LinkedList< String > uris = new LinkedList< String >();
        uris.add( sb.toString() );
        setVariableValueToObject( mojo, "directoryUris", uris );
        setVariableValueToObject( mojo, "fileNameToSearch", "jackrabbit-jca-2.5.1.rar" );
        setVariableValueToObject( mojo, "strictName", false );
        setVariableValueToObject( mojo, "searchSubDirectories", true );
        mojo.execute();
    }

    /**
     * jivalo-client-1.0.jar->/fi/jivalo/client/BaseBusinessDelegate.class
     * 
     * @throws Exception
     */
    public void testFoundFileWithStrictName() throws Exception
    {
        File searchDir = new File( getBasedir() );
        URI searchUri = searchDir.toURI();
        StringBuilder sb = new StringBuilder( searchUri.toString() );
        sb.append( "src/test-resources/first/second/" );
        LinkedList< String > uris = new LinkedList< String >();
        uris.add( sb.toString() );
        setVariableValueToObject( mojo, "directoryUris", uris );
        setVariableValueToObject( mojo, "fileNameToSearch", "fi/jivalo/client/BaseBusinessDelegate.class" );
        setVariableValueToObject( mojo, "strictName", true );
        setVariableValueToObject( mojo, "searchSubDirectories", true );
        mojo.execute();
    }

}
