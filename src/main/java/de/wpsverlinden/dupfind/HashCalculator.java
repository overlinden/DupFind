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
import org.springframework.beans.factory.annotation.Required;

public class HashCalculator {

    private Collection<FileEntry> entries;
    private String userDir;
    private OutputPrinter outputPrinter;

    @Required
    public void setEntries(Collection<FileEntry> entries) {
        this.entries = entries;
    }

    @Required
    public void setUserDir(String userDir) {
        this.userDir = userDir;
    }

    @Required
    public void setOutputPrinter(OutputPrinter outputPrinter) {
        this.outputPrinter = outputPrinter;
    }

    public void calculateHashes() {
        outputPrinter.print("Calculating hashes ...");
        entries.parallelStream()
                .filter((e) -> {
                    File file = new File(userDir + e.getPath());
                    return (file.exists() && file.isFile() && e.getHash().isEmpty() || e.getLastModified() < file.lastModified());
                })
                .forEach((e) -> {
                    calc(e);
                    outputPrinter.print(".");
                });
        outputPrinter.println(" done.");
    }

    private void calc(FileEntry current) {
        try {
            current.setHash(calcHash(new File(userDir + current.getPath())));
        } catch (Exception e) {
            outputPrinter.println("Error calculating hash for " + current.getPath() + ": " + e.getMessage());
        }
    }

    private byte[] calcChecksum(File file) throws Exception {
        byte[] buffer = new byte[8192];
        MessageDigest digest = MessageDigest.getInstance("MD5");
        int numRead;
        try (InputStream fis = new FileInputStream(file)) {
            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    digest.update(buffer, 0, numRead);
                }
            } while (numRead != -1);
        }
        return digest.digest();
    }

    private String calcHash(File file) throws Exception {
        byte[] bytes = calcChecksum(file);
        String result = "";

        for (byte b : bytes) {
            result += Integer.toString((b & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }
}
