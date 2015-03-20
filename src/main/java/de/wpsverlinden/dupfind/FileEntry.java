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

import java.io.Serializable;
import java.util.Objects;

public class FileEntry implements Serializable {

    private static final long serialVersionUID = 1L;
    private String path, hash;
    private long size, lastModified;

    public FileEntry() {
    }

    public FileEntry(String path, long size, long lastModified, String hash) {
        this.path = path;
        this.size = size;
        this.lastModified = lastModified;
        this.hash = hash;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    
    @Override
    public String toString() {
        return "FileEntry{" + "path=" + path + ", hash=" + hash + ", size=" + size + ", lastModified=" + lastModified + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.path);
        hash = 37 * hash + Objects.hashCode(this.hash);
        hash = 37 * hash + (int) (this.size ^ (this.size >>> 32));
        hash = 37 * hash + (int) (this.lastModified ^ (this.lastModified >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileEntry other = (FileEntry) obj;
        if (!Objects.equals(this.path, other.path)) {
            return false;
        }
        if (!Objects.equals(this.hash, other.hash)) {
            return false;
        }
        if (this.size != other.size) {
            return false;
        }
        if (this.lastModified != other.lastModified) {
            return false;
        }
        return true;
    }
}
