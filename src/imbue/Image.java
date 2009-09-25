/* This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://sam.zoy.org/wtfpl/COPYING for more details. */
/* If you do modify it or use my format please let me know, and it'd be nice
 * to get some credit ;) */
package imbue;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Tiko
 */
public class Image extends OutputStream {

    BufferedImage img;
    ArrayList<Point> transparency;
    DataOutputStream dos;
    int idx = 0;
    int sub_idx = 0;
    File file;

    public Image(File f) {
        try {
            img = ImageIO.read(f);
            file = f;
            setup();
        } catch (IOException ex) {
            Logger.getLogger(Image.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setup() {
        transparency = new ArrayList<Point>();
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                Color c = new Color(img.getRGB(x, y), true);
                if (c.getAlpha() == 0) {
                    transparency.add(new Point(x, y));
                }
            }
        }
        dos = new DataOutputStream(this);
    }

    public BufferedImage getImage() {
        return img;
    }

    public File getFile() {
        return file;
    }

    class ImageInputStream extends InputStream {
        int r_idx = 0;
        int r_sub_idx = 0;

        public int read() throws IOException {
            Point p = transparency.get(r_idx);
            Color c = new Color(img.getRGB(p.x, p.y), true);
            int ret = -1;
            switch (r_sub_idx) {
                case 0:
                    ret = c.getRed();
                    break;
                case 1:
                    ret = c.getGreen();
                    break;
                case 2:
                    ret = c.getBlue();
                    break;
            }
            r_sub_idx = ++r_sub_idx % 3;
            if (r_sub_idx == 0) {
                r_idx++;
            }
            return ret;
        }
    };

    public class Metadata {
        String embeddedFileName;
        long size;
        long CRC;
        public Metadata(String efn, long s, long crc) {
            embeddedFileName = efn; size = s; CRC = crc;
        }
    }

    public DataOutputStream getOutputStream() {
        return dos;
    }

    public void write(byte[] b) {
        for (byte a : b) {
            write(a);
        }
    }

    public void write(int i) {
        int b = (i & 0xFF);
        Point p = transparency.get(idx);
        Color oldc = new Color(img.getRGB(p.x, p.y), true);
        Color newc;
        switch (sub_idx) {
            case 0:
                newc = new Color(b, 0, 0, 0);
                break;
            case 1:
                newc = new Color(oldc.getRed(), b, 0, 0);
                break;
            case 2:
                newc = new Color(oldc.getRed(), oldc.getGreen(), b, 0);
                break;
            default:
                newc = null;
        }
        img.setRGB(p.x, p.y, color2Int(newc));
        sub_idx = ++sub_idx % 3;
        if (sub_idx == 0) {
            idx++;
        }
    }

    public void writeFile(File f) throws IOException {
        //rewind
        idx = 0;
        sub_idx = 0;

        long t_size = 4 + 1 + f.getName().length() + 8 + 8 + f.length();
        if (t_size > getCapacity()) {
            throw new IOException("Data too big: need " + (t_size - getCapacity()) + " more bytes!");
        }
        //put header: 4
        dos.writeBytes("TIKO");
        //put version: 1
        dos.writeByte(1);
        //put filename: varies
        dos.writeUTF(f.getName());
        //put CRC: 8
        dos.writeLong(Main.getCRC(f));
        //put flength: 8
        dos.writeLong(f.length());
        //put data
        FileInputStream fos = new FileInputStream(f);
        while (fos.available() > 0) {
            dos.write(fos.read());
        }
    }

    public void save(File f) throws IOException {
        ImageIO.write(img, "png", f);
        file = f;
    }

    public void decode(File f) throws IOException {
        DataInputStream dis = new DataInputStream(new ImageInputStream());
        byte[] header = new byte[4];
        dis.readFully(header);
        if (!new String(header).equals("TIKO")) {
            throw new IOException("Unrecognized header! " + new String(header));
        }
        int version = dis.read();
        if (version != 1) {
            throw new IOException("Image uses unknown encoding version: " + version + "\nTry updating?");
        }
        String filename = dis.readUTF();
        if (f == null) {
            f = new File(filename);
        }
        System.out.println("Original filename: " + filename);
        long CRC = dis.readLong();
        long len = dis.readLong();
        FileOutputStream fos = new FileOutputStream(f);
        for (int pos = 0; pos < len; pos++) {
            fos.write(dis.read());
        }
        fos.flush();
        if (Main.getCRC(f) != CRC) {
            throw new IOException("Warning! CRC doesn't match, image is corrupted?");
        } else {
            System.out.println("CRC OK.");
        }
        System.out.println("Decoded as " + f.getName());
    }

    public Metadata check() throws IOException {
        DataInputStream dis = new DataInputStream(new ImageInputStream());
        byte[] header = new byte[4];
        dis.readFully(header);
        if (!new String(header).equals("TIKO")) {
            System.out.println("Doesn't seem to be an imbued file.");
            System.out.println("Capacity: " + getCapacity() + "B.");
            return null;
        }
        int version = dis.read();
        String filename = dis.readUTF();
        long CRC = dis.readLong();
        long len = dis.readLong();

        System.out.println("File is imbued.");
        System.out.println("Format version: " + version);
        System.out.println("Original filename: " + filename);
        System.out.println("CRC: " + CRC);
        System.out.println("Embedded file size: " + len + "B");
        System.out.println("Capacity: " + getCapacity() + "B (" + ((int)len / (float)getCapacity() * 100) + "% used)");
        
        return new Metadata(filename, len, CRC);
    }


    public int getCapacity() {
        return transparency.size() * 3;
    }

    public static int color2Int(Color c) {
        return ((c.getAlpha() & 0xFF) << 24) |
                ((c.getRed() & 0xFF) << 16) |
                ((c.getGreen() & 0xFF) << 8) |
                ((c.getBlue() & 0xFF) << 0);
    }
}
