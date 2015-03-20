/*
 *   DupFind - An easy to use file duplicate locator
 *   Copyright (C) 2012  Oliver Verlinden (http://wps-verlinden.de)
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.wpsverlinden.dupfind;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class FileIndexerTest {

    private final Map<String, FileEntry> index = new HashMap<>();
    private FileIndexer fi;
    private OutputPrinter o;

    @Rule
    public final TemporaryFolder tmpFolder = new TemporaryFolder();

    @Before
    public void init() throws IOException {

        fi = new FileIndexer(index, tmpFolder.getRoot().getPath());
        o = new OutputPrinter();
        o.setOutputStream(null);
        fi.setOutputPrinter(o);
    }

    @Test
    public void newFilesAreIndexedCorrectly() throws IOException {
        File f1 =tmpFolder.newFile("f1.txt");
        fi.buildIndex();
        assertEquals(1, index.size());
        final FileEntry f1get = index.get("/f1.txt");
        assertNotNull(f1get);
        assertEquals("/" + f1.getName(), f1get.getPath());
        assertEquals(f1.length(), f1get.getSize());
        assertEquals(f1.lastModified(), f1get.getLastModified());
    }

    @Test
    public void testIndexPersistence() throws IOException {
        index.put("path0", new FileEntry("path0", 10, 22, "hash0"));
        fi.saveIndex();
        FileIndexer fi2 = new FileIndexer(new HashMap<>(), tmpFolder.getRoot().getPath());
        fi2.setOutputPrinter(o);
        fi2.loadIndex();
        assertEquals(fi.getFileIndex(), fi2.getFileIndex());
    }

    @Test
    public void loadIndexGeneratesAFreshIndex() {
        index.put("path0", new FileEntry("path0", 10, 22, "hash0"));
        fi.loadIndex();
        assertTrue(fi.getFileIndex().isEmpty());
    }
    
    @Test
    public void getEntryReturnsCorrectEntry() {
        final FileEntry entry = new FileEntry("path0", 10, 22, "hash0");
        index.put("path0", entry);
        assertEquals(entry, fi.getEntry("path0"));
    }
    
    @Test
    public void pwdReturnsCorrectPath() throws IOException {
        assertEquals(tmpFolder.getRoot().getCanonicalPath() + " > ", fi.pwd());
    }
}
