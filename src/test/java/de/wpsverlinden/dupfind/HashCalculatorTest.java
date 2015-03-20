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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:HashCalculatorTest-context.xml")
public class HashCalculatorTest {

    @Autowired
    private HashCalculator hc;
    
    @Rule
    public final TemporaryFolder tmpFolder = new TemporaryFolder();
    
    private File tmp;

    @Before
    public void init() throws IOException {
        tmp = tmpFolder.newFile("DupFind");
        try (FileWriter fw = new FileWriter(tmp)) {
            fw.write("Test Content");
        }
    }
    
    @Test
    public void testCorrectHashCalculation() throws IOException {
        FileEntry e = new FileEntry(tmp.getAbsolutePath(), tmp.length(), tmp.lastModified(), "");
        hc.setEntries(Arrays.asList(e));
        hc.calculateHashes();
        assertEquals("d65cdbadce081581e7de64a5a44b4617", e.getHash()); 
    }
    
    @Test
    public void testThatNonExistingFilesAreSkipped() {
        FileEntry e = new FileEntry("invalid_file", 1, 2, "");
        hc.setEntries(Arrays.asList(e));
        
        hc.calculateHashes();
        
        assertEquals("", e.getHash());
    }
    
    @Test
    public void testThatExistingHashIsNotRecalculated() {
        FileEntry e = new FileEntry(tmp.getAbsolutePath(), tmp.length(), tmp.lastModified(), "old_hash");
        hc.setEntries(Arrays.asList(e));
        hc.calculateHashes();
        assertEquals("old_hash", e.getHash());
    }
    
    @Test
    public void testThatUpdatedFilesGetRecalculated() {
        FileEntry e = new FileEntry(tmp.getAbsolutePath(), tmp.length(), tmp.lastModified() - 1, "old_hash");
        hc.setEntries(Arrays.asList(e));
        hc.calculateHashes();
        assertEquals("d65cdbadce081581e7de64a5a44b4617", e.getHash());
    }
    
    @After
    public void shutdown() {
        tmp.delete();
    }
}
