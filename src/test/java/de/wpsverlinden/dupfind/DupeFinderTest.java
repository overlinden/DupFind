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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:DupeFinderTest-context.xml")
public class DupeFinderTest {
    
    private final Map<String, FileEntry> index = new HashMap<>();
    
    @Autowired
    private DupeFinder df;
    
    @Before
    public void init() {
        index.put("path0", new FileEntry("path0", 10, 22, "hash0"));
        
        //Same file, different path (with hash)
        index.put("path1", new FileEntry("path1", 1, 2, "hash1"));
        index.put("path2", new FileEntry("path2", 1, 2, "hash1"));
               
        //Same file, different path (without hash)
        index.put("path3", new FileEntry("path3", 1, 2, ""));
        index.put("path4", new FileEntry("path4", 1, 2, ""));
        
        df.setFileIndex(index);
    }
    
    @Test
    public void testDupesRecognitionBasedOnHashAndPathWorks() {
        assertEquals(2, df.getNumOfDupes());
    }
    
    @Test
    public void testGetDupesOfPath() {
        assertEquals(0, df.getDupesOf("path0").size());
        assertEquals(1, df.getDupesOf("path1").size());
        assertEquals(new FileEntry("path2", 1, 2, "hash1"), df.getDupesOf("path1").iterator().next());
    }
    
    @Test()
    public void testGetDupesOfInvlidPath() {
        assertEquals(Collections.EMPTY_LIST, df.getDupesOf("invalid_path"));
    }
    
    @Test
    public void testGetDupeEntries() {
        final Collection<List<FileEntry>> dupes = df.getDupeEntries();
        assertEquals(3, dupes.size());
        final Iterator<List<FileEntry>> iterator = dupes.iterator();
        final List<FileEntry> list1 = iterator.next();
        final List<FileEntry> list2 = iterator.next();
        final List<FileEntry> list3 = iterator.next();
        assertEquals(1, list1.size());
        assertEquals(2, list2.size());
        assertEquals(2, list3.size());
        Assert.assertArrayEquals(new FileEntry[]{new FileEntry("path0", 10, 22, "hash0")}, list1.toArray());
        Assert.assertArrayEquals(new FileEntry[]{new FileEntry("path1", 1, 2, "hash1"),new FileEntry("path2", 1, 2, "hash1")}, list2.toArray());
        Assert.assertArrayEquals(new FileEntry[]{new FileEntry("path3", 1, 2, ""),new FileEntry("path4", 1, 2, "")}, list3.toArray());
    }
}
