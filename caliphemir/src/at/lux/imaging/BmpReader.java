package at.lux.imaging;
/*
 * This file is part of Caliph & Emir.
 *
 * Caliph & Emir is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Caliph & Emir is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Caliph & Emir; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2006 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://caliph-emir.sourceforge.net
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reading BMP files ...
 */

public class BmpReader {

    public static int constructInt(byte[] in, int offset) {
        int ret = ((int) in[offset + 3] & 0xff);
        ret = (ret << 8) | ((int) in[offset + 2] & 0xff);
        ret = (ret << 8) | ((int) in[offset + 1] & 0xff);
        ret = (ret << 8) | ((int) in[offset + 0] & 0xff);
        return (ret);
    }

    public static int constructInt3(byte[] in, int offset) {
        int ret = 0xff;
        ret = (ret << 8) | ((int) in[offset + 2] & 0xff);
        ret = (ret << 8) | ((int) in[offset + 1] & 0xff);
        ret = (ret << 8) | ((int) in[offset + 0] & 0xff);
        return (ret);
    }

    public static long constructLong(byte[] in, int offset) {
        long ret = ((long) in[offset + 7] & 0xff);
        ret |= (ret << 8) | ((long) in[offset + 6] & 0xff);
        ret |= (ret << 8) | ((long) in[offset + 5] & 0xff);
        ret |= (ret << 8) | ((long) in[offset + 4] & 0xff);
        ret |= (ret << 8) | ((long) in[offset + 3] & 0xff);
        ret |= (ret << 8) | ((long) in[offset + 2] & 0xff);
        ret |= (ret << 8) | ((long) in[offset + 1] & 0xff);
        ret |= (ret << 8) | ((long) in[offset + 0] & 0xff);
        return (ret);
    }

    public static double constructDouble(byte[] in, int offset) {
        long ret = constructLong(in, offset);
        return (Double.longBitsToDouble(ret));
    }

    public static short constructShort(byte[] in, int offset) {
        short ret = (short) ((short) in[offset + 1] & 0xff);
        ret = (short) ((ret << 8) | (short) ((short) in[offset + 0] & 0xff));
        return (ret);
    }

    public static BufferedImage read(InputStream in) {
        try {
            BitmapHeader bh = new BitmapHeader();
            bh.read(in);
            Image img = null;
            if (bh.nbitcount == 24) img = (readMap24(in, bh));

            if (bh.nbitcount == 32) img = (readMap32(in, bh));

            if (bh.nbitcount == 8) img = (readMap8(in, bh));

            in.close();
            return ImagingTools.toBufferedImage(img);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return (null);
    }

    protected static Image readMap32(InputStream in, BitmapHeader bh) throws IOException {
        Image image;
        // No Palatte data for 24-bit format but scan lines are
        // padded out to even 4-byte boundaries.
        int xwidth = bh.nsizeimage / bh.nheight;
        int ndata[] = new int[bh.nheight * bh.nwidth];
        byte brgb[] = new byte[bh.nwidth * 4 * bh.nheight];
        in.read(brgb, 0, bh.nwidth * 4 * bh.nheight);
        int nindex = 0;
        for (int j = 0; j < bh.nheight; j++) {
            for (int i = 0; i < bh.nwidth; i++) {
                ndata[bh.nwidth * (bh.nheight - j - 1) + i] = constructInt3(brgb, nindex);
                nindex += 4;
            }
        }

        image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(bh.nwidth, bh.nheight,
                ndata, 0, bh.nwidth));
        in.close();
        return (image);
    }

    protected static Image readMap24(InputStream inputStream, BitmapHeader bh) throws IOException {
        Image image;
        // No Palatte data for 24-bit format but scan lines are
        // padded out to even 4-byte boundaries.
        int npad = (bh.nsizeimage / bh.nheight) - bh.nwidth * 3;
        int ndata[] = new int[bh.nheight * bh.nwidth];
        byte brgb[] = new byte[(bh.nwidth + npad) * 3 * bh.nheight];
        inputStream.read(brgb, 0, (bh.nwidth + npad) * 3 * bh.nheight);
        int nindex = 0;
        for (int j = 0; j < bh.nheight; j++) {
            for (int i = 0; i < bh.nwidth; i++) {
                ndata[bh.nwidth * (bh.nheight - j - 1) + i] = constructInt3(brgb, nindex);
                nindex += 3;
            }
            nindex += npad;
        }
        image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(bh.nwidth, bh.nheight,
                ndata, 0, bh.nwidth));
        inputStream.close();
        return (image);

    }

    protected static Image readMap8(InputStream inputStream, BitmapHeader bh) throws IOException {
        Image image;
        int nNumColors = 0;
        if (bh.nclrused > 0) {
            nNumColors = bh.nclrused;
        } else {
            nNumColors = (1 & 0xff) << bh.nbitcount;

        }
        if (bh.nsizeimage == 0) {
            bh.nsizeimage = ((((bh.nwidth * bh.nbitcount) + 31) & ~31) >> 3);
            bh.nsizeimage *= bh.nheight;
        }
        int npalette[] = new int[nNumColors];
        byte bpalette[] = new byte[nNumColors * 4];
        inputStream.read(bpalette, 0, nNumColors * 4);
        int nindex8 = 0;
        for (int n = 0; n < nNumColors; n++) {
            npalette[n] = constructInt3(bpalette, nindex8);
            nindex8 += 4;
        }
        int npad8 = (bh.nsizeimage / bh.nheight) - bh.nwidth;
        int ndata8[] = new int[bh.nwidth * bh.nheight];
        byte bdata[] = new byte[(bh.nwidth + npad8) * bh.nheight];
        inputStream.read(bdata, 0, (bh.nwidth + npad8) * bh.nheight);
        nindex8 = 0;
        for (int j8 = 0; j8 < bh.nheight; j8++) {
            for (int i8 = 0; i8 < bh.nwidth; i8++) {
                ndata8[bh.nwidth * (bh.nheight - j8 - 1) + i8] =
                        npalette[((int) bdata[nindex8] & 0xff)];
                nindex8++;
            }
            nindex8 += npad8;
        }
        image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(bh.nwidth, bh.nheight,
                ndata8, 0, bh.nwidth));
        return (image);
    }


    static class BitmapHeader {
        public int nsize;
        public int nbisize;
        public int nwidth;
        public int nheight;
        public int nplanes;
        public int nbitcount;
        public int ncompression;
        public int nsizeimage;
        public int nxpm;
        public int nypm;
        public int nclrused;
        public int nclrimp;

        // read in the bitmap header
        public void read(InputStream inputStream) throws IOException {
            final int bflen = 14;  // 14 byte BITMAPFILEHEADER
            byte bf[] = new byte[bflen];
            inputStream.read(bf, 0, bflen);
            final int bilen = 40; // 40-byte BITMAPINFOHEADER
            byte bi[] = new byte[bilen];
            inputStream.read(bi, 0, bilen);
            nsize = constructInt(bf, 2);
            nbisize = constructInt(bi, 2);
            nwidth = constructInt(bi, 4);
            nheight = constructInt(bi, 8);
            nplanes = constructShort(bi, 12); //(((int)bi[13]&0xff)<<8) | (int)bi[12]&0xff;
            nbitcount = constructShort(bi, 14); //(((int)bi[15]&0xff)<<8) | (int)bi[14]&0xff;
            ncompression = constructInt(bi, 16);
            nsizeimage = constructInt(bi, 20);
            nxpm = constructInt(bi, 24);
            nypm = constructInt(bi, 28);
            nclrused = constructInt(bi, 32);
            nclrimp = constructInt(bi, 36);
        }
    }
}