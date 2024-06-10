
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

    private final int[] tags;
    private final boolean[] validBits;


    CacheSimulator(int cacheLines, int associativity, int blockSize, String filename, boolean verbose) {
        this.cacheLines = 1 << cacheLines;
        this.associativity = associativity;
        this.blockSize = 1 << blockSize;

        this.verbose = verbose;
        this.valgrindParser = new ValgrindLineParser(filename);
        this.tags = new int[this.cacheLines];
        this.validBits = new boolean[this.cacheLines];
        setup();  // Do some setup stuff before
    }

    private void setup() {
        // Initialize valid bits to false
        for (int i = 0; i < this.cacheLines; i++) {
                validBits[i] = false;
                tags[i]= 0;
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
        System.out.println("Hits: " + hitcounter + " Misses: " + misscounter + " Evictions: " + evictioncounter);

        if (verbose) {
            System.out.println("Dumping Cache Contents:");
            for (int i = 0; i < log2(cacheLines); i++) {
                System.out.print("index " + i+": ");
                if (validBits[i]) {
                    System.out.print(tags[i]);
                }
                System.out.println("");
            }
        }
    }

    private int log2(int x) {
        int result = 0;
        while (x > 1) {
            result++;
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
        int offsetSize = log2(blockSize); // Calculate number of bits for offset
        int indexSize = log2(cacheLines); // Calculate number of bits for index

        long address = line.address; // The memory address to be accessed
        long endAddress = address + line.size - 1; // Calculate the end address of the memory access

        while (address <= endAddress) {
            int offsetMask = (1 << offsetSize) - 1; // Mask to extract the offset
            int indexMask = (1 << indexSize) - 1; // Mask to extract the index

            int offset = (int) (address & offsetMask); // Extract the offset
            int index = (int) ((address >> offsetSize) & indexMask); // Extract the index
            int tag = (int) (address >> (offsetSize + indexSize)); // Extract the tag

            boolean cacheHit = false; // Flag to check if the access is a hit

            // Check if the requested data is present in the cache (cache hit)
            if (validBits[index] && tags[index] == tag) {
                // Cache hit
                if (verbose) {
                    System.out.println(" hit");
                }
                // Update cache statistics
                cacheHit = true;
                hitcounter++;
            }

            // If cache miss, update the cache
            if (!cacheHit) {
                if (verbose) {
                    System.out.println(" miss");
                }

                // Cache miss
                misscounter++;

                // If the cache is full, perform cache eviction
                if (validBits[index]) {
                    evictioncounter++;
                    if (verbose) {
                        System.out.println(" eviction");
                    }
                }

                // Set the valid bit to true for the current cache entry
                validBits[index] = true;

                // Replace the cache entry with the new tag
                tags[index] = tag;
            }

            // Move to the next block if the address spans multiple blocks
            address = (address & ~offsetMask) + blockSize;
        }
    }



}



