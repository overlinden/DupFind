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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FileIndexer {

    private Map<String, FileEntry> index = new HashMap<>();
    private Map<String, FileEntry> synchronizedIndex = Collections.synchronizedMap(index);
    private File dir;
    private String canonicalDir;

    public FileIndexer() throws IOException {
        cd(".");
    }

    public void buildIndex() {
        pruneDeletedFiles();
        addUpdatedFiles(dir);
        index.remove(canonicalDir + File.separator + "DupFind.index.gz");
        System.out.println("Indexing files done. " + index.size() + " file(s) in index");
    }

    public void pruneDeletedFiles() {
        int oldSize = index.size();
        System.out.print("Removing deleted files ... ");
        removeDeletedFiles();
        System.out.println("done. Removed " + (oldSize - index.size()) + " file(s) from index");
    }

    public void addUpdatedFiles(File dir) {
        int oldSize = index.size();
        System.out.print("Adding updated files ... ");
        recAddFilesInDir(dir);
        System.out.println("done. Updated " + (index.size() - oldSize) + " file(s) in index");
    }

    private void removeDeletedFiles() {
        List<String> removeList = index.keySet().parallelStream()
                .filter((e) -> {
                    File f = new File(e);
                    return !f.exists() || !f.isFile();
                })
                .collect(Collectors.toList());

        index.keySet().removeAll(removeList);
    }

    private void recAddFilesInDir(File dir) {
        try {
            Files.walk(Paths.get(dir.getCanonicalPath()))
                    .parallel()
                    .filter(Files::isRegularFile)
                    .map((e) -> e.toFile())
                    .filter((e) -> {
                        FileEntry fi = null;
                        try {
                            fi = synchronizedIndex.get(e.getCanonicalPath());
                        } catch (IOException ex) {
                        }
                        return fi == null || fi.getLastModified() < e.lastModified();
                    })
                    .forEach((e) -> {
                        try {
                            synchronizedIndex.put(e.getCanonicalPath(), new FileEntry(e.getCanonicalPath(), e.length(), e.lastModified(), ""));
                            System.out.print(".");
                        } catch (IOException ex) {
                            System.err.println(ex);
                        }
                    });
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    public Map<String, FileEntry> getIndex() {
        return index;
    }

    public void cd(String dir) throws IOException {
        if (".".equals(dir)) {
            this.dir = new File(dir).getCanonicalFile();
            canonicalDir = this.dir.getCanonicalPath();
            return;
        }
        
        //Navigate up
        if ("..".equals(dir) && this.dir.getParent() != null) {
            this.dir = new File(this.dir.getParent());
            this.canonicalDir = this.dir.getCanonicalPath();
            return;
        }

        //Navigate into sub directory
        File directory = new File(this.dir + File.separator + dir);
        if (directory.exists() && directory.isDirectory()) {
            this.dir = directory;
            this.canonicalDir = this.dir.getCanonicalPath();
            return;
        }

        //Navigate to full specified path
        directory = new File(dir);
        if (directory.exists() && directory.isDirectory()) {
            this.dir = directory;
            this.canonicalDir = this.dir.getCanonicalPath();
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
    public void loadIndex() {
        File file = new File(canonicalDir + File.separator + "DupFind.index.gz");
        System.out.print("Loading index ... ");
        if (file.exists() && file.isFile()) {
            try (ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)))) {
                index = (HashMap<String, FileEntry>) ois.readObject();
                synchronizedIndex = Collections.synchronizedMap(index);
                System.out.println("done. " + index.size() + " files in index.");
            } catch (Exception e) {
                System.err.println(e);
            }
        } else {
            index = new HashMap<>();
            synchronizedIndex = Collections.synchronizedMap(index);
            System.out.println("failed. No index found.");
        }
    }
}
