package com.element54.sorter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Scanner;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.element54.sorter.tags.AudioTagService;
import com.element54.sorter.tags.impl.MP3agicTagService;
import com.element54.utils.file.FileNameCleaner;
import com.element54.utils.file.FilteredFileNameCleaner;
import com.element54.utils.file.PathUtils;
import com.element54.utils.string.FilteredStringCleaner;

public class Sorter {

    private static final CommandLineParser commandLineParser = new DefaultParser();

    public static void main(String[] args) {
        final Options options = new Options();
        options.addOption(Option.builder("i").longOpt("in").hasArg().desc("input folder").build());
        options.addOption(Option.builder("o").longOpt("out").hasArg().desc("output folder").build());
        options.addOption(Option.builder("h").longOpt("help").desc("this help page").build());
        try {
            final CommandLine line = commandLineParser.parse(options, args);
            run(line, options);
        } catch (final ParseException e) {
            System.err.println("Parsing failed.  Reason: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void run(CommandLine line, Options options) {
        if (line.hasOption("h")) {
            printHelp(options);
        } else {
            final boolean i = line.hasOption("i");
            final boolean o = line.hasOption("o");
            if (!(i && o)) {
                error("Missing in and/or output folder");
            } else {
                final Path in = Paths.get(line.getOptionValue("i"));
                final Path out = Paths.get(line.getOptionValue("o"));
                sort(in, out);
            }
        }
    }

    private static void sort(Path in, Path out) {
        System.out.println("Input folder: " + in.toAbsolutePath());
        System.out.println("Output folder: " + out.toAbsolutePath());
        if (!Files.exists(in) || !PathUtils.isDirectory(in)) {
            error("input folder does not exists or is not a directory.");
            return;
        } else if (!Files.exists(out) || !PathUtils.isDirectory(out)) {
            error("output folder does not exists or is not a directory.");
            return;
        }
        final AudioTagService audioTagService = new MP3agicTagService();
        final FileNameCleaner fileNameCleaner = new FilteredFileNameCleaner(new FilteredStringCleaner(30));
        final SorterService sorterService = new StructuredSorterService(audioTagService, fileNameCleaner);
        try {
            sortInt(in, out, false, sorterService);
            final Scanner scanner = new Scanner(System.in);
            System.out.println("Move files? (y|N)");
            final String line = scanner.nextLine();
            scanner.close();
            if (line.equalsIgnoreCase("y")) {
                sortInt(in, out, true, sorterService);
            }
        } catch (final IOException e) {
            error("IO error: " + e.getMessage());
            return;
        }
    }

    private static void sortInt(Path in, Path out, boolean move, SorterService sorterService) throws IOException {
        final Stream<Path> childs = Files.list(in).sorted();
        final Iterator<Path> it = childs.iterator();
        while (it.hasNext()) {
            final Path child = it.next();
            if (!Files.isDirectory(child)) {
                if (!PathUtils.isAudioFile(child)) {
                    continue;
                }
                try {
                    final Path dest = sorterService.getFile(child, out);
                    System.out.println(child.toAbsolutePath() + " -> " + dest.toAbsolutePath());
                    if (move) {
                        final Path parent = dest.getParent();

                        if (!Files.exists(parent)) {
                            Files.createDirectories(parent);
                        }
                        Files.move(child, dest);
                    }
                } catch (final SorterServiceException e) {
                    System.out.println(child.toAbsolutePath() + " FAILED: " + e.getMessage());
                }
            } else {
                sortInt(child, out, move, sorterService);
            }
        }
        childs.close();
    }

    private static void error(String msg) {
        System.err.println(msg);
        System.exit(1);
    }

    private static void printHelp(Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("sorter", "Simple helper to sort audio files according to metadata", options, "");
    }

}
