/* This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://sam.zoy.org/wtfpl/COPYING for more details. */
/* If you do modify it or use my format please let me know, and it'd be nice
 * to get some credit ;) */
package imbue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

/**
 *
 * @author Tiko
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static final float VERSION = 0.03f;

    public static void main(String[] args) {
        System.out.println("imbue " + VERSION + " by Tiko");
        try {
            if (args.length < 1) {
                MainFrame.main(args);
            } else if (args.length == 1 && args[0].equals("-help")) {
                showHelp();
            } else if (args.length == 2 && args[0].equals("-check")) {
                Image img = new Image(new File(args[1]));
                img.check();
                System.out.println("Done.");
            } else if (args.length == 2 && args[0].equals("-decode")) {
                System.out.println("Decoding " + args[1] + "");
                Image img = new Image(new File(args[1]));
                img.decode(null);
            } else if (args.length == 3 && args[0].equals("-decode")) {
                System.out.println("Decoding " + args[1] + ", writing to " + args[2]);
                Image img = new Image(new File(args[1]));
                img.decode(new File(args[2]));
                System.out.println("Done.");
            } else if (args.length == 4 && args[0].equals("-encode")) {
                System.out.println("Encoding " + args[2] + " within " + args[1] + ", writing to " + args[3]);
                Image img = new Image(new File(args[1]));
                img.writeFile(new File(args[2]));
                img.save(new File(args[3]));
                System.out.println("Done.");
            } else {
                showHelp();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* private static String[] realArgs(String[] args) {
        ArrayList<String> arr = new ArrayList<String>(args.length);
        StringBuilder realcmd = new StringBuilder();
        boolean first = true;
        for (String s : args) {
            if (!first) {
                realcmd.append(' ');
            }
            realcmd.append(s);
            first = false;
        }
        String cmdline = realcmd.toString();
        boolean in_quote = false;
        char last_c = 'A';
        String curr = "";
        for (int i = 0; i < cmdline.length(); i++) {
            char c = cmdline.charAt(i);
            if (in_quote) {
                if (c == '"') {
                    in_quote = false;
                } else {
                    curr += c;
                }
            } else {
                if (last_c != '\\') {
                    if (c == '"') {
                        in_quote = true;
                    } else if (c == ' ') {
                        arr.add(curr);
                        curr = "";
                    } else if (c != '\\') {
                        curr += c;
                    }
                } else {
                    curr += c;
                }
            }
            last_c = c;
        }
        if (curr.length() > 0) {
            arr.add(curr);
        }
        return arr.toArray(new String[0]);
    } */

    private static void showHelp() {
        System.out.println("  imbue takes a file and hides it in the transparency of an image.");
        System.out.println("  Arguments: ");
        System.out.println("    -check file.png");
        System.out.println("      Checks an image's storage capacity. If the image is already imbued,");
        System.out.println("      displays the original filename, length, CRC.");
        System.out.println("    -encode host.png whatever.ext out.png");
        System.out.println("      Encodes whatever.ext within host.png and writes a new file out.png.");
        System.out.println("    -decode file.png [out.png]");
        System.out.println("      Decodes file.png to out.png if specified, original filename otherwise.");
        System.out.println("    Running it with no arguments opens the GUI.");
        System.out.println("  http://tiko.be/imbue/");
    }

	/* i copied and pasted this from some java help site lmao */
    public static long getCRC(File fileName) {

        try {

            CheckedInputStream cis = null;
            long fileSize = 0;
            try {
                // Computer CRC32 checksum
                cis = new CheckedInputStream(
                        new FileInputStream(fileName), new CRC32());

                fileSize = fileName.length();

            } catch (FileNotFoundException e) {
                System.err.println("File not found.");
                return -1;
            }

            byte[] buf = new byte[128];
            while (cis.read(buf) >= 0) {
            }

            long checksum = cis.getChecksum().getValue();
            return checksum;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
