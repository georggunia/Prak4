/**
 * @author mock
 * v 1.0
 * SoSe 2020
 */

import org.apache.commons.cli.*;

public class Main {
    private static String filename;
    private static int cacheLines = 1024;
    private static int associativity = 1;
    private static int blockSize = 16;
    private static boolean isVerbose = false;
    private static boolean showHelp = false;

    public static void main(String[] args) {

        parseArgs(args);
        if (showHelp) {
            System.out.println("Some help message");
            System.exit(0);
        }

        System.out.println("Running cache simulation with trace file " + filename);
        System.out.println("Cache lines: " + cacheLines);
        System.out.println("Block size in bytes: " + blockSize);
        System.out.println("Associativity: " + associativity);
        System.out.println("Simulation starting...");


        CacheSimulator simulator = new CacheSimulator(cacheLines, associativity, blockSize, filename, isVerbose);
        simulator.simulate();

        System.out.println("Simulation finished.");
    }

    private static void parseArgs(String[] args) {
        Options options = new Options();
        options.addOption(new Option("s", true,
                "Number of cache lines bits, i.e. 2^s is the number of cache lines"));
        options.addOption(new Option("E", true, "Associativity, must be a power of 2"));
        options.addOption(new Option("b", true,
                "Number of bits used for cache block size in bytes, i.e. 2^b is the cache block" +
                        "size in bytes"));
        options.addOption(new Option("t",true, "File name of trace file to simulate"));
        options.addOption(new Option("h", false, "Print this usage message"));
        options.addOption(new Option("v", false, "Generate verbose simulation output"));
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            filename = cmd.getOptionValue("t");
            cacheLines = sizeFromBitArg(cmd.getOptionValue("s"));
            blockSize = sizeFromBitArg(cmd.getOptionValue("b"));
            associativity = intValFromArg(cmd.getOptionValue("E"));
            isVerbose = cmd.hasOption("v");
            showHelp = cmd.hasOption("h");
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("CacheSimulator", options);System.exit(1);
        }

        if (associativityIsBad(associativity)) {
            System.out.println("Associativity must be a power of 2! .., exiting");
            System.exit(-1);
        }

    }

    private static boolean associativityIsBad(int associativity) {
        return (associativity & (associativity -1)) != 0;  // neat hack to quickly test for power of 2
    }

    private static int intValFromArg(String s) {
        int value = 0;
        try {
            value = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            System.out.println("Integer expected for argument");
            System.exit(-1);
        }
        return value;
    }

    private static int sizeFromBitArg(String b) {
        return 1 << intValFromArg(b);  // turn bits into number
    }



}
