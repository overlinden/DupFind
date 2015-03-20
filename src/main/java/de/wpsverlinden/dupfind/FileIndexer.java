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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.springframework.beans.factory.annotation.Required;

public final class FileIndexer {

    private final Map<String, FileEntry> fileIndex;
    private final File userDir;
    private final String canonicalDir;
    private OutputPrinter outputPrinter;

    public FileIndexer(Map<String, FileEntry> fileIndex, String userDir) throws IOException {
        this.fileIndex = fileIndex;
        this.userDir = new File(userDir);
        canonicalDir = this.userDir.getCanonicalPath();
    }

    @Required
    public void setOutputPrinter(OutputPrinter outputPrinter) {
        this.outputPrinter = outputPrinter;
    }
    

    public void buildIndex() {
        pruneDeletedFiles();
        addUpdatedFiles(userDir);
        fileIndex.remove("/DupFind.index.gz");
        outputPrinter.println("Indexing files done. " + fileIndex.size() + " file(s) in index.");
    }

    public void pruneDeletedFiles() {
        int oldSize = fileIndex.size();
        outputPrinter.print("Removing deleted files ... ");
        removeDeletedFiles();
        outputPrinter.println("done. Removed " + (oldSize - fileIndex.size()) + " file(s) from index");
    }

    public void addUpdatedFiles(File dir) {
        int oldSize = fileIndex.size();
        outputPrinter.print("Adding updated files ... ");
        recAddFilesInDir(dir);
        outputPrinter.println("done. Updated " + (fileIndex.size() - oldSize) + " file(s) in index");
    }

    private void removeDeletedFiles() {
        List<String> removeList = fileIndex.keySet().parallelStream()
                .filter((e) -> {
                    File f = new File(canonicalDir + e);
                    return !f.exists() || !f.isFile();
                })
                .collect(Collectors.toList());

        fileIndex.keySet().removeAll(removeList);
    }

    private void recAddFilesInDir(File dir) {
        try {
            Map<String, FileEntry> collect = Files.walk(Paths.get(dir.getCanonicalPath()))
                    .parallel()
                    .filter(Files::isRegularFile)
                    .map((e) -> e.toFile())
                    .filter((e) -> {
                        FileEntry fi = fileIndex.get(getPath(e));
                        return fi == null || fi.getLastModified() < e.lastModified();
                    })
                    .collect(Collectors.toMap((e) -> getPath(e), (e) -> new FileEntry(getPath(e), e.length(), e.lastModified(), "")));
            fileIndex.putAll(collect);
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    private String getPath(File e) {
        return e.getPath().substring(canonicalDir.length()).replace("\\", "/");
    }

    public Map<String, FileEntry> getFileIndex() {
        return fileIndex;
    }

    public String pwd() {
        return (canonicalDir) + " > ";
    }
    
    public FileEntry getEntry(String path) {
        return fileIndex.get(path);
    }

    public void saveIndex() {
        File outFile = new File(userDir + "/DupFind.index.gz");
        outputPrinter.print("Saving index ... ");
        try (XMLEncoder xenc = new XMLEncoder(new GZIPOutputStream(new FileOutputStream(outFile)))) {
            xenc.writeObject(fileIndex);
            xenc.flush();
            outputPrinter.println("done.  " + fileIndex.size() + " files in index.");
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void loadIndex() {
        File file = new File(userDir + "/DupFind.index.gz");
        outputPrinter.print("Loading index ... ");
        if (file.exists() && file.isFile()) {
            try (XMLDecoder xdec = new XMLDecoder(new GZIPInputStream(new FileInputStream(file)))) {
                fileIndex.clear();
                fileIndex.putAll((Map<String, FileEntry>) xdec.readObject());
                outputPrinter.println("done. " + fileIndex.size() + " files in index.");
            } catch (Exception e) {
                System.err.println(e);
            }
        } else {
            outputPrinter.println("failed. Creating new index.");
            fileIndex.clear();
        }
    }
}
