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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Required;

public class DupeFinder {

    private Map<String, FileEntry> fileIndex;
    private OutputPrinter outputPrinter;

    @Required
    public void setFileIndex(Map<String, FileEntry> fileIndex) {
        this.fileIndex = fileIndex;
    }
    
    @Required
    public void setOutputPrinter(OutputPrinter outputPrinter) {
        this.outputPrinter = outputPrinter;
    }

    public Collection<FileEntry> getDupesOf(String path) {
        FileEntry info = (FileEntry) fileIndex.get(path);
        if (info == null) {
            outputPrinter.println("Could not find \"" + path + "\" in index");
            return Collections.EMPTY_LIST;
        }
        Collection<FileEntry> dupes = fileIndex.values().parallelStream()
                .filter((e) -> !(e.getPath().equals(info.getPath())))
                .filter((e) -> (e.getSize() == info.getSize()))
                .filter((e) -> (e.getHash().equals(info.getHash())))
                .collect(Collectors.toList());
        return dupes;
    }

    public Collection<List<FileEntry>> getDupeEntries() {
        Map<String, List<FileEntry>> dupeMap = fileIndex.values().parallelStream()
                .collect(Collectors.groupingBy((e) -> e.getSize() + "-" + e.getHash()));
        return dupeMap.values();
    }

    public int getNumOfDupes() {
        int numOfFiles = fileIndex.values().size();
        int numOfDistinctFiles = (int) fileIndex.values().parallelStream()
                .map((e) -> e.getSize() + "-" + e.getHash())
                .distinct()
                .count();
        return numOfFiles - numOfDistinctFiles;
    }
}
