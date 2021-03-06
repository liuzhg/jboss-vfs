/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.test.vfs;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import junit.framework.Test;
import org.jboss.test.vfs.support.VirtualFileAdaptor;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VFSUtils;
import org.jboss.vfs.VirtualFile;

/**
 * Tests of no copy nested jars
 *
 * @author ales.justin@jboss.org
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public class JARSerializationUnitTestCase extends AbstractVFSTest {
    public JARSerializationUnitTestCase(String name) {
        super(name);
    }

    public static Test suite() {
        return suite(JARSerializationUnitTestCase.class);
    }

    /**
     * Test reading the contents of nested jar entries.
     *
     * @throws Exception for any error
     */
    public void testInnerJarFile() throws Exception {
        VirtualFile outerjar = getVirtualFile("/vfs/test/outer.jar");
        List<Closeable> mounts = recursiveMount(outerjar);
        try {
            assertTrue("outer.jar != null", outerjar != null);
            VirtualFile jar1 = outerjar.getChild("jar1.jar");
            assertTrue("outer.jar/jar1.jar != null", jar1 != null);
            VirtualFile jar2 = outerjar.getChild("jar2.jar");
            assertTrue("outer.jar/jar2.jar != null", jar2 != null);

            VirtualFile jar1MF = jar1.getChild("META-INF/MANIFEST.MF");
            assertNotNull("jar1!/META-INF/MANIFEST.MF", jar1MF);
            InputStream mfIS = jar1MF.openStream();
            Manifest mf1 = new Manifest(mfIS);
            Attributes mainAttrs1 = mf1.getMainAttributes();
            String title1 = mainAttrs1.getValue(Attributes.Name.SPECIFICATION_TITLE);
            assertEquals("jar1", title1);
            mfIS.close();

            VirtualFile jar2MF = jar2.getChild("META-INF/MANIFEST.MF");
            assertNotNull("jar2!/META-INF/MANIFEST.MF", jar2MF);
            InputStream mfIS2 = jar2MF.openStream();
            Manifest mf2 = new Manifest(mfIS2);
            Attributes mainAttrs2 = mf2.getMainAttributes();
            String title2 = mainAttrs2.getValue(Attributes.Name.SPECIFICATION_TITLE);
            assertEquals("jar2", title2);
            mfIS2.close();
        } finally {
            VFSUtils.safeClose(mounts);
        }
    }

    public void testInnerJarFileSerialization() throws Exception {
        VirtualFile outerjar = getVirtualFile("/vfs/test/outer.jar");
        List<Closeable> mounts = recursiveMount(outerjar);
        try {
            assertTrue("outer.jar != null", outerjar != null);
            log.info("outer.jar: " + outerjar);
            VirtualFile jar1 = outerjar.getChild("jar1.jar");
            assertTrue("outer.jar/jar1.jar != null", jar1 != null);
            VirtualFile jar2 = outerjar.getChild("jar2.jar");
            assertTrue("outer.jar/jar2.jar != null", jar2 != null);

            VirtualFile jar1MF = jar1.getChild("META-INF/MANIFEST.MF");
            assertNotNull("jar1!/META-INF/MANIFEST.MF", jar1MF);
            InputStream mfIS = jar1MF.openStream();
            Manifest mf1 = new Manifest(mfIS);
            Attributes mainAttrs1 = mf1.getMainAttributes();
            String title1 = mainAttrs1.getValue(Attributes.Name.SPECIFICATION_TITLE);
            assertEquals("jar1", title1);
            mfIS.close();

            VirtualFile jar1DS = serializeDeserialize(jar1, VirtualFile.class);
            assertNotNull("jar1 deserialized", jar1DS);
            VirtualFile jar1DSMF = jar1.getChild("META-INF/MANIFEST.MF");
            mfIS = jar1DSMF.openStream();
            mf1 = new Manifest(mfIS);
            mainAttrs1 = mf1.getMainAttributes();
            title1 = mainAttrs1.getValue(Attributes.Name.SPECIFICATION_TITLE);
            assertEquals("jar1", title1);
            mfIS.close();
        } finally {
            VFSUtils.safeClose(mounts);
        }
    }

    public void testInnerJarFilesOnlyFileSerialization() throws Exception {
        VirtualFile outerjar = getVirtualFile("/vfs/test/outer.jar");
        List<Closeable> mounts = recursiveMount(outerjar);
        try {
            assertTrue("outer.jar != null", outerjar != null);
            log.info("outer.jar: " + outerjar);
            VirtualFile jar1 = outerjar.getChild("jar1-filesonly.jar");
            assertTrue("outer.jar/jar1-filesonly.jar != null", jar1 != null);

            VirtualFile jar1MF = jar1.getChild("META-INF/MANIFEST.MF");
            assertNotNull("jar1-filesonly!/META-INF/MANIFEST.MF", jar1MF);
            InputStream mfIS = jar1MF.openStream();
            Manifest mf1 = new Manifest(mfIS);
            Attributes mainAttrs1 = mf1.getMainAttributes();
            String title1 = mainAttrs1.getValue(Attributes.Name.SPECIFICATION_TITLE);
            assertEquals("jar1-filesonly", title1);
            mfIS.close();

            VirtualFile jar1DS = serializeDeserialize(jar1, VirtualFile.class);
            assertNotNull("jar1 deserialized", jar1DS);
            VirtualFile jar1DSMF = jar1DS.getChild("META-INF/MANIFEST.MF");
            assertNotNull("jar1-filesonly!/META-INF/MANIFEST.MF", jar1DSMF);
            mfIS = jar1DSMF.openStream();
            mf1 = new Manifest(mfIS);
            mainAttrs1 = mf1.getMainAttributes();
            title1 = mainAttrs1.getValue(Attributes.Name.SPECIFICATION_TITLE);
            assertEquals("jar1-filesonly", title1);
            mfIS.close();
        } finally {
            VFSUtils.safeClose(mounts);
        }
    }

    public void testLevelZips() throws Exception {
        URL rootURL = getResource("/vfs/test");
        VirtualFile root = VFS.getChild(rootURL);
        VirtualFile one = root.getChild("level1.zip");
        List<Closeable> mounts = recursiveMount(one);
        try {
            VirtualFile textOne = one.getChild("test1.txt");
            testText(textOne);
            VirtualFile two = one.getChild("level2.zip");
            VirtualFile textTwo = two.getChild("test2.txt");
            testText(textTwo);
            VirtualFile three = two.getChild("level3.zip");
            VirtualFile textThree = three.getChild("test3.txt");
            testText(textThree);

            three = serializeDeserialize(three, VirtualFile.class);
            textThree = three.getChild("test3.txt");
            testText(textThree);

            two = serializeDeserialize(two, VirtualFile.class);
            textTwo = two.getChild("test2.txt");
            testText(textTwo);
            three = two.getChild("level3.zip");
            textThree = two.getChild("level3.zip/test3.txt");
            testText(textThree);
            textThree = three.getChild("test3.txt");
            testText(textThree);

            one = serializeDeserialize(one, VirtualFile.class);
            textOne = one.getChild("test1.txt");
            testText(textOne);
            two = one.getChild("level2.zip");
            textTwo = one.getChild("level2.zip/test2.txt");
            testText(textTwo);
            textTwo = two.getChild("test2.txt");
            testText(textTwo);
            three = one.getChild("level2.zip/level3.zip");
            textThree = three.getChild("test3.txt");
            testText(textThree);
            textThree = one.getChild("level2.zip/level3.zip/test3.txt");
            testText(textThree);
            three = two.getChild("level3.zip");
            textThree = three.getChild("test3.txt");
            testText(textThree);
            textThree = two.getChild("level3.zip/test3.txt");
            testText(textThree);

            textThree = serializeDeserialize(textThree, VirtualFile.class);
            testText(textThree);
        } finally {
            VFSUtils.safeClose(mounts);
        }
    }

    public void test2ndLevelRead() throws Exception {
        VirtualFile root = getVirtualFile("/vfs/test/level1.zip");
        List<Closeable> mounts = recursiveMount(root);
        try {
            VirtualFile file = root.getChild("level2.zip");
            file = serializeDeserialize(file, VirtualFile.class);
            VirtualFile text = file.getChild("test2.txt");
            testText(text);
        } finally {
            VFSUtils.safeClose(mounts);
        }
    }

    public void testEarsInnerJarChild() throws Exception {
        VirtualFile root = getVirtualFile("/vfs/test/interop_W2JREMarshallTest_appclient_vehicle.ear");
        List<Closeable> mounts = recursiveMount(root);
        try {
            VirtualFile file = root.getChild("interop_W2JREMarshallTest_appclient_vehicle_client.jar");
            VirtualFile child = file.getChild("MarshallTest.xml");
            String text = getText(child);
            assertNotNull(text);
            assertTrue(text.length() > 0);
            // serialize
            file = serializeDeserialize(file, VirtualFile.class);
            child = file.getChild("MarshallTest.xml");
            text = getText(child);
            assertNotNull(text);
            assertTrue(text.length() > 0);
        } finally {
            VFSUtils.safeClose(mounts);
        }
    }

    public void testVirtualFileAdaptor() throws Exception {
        VirtualFile root = getVirtualFile("/vfs/test/interop_W2JREMarshallTest_appclient_vehicle.ear");
        VirtualFile file = root.getChild("interop_W2JREMarshallTest_appclient_vehicle_client.jar");
        VirtualFile same = file.getChild("");
        // serialize
        testVirtualFileAdaptor(same, "MarshallTest.xml");
    }

    public void testDeepVFAMechanism() throws Exception {
        VirtualFile root = getVirtualFile("/vfs/test");
        VirtualFile one = root.getChild("level1.zip");
        testVirtualFileAdaptor(one, "test1.txt");
        VirtualFile textOne = one.getChild("test1.txt");
        testVirtualFileAdaptor(textOne, "../level2.zip");
        VirtualFile two = one.getChild("level2.zip");
        testVirtualFileAdaptor(two, "test2.txt");
        VirtualFile textTwo = two.getChild("test2.txt");
        testVirtualFileAdaptor(textTwo, "../level3.zip");
        VirtualFile three = two.getChild("level3.zip");
        testVirtualFileAdaptor(three, "test3.txt");
        VirtualFile textThree = three.getChild("test3.txt");
        testVirtualFileAdaptor(textThree, "../test3.txt");

        three = serializeDeserialize(three, VirtualFile.class);
        testVirtualFileAdaptor(three, "test3.txt");
        textThree = three.getChild("test3.txt");
        testVirtualFileAdaptor(textThree, "../test3.txt");

        two = serializeDeserialize(two, VirtualFile.class);
        testVirtualFileAdaptor(two, "test2.txt");
        textTwo = two.getChild("test2.txt");
        testVirtualFileAdaptor(textTwo, "../level3.zip");
        three = two.getChild("level3.zip");
        testVirtualFileAdaptor(three, "test3.txt");
        textThree = two.getChild("level3.zip/test3.txt");
        testVirtualFileAdaptor(textThree, "../test3.txt");
        textThree = three.getChild("test3.txt");
        testVirtualFileAdaptor(textThree, ".././test3.txt");

        one = serializeDeserialize(one, VirtualFile.class);
        testVirtualFileAdaptor(one, "test1.txt");
        textOne = one.getChild("test1.txt");
        testVirtualFileAdaptor(textOne, "../level2.zip");
        two = one.getChild("level2.zip");
        testVirtualFileAdaptor(two, "test2.txt");
        textTwo = one.getChild("level2.zip/test2.txt");
        testVirtualFileAdaptor(textTwo, "../level3.zip");
        textTwo = two.getChild("test2.txt");
        testVirtualFileAdaptor(textTwo, "../level3.zip");
        three = one.getChild("level2.zip/level3.zip");
        testVirtualFileAdaptor(three, "test3.txt");
        textThree = three.getChild("test3.txt");
        testVirtualFileAdaptor(textThree, "..");
        textThree = one.getChild("level2.zip/level3.zip/test3.txt");
        testVirtualFileAdaptor(textThree, "..");
        three = two.getChild("level3.zip");
        testVirtualFileAdaptor(three, "test3.txt");
        textThree = three.getChild("test3.txt");
        testVirtualFileAdaptor(textThree, "../..");
        textThree = two.getChild("level3.zip/test3.txt");
        testVirtualFileAdaptor(textThree, "../..");
    }

    public void testNestedDir() throws Exception {
        URL url = getResource("/vfs/test/spring-ear.ear");
        String urlString = url.toExternalForm();
        int p = urlString.indexOf(":/");
        List<Closeable> mounts = recursiveMount(VFS.getChild(url));
        try {
            url = new URL(VFSUtils.VFS_PROTOCOL + urlString.substring(p) + "/lib/spring-beans.jar/org/jboss/test/spring");
            VirtualFile file = VFS.getChild(url);
            assertNotNull("No beans dir", file.getChild("beans"));
            testVirtualFileAdaptor(file, "beans");
        } finally {
            VFSUtils.safeClose(mounts);
        }
    }

    protected void testVirtualFileAdaptor(VirtualFile file, String pathName) throws Exception {
        VirtualFileAdaptor adaptor = new VirtualFileAdaptor(file);
        adaptor = serializeDeserialize(adaptor, VirtualFileAdaptor.class);
        VirtualFileAdaptor vfaChild = adaptor.findChild(pathName);
        assertNotNull(vfaChild);
        List<VirtualFile> children = file.getChildren();
        if (children != null) {
            for (VirtualFile child : children) {
                adaptor = new VirtualFileAdaptor(child);
                adaptor = serializeDeserialize(adaptor, VirtualFileAdaptor.class);
                assertNotNull(adaptor.findChild("..")); // should find parent
            }
        }
    }

    protected String getText(VirtualFile file) throws Exception {
        InputStream in = file.openStream();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {
            }
        }
    }

    protected void testText(VirtualFile file) throws Exception {
        String text = getText(file);
        assertEquals("Some test.", text);
    }
}