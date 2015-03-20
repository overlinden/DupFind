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

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DupFind {

    private FileIndexer fileIndexer;
    private HashCalculator hashCalculator;
    private DupeFinder dupeFinder;
    private DupeRemover dupeRemover;
    private OutputPrinter outputPrinter;

    public static void main(String[] args) {

        ApplicationContext ctx = new ClassPathXmlApplicationContext("AppConfig.xml");
        DupFind app = (DupFind) ctx.getBean("dupFind");
        try {
            app.run();
        } catch (IOException ex) {
            Logger.getLogger(DupFind.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Required
    public void setFileIndexer(FileIndexer fileIndexer) {
        this.fileIndexer = fileIndexer;
    }

    @Required
    public void setHashCalculator(HashCalculator hashCalculator) {
        this.hashCalculator = hashCalculator;
    }

    @Required
    public void setDupeFinder(DupeFinder dupeFinder) {
        this.dupeFinder = dupeFinder;
    }

    @Required
    public void setDupeRemover(DupeRemover dupeRemover) {
        this.dupeRemover = dupeRemover;
    }

    @Required
    public void setOutputPrinter(OutputPrinter outputPrinter) {
        this.outputPrinter = outputPrinter;
    }

    private void run() throws IOException {
        outputPrinter.printSplash();
        fileIndexer.loadIndex();

        Scanner sc = new Scanner(new InputStreamReader(System.in));
        while (true) {
            System.out.print(fileIndexer.pwd());
            String line = sc.nextLine();
            if ("exit".equals(line)) {
                break;
            }
            String[] words = line.split(" ");

            if ("help".equals(words[0])) {
                outputPrinter.printHelp();
            } else if ("build_index".equals(words[0]) && words.length == 1) {
                buildIndex();

            } else if ("calc_hashes".equals(words[0]) && words.length >= 1 && words.length <= 2) {
                calcHashes();

            } else if ("show_dupes_of".equals(words[0]) && words.length >= 2) {
                showDupesOf(line.substring(words[0].length() + 1).replace("\"", ""));

            } else if ("show_dupes".equals(words[0]) && words.length == 1) {
                showDupes();

            } else if ("num_of_dupes".equals(words[0]) && words.length == 1) {
                numOfDupes();

            } else if ("delete_dupes_of".equals(words[0]) && words.length >= 2) {
                deleteDupesOf(line.substring(words[0].length() + 1).replace("\"", ""));

            } else if ("delete_dupes".equals(words[0]) && words.length == 1) {
                deleteDupes();

            } else {
                outputPrinter.printInvalidCommand(line);
            }
        }
    }

    private void buildIndex() {
        fileIndexer.buildIndex();
        fileIndexer.saveIndex();
    }

    private void calcHashes() throws IOException {
        hashCalculator.calculateHashes();
        fileIndexer.saveIndex();
    }

    private void showDupesOf(String path) {
        FileEntry info = (FileEntry) fileIndexer.getEntry(path);
        if (info != null) {
            Collection<FileEntry> dupes = dupeFinder.getDupesOf(info.getPath());
            outputPrinter.printDupesOf(info, dupes);
        } else {
            outputPrinter.println("Index doesn't contain " + path);
        }

    }

    private void showDupes() {
        Collection<List<FileEntry>> dupeEntries = dupeFinder.getDupeEntries();
        outputPrinter.printDupesOf(dupeEntries);
    }

    private void numOfDupes() {
        outputPrinter.println("Found " + dupeFinder.getNumOfDupes() + " dupes in index.");

    }

    private void deleteDupesOf(String path) {
        FileEntry info = (FileEntry) fileIndexer.getEntry(path);
        if (info == null) {
            outputPrinter.println("Index doesn't contain " + path);
            return;
        }
        Collection<FileEntry> dupes = dupeFinder.getDupesOf(info.getPath());

        dupeRemover.deleteDupes(dupes, info);
        fileIndexer.saveIndex();
    }

    private void deleteDupes() {
        dupeRemover.deleteAllDupes();
        fileIndexer.saveIndex();
    }
}
