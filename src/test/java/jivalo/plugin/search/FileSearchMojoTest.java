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

import org.junit.Test;

/**
 * @author Markku Saarela
 * 
 */
public class FileSearchMojoTest {

  @Test
  public void execute() throws Exception {

    File searchDir = new File(System.getProperty("user.dir"));

    URI searchUri = searchDir.toURI();

    StringBuilder sb = new StringBuilder(searchUri.toString());

    sb.append("src/test-resources/");

    LinkedList<String> uris = new LinkedList<String>();

    uris.add(sb.toString());

    FileSearchMojo mojo = new FileSearchMojo(uris, "InteractionImpl.class", false, true);

    mojo.execute();

    mojo = new FileSearchMojo(uris, "rsadapter.rar", false, true);

    mojo.execute();
    
    mojo = new FileSearchMojo(uris, "BaseBusinessDelegate.class", false, true);

    mojo.execute();
    

  }
}
