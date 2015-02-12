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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DupeFinder {

    private final Map<String, FileEntry> index;

    public DupeFinder(Map<String, FileEntry> index) {
        this.index = index;
    }

    public ArrayList<FileEntry> getDupesOf(String path) {
        if (index == null) {
            throw new NoIndexException();
        }
        ArrayList<FileEntry> dupes = new ArrayList<>();
        FileEntry info = (FileEntry) index.get(path);
        if (info == null) {
            System.out.println("Could not find \"" + path + "\" in index");
            return dupes;
        }
        FileEntry[] d = index.values().parallelStream()
                .filter((e) -> !(e.getPath().equals(info.getPath())))
                .filter((e) -> (e.getSize() == info.getSize()))
                .filter((e) -> (e.getHash().equals(info.getHash())))
                .toArray(FileEntry[]::new);
        dupes.addAll(Arrays.asList(d));

        return dupes;
    }

    public void showDupesOf(String path) {
        if (index == null) {
            throw new NoIndexException();
        }
        FileEntry info = (FileEntry) index.get(path);
        if (info == null) {
            System.out.println("Index doesn't contain " + path);
            return;
        }
        
        ArrayList<FileEntry> dupes = getDupesOf(info.getPath());

        if (dupes.size() > 0) {
            System.out.println("-----\n" + info);
            dupes.stream().forEach((dp) -> {
                System.out.println(dp);
            });
            System.out.println("-----\n");
        } else {
            System.out.println("No dupes found.");
        }
    }

    public void showDupes() {
        Collection<List<FileEntry>> dupeEntries = getDupeEntries();
        dupeEntries.stream()
                .filter((e) -> e.size() >= 2)
                .forEach((lst) -> {
                    System.out.println("-----");
                    while (!lst.isEmpty()) {
                        System.out.println(lst.get(0));
                        lst.remove(0);
                    }
                    System.out.println("-----\n");
                });
    }

    public Collection<List<FileEntry>> getDupeEntries() {
        if (index == null) {
            throw new NoIndexException();
        }

        Map<String, List<FileEntry>> dupeMap = index.values().parallelStream()
                .collect(Collectors.groupingBy((e) -> e.getSize() + "-" + e.getHash()));
        return dupeMap.values();
    }

    public void showNumOfDupes() {
        if (index == null) {
            throw new NoIndexException();
        }

        int numOfFiles = index.values().size();
        long numOfDistinctFiles = index.values().parallelStream()
                .map((e) -> e.getSize() + "-" + e.getHash())
                .distinct()
                .count();
        System.out.println("Found " + (numOfFiles - numOfDistinctFiles) + " dupes in index.");
    }
}
