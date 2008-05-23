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
 * (c) 2005 by Werner Klieber (werner@klieber.info)
 * http://caliph-emir.sourceforge.net
 */
package at.wklieber.tools;


/**
 * reads the timepoint string of an Mpeg7 document and retrieves the values via
 * the given methods
 */
public class Mpeg7MediaDuration {
    private int day, second, hour, minute, frame, fps;

    public Mpeg7MediaDuration(String str_tp) {
        day = 0;
        hour = 0;
        minute = 0;
        second = 0;
        frame = 0;
        fps = 0;

        int last = 0;

        if ((str_tp.indexOf("D") != -1)) {
            day = Integer.parseInt(str_tp.substring(str_tp.indexOf("P") + 1, str_tp.indexOf("D")));
            last = str_tp.indexOf("D");
        } else
            last = str_tp.indexOf("P");
        last = str_tp.indexOf("T");
        if ((str_tp.indexOf("H") != -1)) {
            hour = Integer.parseInt(str_tp.substring(last + 1, str_tp.indexOf("H")));
            last = str_tp.indexOf("H");
        }
        if ((str_tp.indexOf("M") != -1)) {
            minute = Integer.parseInt(str_tp.substring(last + 1, str_tp.indexOf("M")));
            last = str_tp.indexOf("M");
        }
        if ((str_tp.indexOf("S") != -1)) {
            second = Integer.parseInt(str_tp.substring(last + 1, str_tp.indexOf("S")));
            last = str_tp.indexOf("S");
        }
        if ((str_tp.indexOf("N") != -1)) {
            frame = Integer.parseInt(str_tp.substring(last + 1, str_tp.indexOf("N")));
            last = str_tp.indexOf("N");
        }
        if ((str_tp.indexOf("F") != -1)) {
            fps = Integer.parseInt(str_tp.substring(last + 1, str_tp.indexOf("F")));
            last = str_tp.indexOf("F");
        }
    }

    /**
     * Get the hour of the timepoint
     */
    public int getHour() {
        return hour;
    }

    /**
     * Get the minute of the timepoint
     */
    public int getMinute() {
        return minute;
    }

    /**
     * Get the second of the timepoint
     */
    public int getSecond() {
        return second;
    }

    /**
     * Get the frame of the timepoint
     */
    public int getFrame() {
        return frame;
    }

    /**
     * Get the framerate of the timepoint
     */
    public int getFramesPerSecond() {
        return fps;
    }

    /**
     * Get Number of 1/10 seconds, for a graphical repräsentation
     */
    public int getCentSeconds() {
        double tmp = ((double) frame) / ((double) fps) * 10;
        int returnVal = hour * (60 * 60) + minute * 60 + second;
        returnVal = returnVal * 10 + (int) tmp;
        return returnVal;
    }

    /**
     * Get the position as number of frames
     *
     * @return position as number of frames
     */
    public long getNumFrames() {
        long frames = frame + second * fps + minute * 60 * fps + hour * 60 * 60 * fps;
        return frames;
    }

    public String toString() {
        StringBuffer out = new StringBuffer();
        if (getHour() < 10)
            out.append("0" + getHour() + ":");
        else
            out.append(getHour() + ":");
        if (getMinute() < 10)
            out.append("0" + getMinute() + ":");
        else
            out.append(getMinute() + ":");
        if (getSecond() < 10)
            out.append("0" + getSecond() + " ");
        else
            out.append(getSecond() + " ");
        if (getFrame() < 10)
            out.append("0" + getFrame() + " frames ");
        else
            out.append(getFrame() + " frames ");

        out.append("(" + getFramesPerSecond() + " fps)");

        return out.toString();
    }

    //wklieber: the other way round. Make a MediaDuration out of a date.
    // attetion: just a functional prototype: extracts not all data. It just generates a valid output
    public static String dateToMediaDuration(String dateString) {
        String returnValue = "PT0S";
        int seconds = 0;

        try {
            seconds = Integer.parseInt(dateString);
        } catch (NumberFormatException e) {
            //e.printStackTrace();
            return returnValue;
        }


        try {

            StringBuffer out = new StringBuffer();
            // add date
            out.append("PT");
            out.append(twoDigits(seconds) + "S");
            //out.append("09N25F");

            returnValue = out.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(returnValue);
        return returnValue;
    }

    private static String twoDigits(int number) {
        String returnValue;
        if (number < 10)
            returnValue = "0" + number;
        else
            returnValue = "" + number;

        return returnValue;
    }

}