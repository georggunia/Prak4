/**
 * @author mock
 * v 1.0
 * SoSe 2020
 */


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class ValgrindLineParser {
    public long getLineNumber() {
        return lineNumber;
    }

    private long lineNumber = 0;


    /**
     * Simple struct like class to encapsulate the data on a valgrind line.
     * For convenience, the fields are public but since they are final, that's ok.
     */
    public class ValgrindLine {
        public final char accessKind;
        public final long address;  // so that the 32 bit address is sure to fit we use long
        public final int size;

        public ValgrindLine(char accessKind, long address, int size) {
            this.accessKind = accessKind;
            this.address = address;
            this.size = size;
        }

        @Override
        public String toString() {
            return "ValgrindLine{" +
                    "accessKind=" + accessKind +
                    ", address=" + Long.toString(address,16) +
                    ", size=" + size +
                    '}';
        }
    }

    private BufferedReader reader = null;
    public ValgrindLineParser(String filename) {
        // try to open valgrind file
        try {
            reader = new BufferedReader(new FileReader(filename));
        } catch (Exception ex) {
            System.out.println("Could not open valgrind file at: " + filename + "\n Error was: " + ex);
            System.exit(-1);
        }
    }


    public ValgrindLine getNext() {
        String line = null;

        do {
            try {
                line = reader.readLine();
                lineNumber++;
            } catch (IOException ex) {
                System.out.println("Error while reading valgrind file " + ex);
                System.exit(-1);
            }
            if (line == null) {
                return null;
            }
            line = line.trim(); // get rid of leading or trailing whitespace
        } while (isComment(line));

        // got something, take it apart, first char is kind now
        char kind = line.charAt(0);
        line = line.substring(1).trim();  // remove the first char and trim
        // now expext address with , and size part
        String[] parts = line.split(",");
        if (parts.length != 2) {
            System.out.println("Found malformed line on line " + lineNumber);
            System.out.println(line);
            System.out.println("Exiting");
            System.exit(-1);
        }

        // we now have address and size of cache access in the two parts of 'parts'
        int size = 0;
        long address = 0;
        try {
            address = Long.parseLong(parts[0], 16);
            size = Integer.parseInt(parts[1]);
        } catch (NumberFormatException ex) {
            System.out.println("Error parsing numbers at line " + lineNumber);
            System.out.println(parts[0] + " , " + parts[1]);
            System.exit(-1);
        }



    return  new ValgrindLine(kind, address, size);
    }

    private boolean isComment(String line) {
        line = line.trim();
        return (('-' == line.charAt(0)) || ('=' == line.charAt(0)));
    }

}
