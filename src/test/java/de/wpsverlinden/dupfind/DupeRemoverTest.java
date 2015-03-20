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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
@ContextConfiguration("classpath:DupeRemoverTest-context.xml")
public class DupeRemoverTest {
    
    private final Map<String, FileEntry> index = new HashMap<>();
    
    @Autowired
    private DupeFinder dupeFinder;
    
    @Autowired
    private DupeRemover dupeRemover;
    
    @Rule
    public final TemporaryFolder tmpFolder = new TemporaryFolder();
    
    private String tmp1, tmp2;
    private FileEntry e1, e2;
    
    @Before
    public void init() throws IOException {
        tmp1 = tmpFolder.newFile().getAbsolutePath();
        tmp2 = tmpFolder.newFile().getAbsolutePath();
        e1 = new FileEntry(tmp1, 0, 1, "");
        e2 = new FileEntry(tmp2, 0, 1, "");
        index.put(tmp1, e1);
        index.put(tmp2, e2);

        dupeRemover.setFileIndex(index);
        dupeFinder.setFileIndex(index);
    }
    
    @Test
    public void testDupeGetsDeleted() throws InterruptedException {
        dupeRemover.deleteDupes(Arrays.asList(e2), e1);
        assertNotNull(index.get(tmp1));
        assertTrue(new File(tmp1).exists());
        assertNull(index.get(tmp2));
        assertFalse(new File(tmp2).exists());
    }
    
    @Test
    public void testAllDupesGetsDeleted() throws InterruptedException {
        dupeRemover.deleteAllDupes();
        assertEquals(1, index.size());
        assertTrue(new File(tmp1).exists() ^ new File(tmp2).exists());
    }
    
    @After
    public void shutdown() {
        new File(tmp1).delete();
        new File(tmp2).delete();
    }
}
