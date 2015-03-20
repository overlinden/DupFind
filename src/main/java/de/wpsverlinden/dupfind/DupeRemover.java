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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Required;

public class DupeRemover {

    private Map<String, FileEntry> fileIndex;
    private DupeFinder dupeFinder;
    private OutputPrinter outputPrinter;
    private String userDir;

    @Required
    public void setDupeFinder(DupeFinder dupeFinder) {
        this.dupeFinder = dupeFinder;
    }
    
    @Required
    public void setFileIndex(Map<String, FileEntry> fileIndex) {
        this.fileIndex = fileIndex;
    }

    @Required
    public void setOutputPrinter(OutputPrinter outputPrinter) {
        this.outputPrinter = outputPrinter;
    }

    @Required
    public void setUserDir(String userDir) {
        this.userDir = userDir;
    }

    public void deleteDupes(Collection<FileEntry> dupes, FileEntry info) {
        if (dupes.size() > 0) {
            outputPrinter.println("Deleting dupes of " + info);
            dupes.stream()
                    .map((e) -> e.getPath())
                    .forEach((e) -> {
                        File del = new File(userDir + e);
                        fileIndex.remove(e);
                        del.delete();
                    });
            outputPrinter.println(" done.");
        }
    }

    public void deleteAllDupes() {
        outputPrinter.print("Delete dupes ...");
        Collection<List<FileEntry>> dupeEntries = dupeFinder.getDupeEntries();
        dupeEntries.stream()
                .forEach((lst) -> {
                    while (lst.size() > 1) {
                        String delPath = lst.get(lst.size() - 1).getPath();
                        File del = new File(userDir + delPath);
                        del.delete();
                        fileIndex.remove(delPath);
                        lst.remove(lst.size() - 1);
                    }
                });
        outputPrinter.println(" done.");
    }
}
