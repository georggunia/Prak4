import java.util.HashSet;
import java.util.Set;

public class CacheSimulator {

    private final int cacheLines; // Number of cache lines
    private final int associativity; // Associativity (not used here because of direct image cache)
    private final int blockSize; // Size of a cache block
    private final String filename; // Name of the file with the memory accesses
    private final boolean verbose; // Verbose mode for verbose output
    private final ValgrindLineParser valgrindParser; // Parser for Valgrind output
    // Class for representing a cache line (with validity bit and tag)
    private class CacheLine {
        long tag; // Day of the cache block
        boolean valid; // Validity bit

        CacheLine() {
            valid = false; // Set initial validity to false
            day = -1; // Initial tag value
        }
    }

    private CacheLine[] cache; // Array to represent the cache
    private int hits = 0; // Cache hit counter
    private int misses = 0; // Cache miss counter
    private int evictions = 0; // Counter for cache evictions

    CacheSimulator(int cacheLines, int associativity, int blockSize, String filename, boolean verbose) {
        this.cacheLines = cacheLines;
        this.associativity = associativity;
        this.blockSize = blockSize;
        this.filename = filename;
        this.verbose = verbose;
        valgrindParser = new ValgrindLineParser(filename); // Initialize the Valgrind parser
        setup(); // Initialize the cache
    }

    private void setup() {
        cache = new CacheLine[cacheLines]; // Create an array of cache lines
        for (int i = 0; i < cacheLines; i++) {
            cache[i] = new CacheLine(); // Initialize each cache line
        }
    }

    public void simulate() {
    ValgrindLineParser.ValgrindLine line = null;
    Set<Integer> sizes = new HashSet<Integer>(); // Set to store different access sizes

        while ((line = valgrindParser.getNext()) != null) { // Read lines from the Valgrind file
            long clock = valgrindParser.getLineNumber(); // Use the line number as a logical clock
            sizes.add(line.size); // Add the access size to the set
            switch (line.accessKind) {
                case 'L':
                    simulateLoad(line, clock); // Simulate read access
                    break;
                case 'M':
                    simulateLoad(line, clock); // Simulate read access
                    simulateStore(line, clock); // Simulate write access
                    break;
                case 'S':
                    simulateStore(line, clock); // Simulate write access
                    break;
                case 'I':
                    // Nothing to do for the D cache
                    break;
                default:
                    // Shouldn't happen
                    System.out.println("Don't know how to simulate access kind " + line.accessKind);
                    break;
            }
        }

        System.out.println("Successfully simulated " + valgrindParser.getLineNumber() + " valgrind lines");
        System.out.println("Access sizes in trace: ");

        for (int size : sizes) {
            System.out.print(size + " "); // Output all different access sizes
        }

        System.out.println("");

        // Output the overall statistics
        System.out.println("hits: " + hits + " misses: " + misses + " evictions: " + evictions);

        // Output the contents of the cache when verbose mode is enabled
        if (verbose) {
            System.out.println("Dumping cache contents: ");
            for (int i = 0; i < cache.length; i++) {
                if (cache[i].valid) {
                    System.out.println("index: " + i + ": " + cache[i].tag);
                } else {
                    System.out.println("index: " + i + ": ");
                }
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
        simulateAccess(line, clock) // Simulate the memory access
    }

    private void simulateLoad(ValgrindLineParser.ValgrindLine line, long clock) {
        if (verbose) {
            System.out.print("load " + Long.toString(line.address, 16) + " " + line.size);
        }
        simulateAccess(line, clock); // Simulate memory access
    }

    private void simulateAccess(ValgrindLineParser.ValgrindLine line, long clock) {
        int indexBits = log2(cacheLines); // Number of bits for the cache index
        int blockOffsetBits = log2(blockSize); // Number of bits for the block offset

        long address = line.address; // The memory address of the access
        long endAddress = line.address + line.size - 1; // The end address of the access

        int startBlock = (int)(address/blockSize); // Calculate the starting block
        int endBlock = (int)(endAddress/blockSize); // Calculate the end block

        for (int block = startBlock; block <= endBlock; block++) { // Iterate over all affected blocks
            long blockAddress = block * blockSize; // Calculate the block address
            int index = (int) ((blockAddress >> blockOffsetBits) & ((1 << indexBits) - 1)); // Calculate the cache index
            long tag = blockAddress >> (blockOffsetBits + indexBits); // Calculate the tag

            CacheLine cacheLine = cache[index]; // Get the cache line with the calculated index
            boolean hit = cacheLine.valid && cacheLine.tag == tag; // Check whether the cache line is valid and whether the tag matches
            if (hit) {
                if (verbose)
                        System.out.println(" hit"); // If it's a hit,
                hits++; // Increase the number of hits
            } else {
                if (verbose)
                    System.out.println(" miss"); // If it's a miss,
                misses++; // Increase the miss number
                if (cacheLine.valid) {
                    if (verbose)
                        System.out.println(" eviction"); // If the cache line is valid,
                    evictions++; // Increase the eviction number
                }
                cacheLine.valid = true; // Set the cache line to valid
                cacheLine.tag = tag; // Set the cache line tag
                if (verbose)
                    System.out.println("updating index " + index + " with tag: " + tag); // Spend
            }
                if (verbose)
                    System.out.println(); // new line
        }
    }
}
