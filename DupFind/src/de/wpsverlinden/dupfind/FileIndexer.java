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

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class FileIndexer {

    private final Map<String, FileEntry> index = new HashMap<>();
    private final File dir;
    private final String canonicalDir;

    public FileIndexer() throws IOException {
        this.dir = new File(".");
        canonicalDir = this.dir.getCanonicalPath();
    }

    public void buildIndex() {
        pruneDeletedFiles();
        addUpdatedFiles(dir);
        index.remove("/DupFind.index.gz");
        System.out.println("Indexing files done. " + index.size() + " file(s) in index.");
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
                    File f = new File(canonicalDir + e);
                    return !f.exists() || !f.isFile();
                })
                .collect(Collectors.toList());

        index.keySet().removeAll(removeList);
    }

    private void recAddFilesInDir(File dir) {
        try {
            Map<String, FileEntry> collect = Files.walk(Paths.get(dir.getCanonicalPath()))
                    .parallel()
                    .filter(Files::isRegularFile)
                    .map((e) -> e.toFile())
                    .filter((e) -> {
                        FileEntry fi = index.get(getPath(e));
                        return fi == null || fi.getLastModified() < e.lastModified();
                    })
                    .collect(Collectors.toMap((e) -> getPath(e), (e) -> new FileEntry(getPath(e), e.length(), e.lastModified(), "")));
            index.putAll(collect);
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    private String getPath(File e) {
        return e.getPath().substring(canonicalDir.length()).replace("\\", "/");
    }

    public Map<String, FileEntry> getIndex() {
        return index;
    }

    public String pwd() {
        return (canonicalDir) + " > ";
    }

    public void saveIndex() {
        File outFile = new File("DupFind.index.gz");
        System.out.print("Saving index ... ");
        try (XMLEncoder xenc = new XMLEncoder(new GZIPOutputStream(new FileOutputStream(outFile)))) {
            xenc.writeObject(index);
            xenc.flush();
            System.out.println("done.  " + index.size() + " files in index.");
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void loadIndex() {
        File file = new File("DupFind.index.gz");
        System.out.print("Loading index ... ");
        if (file.exists() && file.isFile()) {
            try (XMLDecoder xdec = new XMLDecoder(new GZIPInputStream(new FileInputStream(file)))) {
                index.clear();
                index.putAll((HashMap<String, FileEntry>) xdec.readObject());
                System.out.println("done. " + index.size() + " files in index.");
            } catch (Exception e) {
                System.err.println(e);
            }
        } else {
            System.out.println("failed. Creating new index.");
        }
    }
}
