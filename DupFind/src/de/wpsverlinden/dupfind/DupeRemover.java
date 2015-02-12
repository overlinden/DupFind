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

public class DupeRemover {

    private final Map<String, FileEntry> index;
    private final DupeFinder df;

    public DupeRemover(DupeFinder df, Map<String, FileEntry> index) {
        this.index = index;
        this.df = df;
    }

    public void deleteDupesOf(String path) {
        if (index == null) {
            throw new NoIndexException();
        }
        FileEntry info = (FileEntry) index.get(path);
        if (info == null) {
            System.out.println("Index doesn't contain " + path);
            return;
        }
        Collection<FileEntry> dupes = df.getDupesOf(info.getPath());

        if (dupes.size() > 0) {
            System.out.println("Deleting dupes of " + info);

            dupes.stream()
                    .map((e) -> e.getPath())
                    .forEach((e) -> {
                        File del = new File(System.getProperty("user.dir") + e);
                        index.remove(e);
                        del.delete();
                    });
            System.out.println(" done.");
        }
    }

    public void deleteDupes() {
        System.out.print("Delete dupes ...");
        Collection<List<FileEntry>> dupeEntries = df.getDupeEntries();
        dupeEntries.stream()
                .forEach((lst) -> {
                    while (lst.size() > 1) {
                        String delPath = lst.get(lst.size() - 1).getPath();
                        File del = new File(System.getProperty("user.dir") + delPath);
                        del.delete();
                        index.remove(delPath);
                        lst.remove(lst.size() - 1);
                    }
                });
        System.out.println(" done.");
    }
}
