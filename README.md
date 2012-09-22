# Search Maven plugin for searching files from directories and variant packages.

Mainly used for searching missing dependency class (ClassNotFound) jars

## Define Maven plugin groups for jIvalo plugins


if plugin group definition is not done then fully qualified plugin name must be used.

mvn jivalo.plugins:search-maven-plugin:file

    <pluginGroups>
      <pluginGroup>jivalo.plugins</pluginGroup>
    </pluginGroups>

If jivalo plugin groups is defined then short version of plugin is used

mvn search:file

Here is sample search plugin configuration:

      <plugin>
        <groupId>jivalo.plugins</groupId>
        <artifactId>search-maven-plugin</artifactId>
        <version>1.0-SNAPSHOT</version>
        <configuration>
          <directoryUris>
            <directoryUri>file:/home/markku/eclipse/plugins</directoryUri>
          </directoryUris>
          <fileNameToSearch>PreferenceableOption.class</fileNameToSearch>
          <strictName>false</strictName>
          <searchSubDirectories>true</searchSubDirectories>
        </configuration>
      </plugin>

