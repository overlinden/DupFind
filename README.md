DupFind
=======

DupFind - An easy to use file duplicate locator
Written by Oliver Verlinden (http://wps-verlinden.de)

General commands:
 -> help                      : Displays this help message
 -> exit                      : Close this application
 -> cd path                   : Navigates to the given directory
                                This directory is the base directory for the indexing and cleanup process
Indexing:
 -> build_index               : Build a new index or update an existing index in the current directory
 -> calc_hashes [num_threads] : Optionally extend the previously generated index with hash information of each file
                                The calculation time depends on the CPU/IO performance and the number/sizes
                                of the indexed files.
Searching:
 -> num_of_dupes              : Displays the total number of dupes
 -> show_dupes_of "path"      : Displays dupes of the file specified by "path"
 -> show_dupes                : Displays a list of all duplicate files within the indexed directory
 -> delete_dupes_of "path"    : Deletes all duplicates of the file specified by "path"
 -> delete_dupes              : Deletes all duplicate files within the indexed directory


Additional information:
When you only index a directory without calculating the hashes, the duplicate recognition only
depends on the file size. It's hightly recommended to calculate the hashes to decrease the risk
of false positives (files are shown as dupes, but they aren't). When the index includes the hashes
duplicates are recognized by size and hash. The risk of false positives is very low.

The indexing and hash calculating mechanism only processes newly created or changed files.
So the first initial run of "build_index" and "calc_hashes" will take much time. But the next runs
will be executed faster.


Usage example (Find all dupes in "D:\Images\" and remove them):
1) cd D:\Images
2) build_index
3) calc_hashes
4) num_of_dupes
5) show_dupes
6) delete_dupes
