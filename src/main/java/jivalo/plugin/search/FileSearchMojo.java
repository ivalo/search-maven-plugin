/*
 * Copyright 2007 Markku Saarela 
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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal file-search
 * @requiresProject false
 * 
 * @author Markku Saarela
 * 
 */
public class FileSearchMojo extends AbstractMojo
{

    private static String NL = System.getProperty( "line.separator" );

    /**
     * list of URI's.
     * If not defined System property <code>user.dir</code> is used.
     * @parameter directoryUris list of directories for search to be used
     */
    private List< String > directoryUris;

    /**
     * @parameter expression = "${search.file}"
     */
    private String fileNameToSearch;

    /**
     * @parameter default-value="true"
     */
    private boolean strictName;

    /**
     * @parameter expression = "${search.sub}" default-value="true"
     */
    private boolean searchSubDirectories;

    /**
     * Default constructor.
     */
    public FileSearchMojo()
    {
        super();
    }

    /**
     * Constructor for testing.
     * 
     * @param directoryUris
     * @param fileNameToSearch
     * @param strictName
     * @param useFQCN
     * @param searchSubDirectories
     */
    FileSearchMojo( final List< String > directoryUris, final String fileNameToSearch, final boolean strictName, final boolean searchSubDirectories )
    {
        super();
        this.directoryUris = directoryUris;
        this.fileNameToSearch = fileNameToSearch;
        this.strictName = strictName;
        this.searchSubDirectories = searchSubDirectories;
    }

    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        List< File > dirs = searchDirectoryList();

        StringBuilder msg = new StringBuilder( "file-search :" );
        msg.append( NL ).append( "\t" );
        msg.append( "Directories to search:" ).append( dirs );
        msg.append( NL ).append( "\t" );
        msg.append( "fileNameToSearch=" ).append( fileNameToSearch );
        msg.append( NL ).append( "\t" );
        msg.append( "searchSubDirectories=" ).append( searchSubDirectories );
        msg.append( NL ).append( "\t" );
        msg.append( "strictName=" ).append( strictName );
        msg.append( NL ).append( "\t" );
        getLog().info(msg);

        if ( this.fileNameToSearch != null )
        {

            processResult( processDirectories( dirs ) );
        }
        else
        {
            getLog().info( "File name to search was null" );
        }
    }

    private void processResult( final List< String > list )
    {
        StringBuffer sb = new StringBuffer();

        if ( list.size() > 0 )
        {
            sb.append( "File " ).append( this.fileNameToSearch );
            sb.append( " found in these locations:" );

            String prefix = System.getProperty( "line.separator" ) + "\t";

            for ( String location : list )
            {
                sb.append( prefix ).append( location );
            }
        }
        else
        {
            sb.append( "File " ).append( this.fileNameToSearch );
            sb.append( " not found in directories " );
            if ( this.searchSubDirectories )
            {
                sb.append( " ( subdirectories included )" );
            }
            else
            {

            }
            sb.append( this.directoryUris );

        }
        getLog().info( sb );
    }

    private List< File > searchDirectoryList()
    {
        LinkedList< File > dirs = new LinkedList< File >();

        if ( this.directoryUris != null )
        {
            for ( String uri : this.directoryUris )
            {
                try
                {
                    dirs.add( new File( new URI( uri ) ) );
                }
                catch ( URISyntaxException e )
                {
                    getLog().error( "Wrong URI syntax: " + uri, e );
                    new MojoExecutionException( "Wrong URI syntax: " + uri, e );
                }
            }
        }
        else
        {
            dirs.add( new File( System.getProperty( "user.dir" ) ) );
        }

        return dirs;
    }

    private List< String > processDirectories( final List< File > dirs )
    {
        LinkedList< String > list = new LinkedList< String >();

        for ( File file : dirs )
        {
            LinkedList< String > processedDirectory = processDirectory( file );

            if ( processedDirectory != null && !processedDirectory.isEmpty() )
            {
                list.addAll( processedDirectory );
            }
        }

        return list;
    }

    private LinkedList< String > processDirectory( final File directory )
    {

        LinkedList< String > list = new LinkedList< String >();
        StringBuilder logMsg = new StringBuilder( "Start processing directory: " + directory );
        if ( null == directory || !directory.exists() )
        {
            if ( null != directory )
            {
                logMsg.append( " not found absolute path " + directory.getAbsolutePath() );
            }
            getLog().info( logMsg );
            return list;
        }

        logMsg.append( " absolute path " + directory.getAbsolutePath() );
        getLog().debug( logMsg );

        File[] listFiles = directory.listFiles();

        for ( int i = 0; i < listFiles.length; i++ )
        {
            File dirFile = listFiles[i];

            if ( dirFile.isDirectory() && this.searchSubDirectories )
            {
                LinkedList< String > dirList = processDirectory( dirFile );

                if ( dirList != null && !dirList.isEmpty() )
                {
                    list.addAll( dirList );
                }
            }
            else
            {

                LinkedList< String > matchedFiles = processFile( dirFile );

                if ( matchedFiles != null && !matchedFiles.isEmpty() )
                {
                    list.addAll( matchedFiles );
                }
            }
        }

        return list;
    }

    private LinkedList< String > processFile( final File file )
    {
        LinkedList< String > matchedFile = new LinkedList< String >();

        try
        {
            String canonicalFile = file.getCanonicalPath();

            if ( canonicalFile.endsWith( this.fileNameToSearch ) )
            {
                matchedFile.add( canonicalFile );
            }
            else if ( canonicalFile.endsWith( ".zip" ) || canonicalFile.endsWith( ".jar" )
                    || canonicalFile.endsWith( ".rar" ) )
            {
                ZipFile zipFile = new ZipFile( file );

                matchedFile.addAll( searchEntries( zipFile ) );

                zipFile.close();
            }
        }
        catch ( IOException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return matchedFile;
    }

    private List< String > searchEntries( final ZipFile file )
    {
        Enumeration< ? extends ZipEntry > entries = file.entries();

        LinkedList< String > matchedFiles = new LinkedList< String >();

        while ( entries.hasMoreElements() )
        {
            ZipEntry entry = entries.nextElement();

            if ( !entry.isDirectory() )
            {
                if ( ( !strictName && entry.getName().endsWith( this.fileNameToSearch ) || strictName
                        && entry.getName().substring( entry.getName().lastIndexOf( '/' ) + 1 )
                                .equals( this.fileNameToSearch ) ) )
                {
                    matchedFiles.add( file.getName() + "!" + entry.getName() );
                }
                else if ( entry.getName().endsWith( ".jar" ) )
                {
                    File file2 = new File( file.getName() );
                    URI uri = file2.toURI();
                    try
                    {
                        StringBuilder stringBuilder = new StringBuilder( "jar:" );
                        stringBuilder.append( uri.toURL().toString() );
                        // JarURLConnection openConnection = (JarURLConnection) new URL( stringBuilder.append( "!/" )
                        // .append( entry.getName() ).toString() ).openConnection();
                        // Object content = openConnection.getContent();
                        // List< String > lst1 = processEntries(jarFile.entries(), jarFile.getName());
                    }
                    catch ( IOException e )
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }

        return matchedFiles;
    }

}
