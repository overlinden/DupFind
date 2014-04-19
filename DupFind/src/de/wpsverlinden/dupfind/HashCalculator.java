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

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HashCalculator {

    HashMap<String, FileEntry> index;

    public HashCalculator(HashMap<String, FileEntry> index) {
        this.index = index;
    }

    public void calculateHashes(int numThreads) {
        if (index == null) {
            System.out.println("No index loaded");
            return;
        }
        ConcurrentLinkedQueue<FileEntry> workQueue = new ConcurrentLinkedQueue<>();
        for (FileEntry entry : index.values()) {
            if (entry.getHash().isEmpty()) {
                workQueue.add(entry);
            }
        }
        HashCalcThread[] threads = new HashCalcThread[numThreads];
        System.out.print("Calculating hashes ...");
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new HashCalcThread(workQueue);
            threads[i].start();
        }

        try {
            for (int i = 0; i < numThreads; i++) {
                threads[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(" done.");
    }
}
