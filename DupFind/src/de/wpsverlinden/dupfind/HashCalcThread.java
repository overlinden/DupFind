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
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HashCalcThread extends Thread {

	private final static int MB_PER_DOT = 250;
	
	ConcurrentLinkedQueue<FileEntry> workQueue;

	public HashCalcThread(ConcurrentLinkedQueue<FileEntry> workQueue) {
		this.workQueue = workQueue;
	}

    @Override
	public void run() {
		FileEntry current;
		File file;
		int fileSizeMB;
		int cntMB = 0;
		while (true) {
			current = workQueue.poll();
			if (current == null) {
				break;
			}

			file = new File(current.getPath());
			if (current.getHash().isEmpty() || current.getLastModified() < file.lastModified()) {
				try {
					fileSizeMB = (int)file.length() / (1024 * 1024);
					current.setHash(calcHash(current.getPath()));
					cntMB += (fileSizeMB >= 1 ? fileSizeMB : 1);
					if (cntMB >= MB_PER_DOT) {
						cntMB = 0;
						System.out.print(".");
					}
				} catch (Exception e) {
					System.out.println("Error calculating hash for " + current.getPath());
				}
			}
		}
	}

	private byte[] calcChecksum(String path) throws Exception {
		byte[] buffer = new byte[1024];
		MessageDigest digest = MessageDigest.getInstance("MD5");
		int numRead;
		byte[] hash = null;
		InputStream fis;
		File file = new File(path);
		if (file.exists() && file.isFile()) {
			fis = new FileInputStream(file);
			do {
				numRead = fis.read(buffer);
				if (numRead > 0) {
					digest.update(buffer, 0, numRead);
				}
			} while (numRead != -1);

			fis.close();
			hash = digest.digest();
		}
		return hash;
	}

	public String calcHash(String path) throws Exception {
		byte[] b = calcChecksum(path);
		String result = "";

		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}
}
