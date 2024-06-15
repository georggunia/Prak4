import java.util.HashSet;
import java.util.Set;

public class CacheSimulator {

    private final int cacheLines; // Anzahl der Cachezeilen
    private final int associativity; // Assoziativität (hier nicht verwendet, da direkter Abbildcache)
    private final int blockSize; // Größe eines Cacheblocks
    private final String filename; // Name der Datei mit den Speicherzugriffen
    private final boolean verbose; // Verbose-Modus für ausführliche Ausgaben
    private final ValgrindLineParser valgrindParser; // Parser für Valgrind-Ausgaben

    // Klasse zur Darstellung einer Cachezeile (mit Gültigkeitsbit und Tag)
    private class CacheLine {
        boolean valid; // Gültigkeitsbit
        long tag; // Tag des Cacheblocks

        CacheLine() {
            valid = false; // Initiale Gültigkeit auf false setzen
            tag = -1; // Initialer Tag-Wert
        }
    }

    private CacheLine[] cache; // Array zur Darstellung des Caches
    private int hits = 0; // Zähler für Cache-Hits
    private int misses = 0; // Zähler für Cache-Misses
    private int evictions = 0; // Zähler für Cache-Evictions

    CacheSimulator(int cacheLines, int associativity, int blockSize, String filename, boolean verbose) {
        this.cacheLines = cacheLines;
        this.associativity = associativity;
        this.blockSize = blockSize;
        this.filename = filename;
        this.verbose = verbose;
        valgrindParser = new ValgrindLineParser(filename); // Initialisiere den Valgrind-Parser
        setup(); // Initialisiere den Cache
    }

    private void setup() {
        cache = new CacheLine[cacheLines]; // Erstelle ein Array von Cachezeilen
        for (int i = 0; i < cacheLines; i++) {
            cache[i] = new CacheLine(); // Initialisiere jede Cachezeile
        }
    }

    public void simulate() {
        ValgrindLineParser.ValgrindLine line = null;
        Set<Integer> sizes = new HashSet<Integer>(); // Set zur Speicherung unterschiedlicher Zugriffgrößen

        while ((line = valgrindParser.getNext()) != null) { // Lese Zeilen aus der Valgrind-Datei
            long clock = valgrindParser.getLineNumber(); // Benutze die Zeilennummer als logische Uhr
            sizes.add(line.size); // Füge die Zugriffgröße zum Set hinzu
            switch (line.accessKind) {
                case 'L':
                    simulateLoad(line, clock); // Simuliere einen Lesezugriff
                    break;
                case 'M':
                    simulateLoad(line, clock); // Simuliere einen Lesezugriff
                    simulateStore(line, clock); // Simuliere einen Schreibzugriff
                    break;
                case 'S':
                    simulateStore(line, clock); // Simuliere einen Schreibzugriff
                    break;
                case 'I':
                    // Nichts zu tun für den D-Cache
                    break;
                default:
                    // Sollte nicht passieren
                    System.out.println("Don't know how to simulate access kind " + line.accessKind);
                    break;
            }
        }
        System.out.println("Successfully simulated " + valgrindParser.getLineNumber() + " valgrind lines");
        System.out.println("Access sizes in trace: ");
        for (int size : sizes) {
            System.out.print(size + " "); // Gebe alle unterschiedlichen Zugriffgrößen aus
        }
        System.out.println("");
        // Gebe die Gesamtstatistik aus
        System.out.println("hits: " + hits + " misses: " + misses + " evictions: " + evictions);

        // Gebe den Inhalt des Caches aus, wenn der verbose-Modus aktiviert ist
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
        while (x > 1) { // Bestimme den Logarithmus zur Basis 2
            result++;
            x = x >> 1; // Rechne x = x / 2
        }
        return result;
    }

    private void simulateStore(ValgrindLineParser.ValgrindLine line, long clock) {
        if (verbose) {
            System.out.print("store " + Long.toString(line.address, 16) + " " + line.size);
        }
        simulateAccess(line, clock); // Simuliere den Speicherzugriff
    }

    private void simulateLoad(ValgrindLineParser.ValgrindLine line, long clock) {
        if (verbose) {
            System.out.print("load " + Long.toString(line.address, 16) + " " + line.size);
        }
        simulateAccess(line, clock); // Simuliere den Speicherzugriff
    }

    private void simulateAccess(ValgrindLineParser.ValgrindLine line, long clock) {
        int blockOffsetBits = log2(blockSize); // Anzahl der Bits für den Blockoffset
        int indexBits = log2(cacheLines); // Anzahl der Bits für den Cacheindex

        long address = line.address; // Die Speicheradresse des Zugriffs
        long endAddress = line.address + line.size - 1; // Die Endadresse des Zugriffs

        int startBlock = (int) (address / blockSize); // Berechne den Startblock
        int endBlock = (int) (endAddress / blockSize); // Berechne den Endblock

        for (int block = startBlock; block <= endBlock; block++) { // Iteriere über alle betroffenen Blöcke
            long blockAddress = block * blockSize; // Berechne die Blockadresse
            // int blockOffset = (int) (blockAddress & ((1 << blockOffsetBits) - 1)); //
            // Berechne den BlockOffset (keine weitere Verwendung hier)
            int index = (int) ((blockAddress >> blockOffsetBits) & ((1 << indexBits) - 1)); // Berechne den Cacheindex
            long tag = blockAddress >> (blockOffsetBits + indexBits); // Berechne den Tag

            CacheLine cacheLine = cache[index]; // Hole die Cachezeile mit dem berechneten Index
            boolean hit = cacheLine.valid && cacheLine.tag == tag; // Prüfe, ob die Cachezeile gültig ist und ob der Tag
            // übereinstimmt
            if (hit) {
                if (verbose)
                    System.out.println(" hit"); // Wenn es ein Hit ist,
                hits++; // Erhöhe die Hit-Zahl
            } else {
                if (verbose)
                    System.out.println(" miss"); // Wenn es ein Miss ist,
                misses++; // Erhöhe die Miss-Zahl
                if (cacheLine.valid) {
                    if (verbose)
                        System.out.println(" eviction"); // Wenn die Cachezeile gültig ist,
                    evictions++; // Erhöhe die Eviction-Zahl
                }
                cacheLine.valid = true; // Setze die Cachezeile auf gültig
                cacheLine.tag = tag; // Setze den Tag der Cachezeile
                if (verbose)
                    System.out.println("updating index " + index + " with tag: " + tag); // Ausgeben
            }
            if (verbose)
                System.out.println(); // neue Zeile printen
        }
    }
}
