/*
 * Copyright 2007 - 2012 Markku Saarela 
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * 
 * @author Markku Saarela
 * 
 */
@Mojo( name = "file", requiresProject = false, threadSafe = true )
public class FileSearchMojo extends AbstractMojo
{
    private static String NL = System.getProperty( "line.separator" );

    /**
     * list of URI's. If not defined System property <code>user.dir</code> is used.
     * 
     */
    @Parameter( required = false )
    private List< String > directoryUris;

    /**
     * 
     */
    @Parameter( property = "search.file", required = false )
    private String fileNameToSearch;

    /**
     * If true then <code>fileNameToSearch</code> is fully qualified file name within Package (JAR,RAR,ZIP). Example:
     * fi/jivalo/client/BaseBusinessDelegate.class
     */
    @Parameter( property = "strict", defaultValue = "true", required = false )
    private boolean strictName;

    /**
     * If true then also all sub directories of <code>directoryUris</code> is searched
     */
    @Parameter( property = "sub.dirs", defaultValue = "true", required = false )
    private boolean searchSubDirectories;

    /**
     * Default constructor.
     */
    public FileSearchMojo()
    {
        super();
    }

    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        List< File > dirs = searchDirectoryList();
        getLog().info( buildEntryLogMsg( dirs ) );
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
            List< String > processedDirectory = processDirectory( file );
            if ( processedDirectory != null && !processedDirectory.isEmpty() )
            {
                list.addAll( processedDirectory );
            }
        }
        return list;
    }

    private List< String > processDirectory( final File directory )
    {
        StringBuilder logMsg = new StringBuilder( "Start processing directory: " + directory );
        if ( null == directory || !directory.exists() )
        {
            if ( null != directory )
            {
                logMsg.append( " not found absolute path " + directory.getAbsolutePath() );
            }
            getLog().info( logMsg );
            return new ArrayList< String >( 0 );
        }
        logMsg.append( " absolute path " + directory.getAbsolutePath() );
        getLog().debug( logMsg );
        return processFiles( directory.listFiles() );
    }

    private LinkedList< String > processFiles( File[] listFiles )
    {
        LinkedList< String > list = new LinkedList< String >();
        for ( File dirFile : listFiles )
        {
            if ( dirFile.isDirectory() && this.searchSubDirectories )
            {
                List< String > dirList = processDirectory( dirFile );
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

    private List< String > searchEntries( final ZipFile zip )
    {
        Enumeration< ? extends ZipEntry > entries = zip.entries();
        LinkedList< String > matchedFiles = new LinkedList< String >();
        while ( entries.hasMoreElements() )
        {
            matchedFiles.addAll( searchEntry( zip, entries.nextElement(), null ) );
        }
        return matchedFiles;
    }

    private List< String > searchEntry( final ZipFile zip, final ZipEntry entry, final String nestedZipName )
    {
        LinkedList< String > matchedFiles = new LinkedList< String >();
        if ( !entry.isDirectory() )
        {
            if ( ( ( !strictName && entry.getName().endsWith( this.fileNameToSearch ) ) || ( strictName && entry
                    .getName().equals( this.fileNameToSearch ) ) ) )
            {
                matchedFiles.add( getEntryName( zip, entry, nestedZipName ) );
            }
            else if ( entry.getName().endsWith( ".jar" ) )
            {
                JarInputStream jarInputStream = null;
                try
                {
                    jarInputStream = new JarInputStream( zip.getInputStream( entry ) );
                    ZipEntry nextEntry = jarInputStream.getNextEntry();
                    while ( nextEntry != null )
                    {
                        matchedFiles.addAll( searchEntry( zip, nextEntry, entry.getName() ) );
                        jarInputStream.closeEntry();
                        nextEntry = jarInputStream.getNextEntry();
                    }
                }
                catch ( IOException e1 )
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                finally
                {
                    if ( jarInputStream != null )
                    {
                        try
                        {
                            jarInputStream.close();
                        }
                        catch ( IOException e )
                        {
                            // Nothing to do.
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return matchedFiles;
    }

    private String getEntryName( ZipFile zip, ZipEntry entry, String nestedZipName )
    {
        StringBuilder sb = new StringBuilder( zip.getName() ).append( "!" );
        if ( nestedZipName != null )
        {
            sb.append( nestedZipName ).append( "!" );
        }
        return sb.append( entry.getName() ).toString();
    }

    private CharSequence buildEntryLogMsg( List< File > dirs )
    {
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
        return msg;
    }
}
