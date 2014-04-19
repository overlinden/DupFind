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
import java.util.HashMap;
import java.util.List;

public class DupeRemover {

    private HashMap<String, FileEntry> index;
    private DupeFinder df;

    public DupeRemover(DupeFinder df, HashMap<String, FileEntry> index) {
        this.index = index;
        this.df = df;
    }

    public void deleteDupesOf(String dir, String path) {
        if (index == null) {
            System.out.println("No index loaded");
            return;
        }
        FileEntry info = (FileEntry) index.get(dir + File.separator + path);
        FileEntry info2 = (FileEntry) index.get(path);
        if (info == null && info2 == null) {
            System.out.println("Index doesn't contain " + path);
            return;
        }
        info = (info != null ? info : info2);
        ArrayList<FileEntry> dupes = df.getDupesOf(info.getPath());

        if (dupes.size() > 0) {
            System.out.println("Deleting dupes of " + info);
            for (FileEntry dp : dupes) {
                String delPath = dp.getPath();
                File del = new File(delPath);
                index.remove(delPath);
                del.delete();
            }
        }
    }

    public void deleteDupes() {
        System.out.print("Delete dupes ...");
        HashMap<String, List<FileEntry>> dupeMap = df.getDupeMap();
        for (List<FileEntry> lst : dupeMap.values()) {
            while (lst.size() > 1) {
                String delPath = lst.get(lst.size() - 1).getPath();
                File del = new File(delPath);
                del.delete();
                index.remove(delPath);
                lst.remove(lst.size() - 1);
            }
        }
        System.out.println(" done.");
    }
}
