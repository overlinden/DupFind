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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DupFind {

	public static void main(String[] args) {

		FileIndexer fi = new FileIndexer();
		HashCalculator hc = new HashCalculator(null);
		DupeFinder df = new DupeFinder(null);
		DupeRemover dr = new DupeRemover(null, null);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line = "";
		String[] words;

		System.out.println("DupFind 1.0 - written by Oliver Verlinden (http://wps-verlinden.de)");
		System.out.println("Type \"help\" to display usage information");
		try {
			while (true) {
				System.out.print(fi.pwd());
				line = br.readLine();
				if ("exit".equals(line)) {
					break;
				}
				words = line.split(" ");
				if ("help".equals(words[0])) {
					printHelp();
				} else if ("cd".equals(words[0]) && words.length >= 2) {
					fi.cd(line.substring(words[0].length() + 1).replace("\"", ""));
					if (fi.loadIndex()) {
						df = new DupeFinder(fi.getIndex());
						dr = new DupeRemover(df, fi.getIndex());
					} else {
						df = new DupeFinder(null);
						dr = new DupeRemover(df, null);
					}

				} else if ("build_index".equals(words[0]) && words.length == 1) {
					boolean initialBuild = (fi.getIndex() != null);
					fi.buildIndex();
					if (initialBuild) {
						df = new DupeFinder(fi.getIndex());
						dr = new DupeRemover(df, fi.getIndex());
					}
					fi.saveIndex();
				} else if ("calc_hashes".equals(words[0]) && words.length >= 1 && words.length <= 2) {
					if (fi.getIndex() == null) {
						System.out.println("Please build index first.");
					} else {
						hc = new HashCalculator(fi.getIndex());
						int numThreads = (words.length == 2 ? Integer.parseInt(words[1]) : Runtime.getRuntime().availableProcessors());
						hc.calculateHashes(numThreads);
						fi.saveIndex();
					}
				} else if ("show_dupes_of".equals(words[0]) && words.length >= 2) {
					if (fi.getIndex() == null) {
						System.out.println("Please build index first.");
					} else {
						df.showDupesOf(fi.pwd(), line.substring(words[0].length() + 1).replace("\"", ""));
					}
				} else if ("show_dupes".equals(words[0]) && words.length == 1) {
					if (fi.getIndex() == null) {
						System.out.println("Please build index first.");
					} else {
						df.showDupes();
					}
				} else if ("num_of_dupes".equals(words[0]) && words.length == 1) {
					if (fi.getIndex() == null) {
						System.out.println("Please build index first.");
					} else {
						df.showNumOfDupes();
					}
				} else if ("delete_dupes_of".equals(words[0]) && words.length >= 2) {
					if (fi.getIndex() == null) {
						System.out.println("Please build index first.");
					} else {
						dr.deleteDupesOf(fi.pwd(), line.substring(words[0].length() + 1).replace("\"", ""));
						fi.saveIndex();
					}

				} else if ("delete_dupes".equals(words[0]) && words.length == 1) {
					if (fi.getIndex() == null) {
						System.out.println("Please build index first.");
					} else {
						dr.deleteDupes();
						fi.saveIndex();
					}
				} else {
					System.out.println("Invaid command: " + line);
					System.out.println("Type \"help\" to display usage information");
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void printHelp() {
		System.out.println("DupFind 1.0 - written by Oliver Verlinden (http://wps-verlinden.de)\n");
		System.out.println("General commands:");
		System.out.println(" -> help                      : Displays this help message");
		System.out.println(" -> exit                      : Close this application");
		System.out.println(" -> cd path                   : Navigates to the given directory");
		System.out.println("                                This directory is the base directory for the indexing and cleanup process");
		System.out.println("Indexing:");
		System.out.println(" -> build_index               : Build a new index or update an existing index in the current directory");
		System.out.println(" -> calc_hashes [num_threads] : Optionally extend the previously generated index with hash information of each file");
		System.out.println("                                The calculation time depends on the CPU/IO performance and the number/sizes");
		System.out.println("                                of the indexed files.");
		System.out.println("Searching:");
		System.out.println(" -> num_of_dupes              : Displays the total number of dupes");
		System.out.println(" -> show_dupes_of \"path\"      : Displays dupes of the file specified by \"path\"");
		System.out.println(" -> show_dupes                : Displays a list of all duplicate files within the indexed directory");
		System.out.println(" -> delete_dupes_of \"path\"    : Deletes all duplicates of the file specified by \"path\"");
		System.out.println(" -> delete_dupes              : Deletes all duplicate files within the indexed directory");
		System.out.println();
		System.out.println();
		System.out.println("Additional information:");
		System.out.println("When you only index a directory without calculating the hashes, the duplicate recognition only");
		System.out.println("depends on the file size. It's hightly recommended to calculate the hashes to decrease the risk");
		System.out.println("of false positives (files are shown as dupes, but they aren't). When the index includes the hashes");
		System.out.println("duplicates are recognized by size and hash. The risk of false positives is very low.");
		System.out.println();
		System.out.println("The indexing and hash calculating mechanism only processes newly created or changed files.");
		System.out.println("So the first initial run of \"build_index\" and \"calc_hashes\" will take much time. But the next runs");
		System.out.println("will be executed faster.");
		System.out.println();
		System.out.println();
		System.out.println("Usage example (Find all dupes in \"D:\\Images\\\" and remove them):");
		System.out.println("1) cd D:\\Images");
		System.out.println("2) build_index");
		System.out.println("3) calc_hashes");
		System.out.println("4) num_of_dupes");
		System.out.println("5) show_dupes");
		System.out.println("6) delete_dupes");
	}
}