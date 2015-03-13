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
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Collection;

public class HashCalculator {

    private final Collection<FileEntry> entries;

    public HashCalculator(Collection<FileEntry> entries) {
        this.entries = entries;
    }

    public void calculateHashes() {
        if (entries == null) {
            throw new NoIndexException();
        }
        System.out.print("Calculating hashes ...");
        entries.parallelStream()
                .filter((e) -> {
                    File file = new File(System.getProperty("user.dir") + e.getPath());
                    return (e.getHash().isEmpty() || e.getLastModified() < file.lastModified());
                })
                .forEach((e) -> {
                    calc(e);
                });
        System.out.println(" done.");
    }

    public void calc(FileEntry current) {
        try {
            current.setHash(calcHash(System.getProperty("user.dir") + current.getPath()));
            System.out.print(".");
        } catch (Exception e) {
            System.out.println("Error calculating hash for " + current.getPath() + ": " + e.getMessage());
        }
    }

    private byte[] calcChecksum(String path) throws Exception {
        byte[] buffer = new byte[8192];
        MessageDigest digest = MessageDigest.getInstance("MD5");
        int numRead;
        byte[] hash = null;
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            try (InputStream fis = new FileInputStream(file)) {
                do {
                    numRead = fis.read(buffer);
                    if (numRead > 0) {
                        digest.update(buffer, 0, numRead);
                    }
                } while (numRead != -1);
            }
            hash = digest.digest();
        }
        return hash;
    }

    public String calcHash(String path) throws Exception {
        byte[] b = calcChecksum(path);
        String result = "";

        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }
}