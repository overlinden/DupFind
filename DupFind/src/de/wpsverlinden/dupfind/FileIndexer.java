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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FileIndexer {

    private final static int MB_PER_DOT = 250;
    private HashMap<String, FileEntry> index = new HashMap<>();
    private File dir = new File(".");
    private String canonicalDir;

    public FileIndexer() {
        try {
            canonicalDir = dir.getCanonicalPath();
        } catch (IOException e) {
            canonicalDir = dir.getAbsolutePath();
        }
    }

    public void buildIndex() {
        System.out.print("Indexing files ...");
        removeDeletedFiles();
        recAddFilesInDir(dir);
        index.remove(canonicalDir + File.separator + "DupFind.index.gz");
        System.out.println(" done. " + index.size() + " file(s) in index");
    }

    public void pruneDeletedFiles() {
        int oldSize = index.size();
        System.out.print("Removing deleted files ... ");
        removeDeletedFiles();
        System.out.println("done. Removed " + (oldSize - index.size()) + " file(s) from index");
    }

    private void removeDeletedFiles() {
        File file;
        ArrayList<String> removeList = new ArrayList<>();
        for (String path : index.keySet()) {
            file = new File(path);
            if (!file.exists() || !file.isFile()) {
                removeList.add(path);
            }
        }

        for (String path : removeList) {
            index.remove(path);
        }
    }

    private void recAddFilesInDir(File dir) {
        int fileSizeMB;
        int cntMB = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File entry : files) {
                try {
                    FileEntry fi = index.get(entry.getCanonicalPath());
                    if (entry.isDirectory()) {
                        recAddFilesInDir(entry);
                    } else if (fi == null || fi.getLastModified() < entry.lastModified()) {
                        index.put(entry.getCanonicalPath(), new FileEntry(entry.getCanonicalPath(), entry.length(), entry.lastModified(), ""));
                        fileSizeMB = (int) entry.length() / (1024 * 1024);
                        cntMB += (fileSizeMB >= 1 ? fileSizeMB : 1);
                    }
                    if (cntMB >= MB_PER_DOT) {
                        cntMB = 0;
                        System.out.print(".");
                    }
                } catch (IOException e) {
                }
            }
        }
    }

    public HashMap<String, FileEntry> getIndex() {
        return index;
    }

    public void cd(String dir) {
        if (".".equals(dir)) {
            return;
        }
        if ("..".equals(dir) && this.dir.getParent() != null) {
            this.dir = new File(this.dir.getParent());
            try {
                this.canonicalDir = this.dir.getCanonicalPath();
            } catch (IOException e) {
                this.canonicalDir = this.dir.getAbsolutePath();
            }
            return;
        }

        File directory = new File(this.dir + File.separator + dir);

        if (directory.exists() && directory.isDirectory()) {
            this.dir = directory;
            try {
                this.canonicalDir = this.dir.getCanonicalPath();
            } catch (IOException e) {
                this.canonicalDir = this.dir.getAbsolutePath();
            }
            return;
        }

        directory = new File(dir);
        if (directory.exists() && directory.isDirectory()) {
            this.dir = directory;
            try {
                this.canonicalDir = this.dir.getCanonicalPath();
            } catch (IOException e) {
                this.canonicalDir = this.dir.getAbsolutePath();
            }
            return;
        }

        System.out.println("Invalid directory: " + dir);
    }

    public String pwd() {
        return canonicalDir + "> ";
    }

    public void saveIndex() {
        String outFile = canonicalDir + File.separator + "DupFind.index.gz";
        System.out.print("Saving index ... ");
        try (ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(outFile)))) {
            oos.writeObject(index);
            oos.flush();
            System.out.println("done.  " + index.size() + " files in index.");
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    @SuppressWarnings("unchecked")
    public boolean loadIndex() {
        boolean ret = false;
        File file = new File(canonicalDir + File.separator + "DupFind.index.gz");
        System.out.print("Loading index ... ");
        if (file.exists() && file.isFile()) {
            try (ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)))) {
                index = (HashMap<String, FileEntry>) ois.readObject();
                System.out.println("done. " + index.size() + " files in index.");
                ret = true;
            } catch (Exception e) {
                System.err.println(e);
            }
        } else {
            index = new HashMap<>();
            System.out.println("failed. No index found.");
            ret = false;
        }

        return ret;
    }
}
