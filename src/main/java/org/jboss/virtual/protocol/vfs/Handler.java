/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.virtual.protocol.vfs;

import org.jboss.virtual.plugins.vfs.VirtualFileURLConnection;
import org.jboss.virtual.plugins.context.vfs.AssembledContextFactory;
import org.jboss.virtual.plugins.context.vfs.AssembledDirectory;
import org.jboss.virtual.VirtualFile;

import java.net.URLStreamHandler;
import java.net.URLConnection;
import java.net.URL;
import java.io.IOException;

/**
 * URLStreamHandler for VFS
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 1.1 $
 */
public class Handler extends URLStreamHandler
{
   protected URLConnection openConnection(URL u) throws IOException
   {
      String host = u.getHost();
      AssembledDirectory directory = AssembledContextFactory.getSingleton().find(host);
      if (directory == null) throw new IOException("vfs does not exist: " + u.toString());
      VirtualFile vf = directory.findChild(u.getPath());
      if (vf == null) throw new IOException("vfs does not exist: " + u.toString());
      return new VirtualFileURLConnection(u, vf);
   }

}