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

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Required;

public class OutputPrinter {

    private PrintStream outputStream;

    @Required
    public void setOutputStream(PrintStream outputStream) {
        this.outputStream = outputStream;
    }

    public void printHelp() {
        println("DupFind 2.1 - written by Oliver Verlinden (http://wps-verlinden.de)\n");
        println("General commands:");
        println(" -> help                      : Displays this help message");
        println(" -> exit                      : Close this application");
        println("Indexing:");
        println(" -> build_index               : Build a new index or update an existing index in the current directory");
        println(" -> calc_hashes               : Optionally extend the previously generated index with hash information of each file");
        println("                                The calculation time depends on the CPU/IO performance and the number/sizes");
        println("                                of the indexed files.");
        println("Searching:");
        println(" -> num_of_dupes              : Displays the total number of dupes");
        println(" -> show_dupes_of \"path\"      : Displays dupes of the file specified by \"path\"");
        println(" -> show_dupes                : Displays a list of all duplicate files within the indexed directory");
        println("Cleanup:");
        println(" -> delete_dupes_of \"path\"    : Deletes all duplicates of the file specified by \"path\"");
        println(" -> delete_dupes              : Deletes all duplicate files within the indexed directory");
        println("");
        println("");
        println("Additional information:");
        println("When you only index a directory without calculating the hashes, the duplicate recognition only");
        println("depends on the file size. It's hightly recommended to calculate the hashes to decrease the risk");
        println("of false positives (files are shown as dupes, but they aren't). When the index includes the hashes");
        println("duplicates are recognized by size and hash. The risk of false positives is very low.");
        println("");
        println("The indexing and hash calculating mechanism only processes newly created or changed files.");
        println("So the first initial run of \"build_index\" and \"calc_hashes\" will take much time. But the next runs");
        println("will be executed faster.");
        println("");
        println("");
        println("Usage example (Find all dupes in \"D:\\Images\\\" and remove them):");
        println("1) Navigate to \"D:\\Images\\\" directory");
        println("2) Start dupfind via \"java -jar DupFind.jar\"");
        println("3) build index");
        println("4) calc_hashes");
        println("5) num_of_dupes");
        println("6) show_dupes");
        println("7) delete_dupes");
    }

    void printSplash() {
        println("DupFind 2.1 - written by Oliver Verlinden (http://wps-verlinden.de)");
        println("Type \"help\" to display usage information");
    }

    void printInvalidCommand(String line) {
        println("Invaid command: " + line);
        println("Type \"help\" to display usage information");
    }

    void print(String message) {
        if (outputStream != null) {
            outputStream.print(message);
        }
    }

    void println(String message) {
        if (outputStream != null) {
            outputStream.println(message);
        }
    }

    void printDupesOf(FileEntry info, Collection<FileEntry> dupes) {
        if (dupes.size() > 0) {
            println("-----\n" + info);
            dupes.stream().forEach((e) -> println(e.toString()));
            println("-----\n");
        } else {
            println("No dupes found.");
        }
    }

    void printDupesOf(Collection<List<FileEntry>> dupeEntries) {
        dupeEntries.stream()
                .filter((e) -> e.size() >= 2)
                .forEach((lst) -> {
                    println("-----");
                    while (!lst.isEmpty()) {
                        println(lst.get(0).toString());
                        lst.remove(0);
                    }
                    println("-----\n");
                });
    }
}
