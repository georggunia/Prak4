/**
 * @author mock
 * v 1.0
 * SoSe 2020
 */

import java.util.HashSet;
import java.util.Set;

public class CacheSimulator {
    private int misscounter = 0;
    private int hitcounter = 0;
    private int evictioncounter = 0;
    private final int cacheLines;
    private final int associativity;
    private final int blockSize;
    private final boolean verbose;
    private final ValgrindLineParser valgrindParser;

    private final int[][] tags;
    private final boolean[][] validBits;
    private final long[][] accessTimes;

    CacheSimulator(int cacheLines, int associativity, int blockSize, String filename, boolean verbose, long[][] accesTimes) {
        this.cacheLines = cacheLines;
        this.associativity = associativity;
        this.blockSize = blockSize;
        this.verbose = verbose;
        this.valgrindParser = new ValgrindLineParser(filename);
        this.tags = new int[cacheLines][associativity];
        this.validBits = new boolean[cacheLines][associativity];
        this.accessTimes = new long[cacheLines][associativity];
        setup();  // Do some setup stuff before
    }

    private void setup() {
        // Initialize valid bits to false
        for (int i = 0; i < cacheLines; i++) {
            for (int j = 0; j < associativity; j++) {
                validBits[i][j] = false;
                tags[i][j] = 0;
                accessTimes[i][j] = 0;
            }
        }
        // Additional setup tasks
    }




    public void simulate() {
        ValgrindLineParser.ValgrindLine line = null;
        Set<Integer> sizes = new HashSet<Integer>();

        while ((line = valgrindParser.getNext()) != null) {
            long clock = valgrindParser.getLineNumber();  // we use the line number as a logical clock 
            sizes.add(line.size);
            switch (line.accessKind) {
                case 'L':
                    simulateLoad(line, clock);
                    break;
                case 'M':
                    simulateLoad(line, clock);
                    simulateStore(line, clock);
                    break;
                case 'S':
                    simulateStore(line, clock);
                    break;
                case 'I':
                    // nothing to do for D cache
                    break;
                default:
                    // hmm that should not happen
                    System.out.println("Don't know how to simulate access kind " + line.accessKind);
                    break;
            }

        }
        System.out.println("Successfully simulated " + valgrindParser.getLineNumber() + " valgrind lines");
        System.out.println("Access sizes in trace: ");
        for (int size : sizes) {
            System.out.print(size + " ");
        }
        System.out.println("");
        System.out.println("Hits: "+hitcounter+" Misses: "+misscounter+" Evictions: "+evictioncounter);
    }

    private int log2(int x) {
        int result = 0;
        while (x > 1) {
            result ++;
            x = x >> 1;
        }
        return result;
    }



    private void simulateStore(ValgrindLineParser.ValgrindLine line, long clock) {
        if (verbose) {
            System.out.print("store " + Long.toString(line.address, 16) + " " + line.size);
        }
        simulateAccess(line, clock);
    }

    private void simulateLoad(ValgrindLineParser.ValgrindLine line, long clock) {
        if (verbose) {
            System.out.print("load " + Long.toString(line.address, 16) + " " + line.size);
        }
        simulateAccess(line, clock);

    }

    public void simulateAccess(ValgrindLineParser.ValgrindLine line, long clock) {
        int index = (int) (line.address / blockSize) % cacheLines;
        int tag = (int) (line.address / (blockSize * cacheLines));

        boolean cacheHit = false;
        boolean cacheFull = true; // Flag to check if the cache is full
        int lruIndex = 0;
        // Check if the requested data is present in the cache (cache hit)
        for (int i = 0; i < associativity; i++) {
            if (validBits[index][i] && tags[index][i] == tag) {
                // Cache hit
                if (verbose) {
                    System.out.println("hit");
                }
                // Update cache statistics if needed
                cacheHit = true;
                hitcounter++;
                // Example: Update cache access time
                // cacheAccessTime[index][i] = clock;
                break;
            }
            if (validBits[index][i] == false) {
                cacheFull = false;
            }
        }

        // If cache miss, update the cache
        if (!cacheHit) {
            if (verbose) {
                System.out.println("miss");
            }
            // If the cache is full, perform cache eviction
            if (cacheFull) {
                // Perform cache eviction (replace the least recently used entry)
                evictioncounter++;
                if (verbose){
                    System.out.println("eviction");
                }
                // Replace the least recently used entry (or any other eviction policy)
                // Example: Update cache eviction policy
                // implementEvictionPolicy(index);
            }
            // Replace the cache entry with the new tag and set the valid bit
            tags[index][0] = tag;
            validBits[index][0] = true;
            // Example: Update cache miss counter
            misscounter++; // increment counter
        }

        System.out.println("Dumping Cache Contents:");
        for (int i = 0; i< cacheLines; i++) {
            System.out.print("index: "+i+": ");
            if (validBits[i][0]){
                System.out.println(tags[i][0]);
            }
            System.out.println();
        }
    }





}
