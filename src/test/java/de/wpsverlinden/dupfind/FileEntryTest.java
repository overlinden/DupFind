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

import org.junit.Test;
import static org.junit.Assert.*;

public class FileEntryTest {

    @Test
    public void testConstructorAndGetter() {
        FileEntry e = new FileEntry("path", 1, 2, "hash");
        assertEquals("path", e.getPath());
        assertEquals(1, e.getSize());
        assertEquals(2, e.getLastModified());
        assertEquals("hash", e.getHash());
    }

    @Test
    public void testSetHash() {
        FileEntry e = new FileEntry();
        e.setHash("hash");
        assertEquals("hash", e.getHash());
    }

    @Test
    public void testToString() {
        FileEntry e = new FileEntry("path", 1, 2, "hash");
        assertEquals("FileEntry{path=path, hash=hash, size=1, lastModified=2}", e.toString());
    }
    
    @Test
    public void testEqualsAndHashCode() {
        FileEntry e1 = new FileEntry("path", 1, 2, "hash");
        FileEntry e2 = new FileEntry("path", 1, 2, "hash");
        FileEntry e3 = new FileEntry("path2", 3, 4, "hash2");
        
        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        
        assertFalse(e1.equals(e3));
        assertFalse(e1.hashCode() == e3.hashCode());
        
        assertFalse(e1.equals((FileEntry)null));
        assertFalse(e1.equals(new Object()));
    }
}
