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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    public void showDupesOf(String dir, String path) {
        if (index == null) {
            throw new NoIndexException();
        }
        FileEntry info = (FileEntry) index.get(dir + File.separator + path);
        FileEntry info2 = (FileEntry) index.get(path);
        if (info == null && info2 == null) {
            System.out.println("Index doesn't contain " + path);
            return;
        }
        info = (info != null ? info : info2);
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
        boolean output = false;
        Collection<List<FileEntry>> dupeEntries = getDupeEntries();
        for (List<FileEntry> lst : dupeEntries) {
            if (lst.size() >= 2) {
                output = true;
                System.out.println("-----");
                while (!lst.isEmpty()) {
                    System.out.println(lst.get(0));
                    lst.remove(0);
                }
                System.out.println("-----\n");
            }
        }

        if (!output) {
            System.out.println("No dupes found.");
        }
    }

    public Collection<List<FileEntry>> getDupeEntries() {
        Map<String, List<FileEntry>> dupeMap = new HashMap<>();
        if (index == null) {
            throw new NoIndexException();
        }

        for (FileEntry fe : index.values()) {
            String key = fe.getSize() + "-" + fe.getHash();
            List<FileEntry> lst = dupeMap.get(key);
            if (lst == null) {
                lst = new LinkedList<>();
                dupeMap.put(key, lst);
                lst.add(fe);
            } else {
                lst.add(fe);
            }
        }
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
