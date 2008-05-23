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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Mpeg7DateFormat {
    public static Date format(String str_date) throws Exception {
        Date returnValue = null;
        try {
            if (str_date.length() > 18) {
                int start = 0;
                String year = str_date.substring(0, 4);
                String month = str_date.substring(5, 7);
                String day = str_date.substring(8, 10);
                String hour = str_date.substring(11, 13);
                String minute = str_date.substring(14, 16);
                String second = str_date.substring(17, 19);

                Calendar cal = Calendar.getInstance();

                cal.set(Integer.parseInt(year),
                        Integer.parseInt(month) - 1,
                        Integer.parseInt(day),
                        Integer.parseInt(hour),
                        Integer.parseInt(minute),
                        Integer.parseInt(second));
                returnValue = cal.getTime();
            } else
                returnValue = new Date();
        } catch (NumberFormatException e) {
            System.err.println("Intput date String: \"" + str_date + "\"");
            //e.printStackTrace();
        }

        return returnValue;
    }

    // format version that does not throw exceptions
    // instead it returns the default1 on errors
    public static Date format(String str_date, Date default1) {
        Date returnValue = default1;

        try {
            returnValue = format(str_date);
        } catch (Exception e) {
            // silent catch. the default Value is returned
            returnValue = default1;
        }

        return returnValue;
    }

    // simple method to add a leading zero if numer is less then 10
    private static String twoDigits(int number) {
        String returnValue;
        if (number < 10)
            returnValue = "0" + number;
        else
            returnValue = "" + number;

        return returnValue;
    }

    private static String fourDititsYear(int number) {
       String returnValue;

       SimpleDateFormat format = new SimpleDateFormat("yyyy");
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, number);
        returnValue = format.format(c.getTime());

        return returnValue;
    }

    /**
     * Convert a java Date to a mpeg7 Timepoint
     * Author: Werner Klieber
     */
    public static String date2Timepoint(Date date) {
        String returnValue = "";

        if (date == null)
            return returnValue;

        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            StringBuffer out = new StringBuffer();
            // add date
            int year = Calendar.YEAR;
            String yearString = fourDititsYear(year);
            out.append(yearString + "-");
            out.append(twoDigits(Calendar.MONTH) + "-");
            out.append(twoDigits(Calendar.DAY_OF_MONTH) + "T");

            out.append(twoDigits(Calendar.HOUR_OF_DAY) + ":");
            out.append(twoDigits(Calendar.MINUTE) + ":");
            out.append(twoDigits(Calendar.SECOND) + "+");
            out.append(twoDigits(Calendar.ZONE_OFFSET) + ":00");

            returnValue = out.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //System.out.println(returnValue);
        return returnValue;
    }

    /**
     * @return a list of strings containing hour, min, sec
     */
    public static String[] getTimePointArray(Date date) {
        String[] returnValue = new String[3];
        returnValue[0] = "";
        returnValue[1] = "";
        returnValue[2] = "";

        try {
            if (date == null) {
                return returnValue;
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            returnValue[0] = twoDigits(Calendar.DAY_OF_MONTH);
            returnValue[1] = twoDigits(Calendar.MINUTE);
            returnValue[2] = twoDigits(Calendar.SECOND);


        } catch (NumberFormatException e) {
            e.printStackTrace();
            //cat.severe(e.toString());
        }

        return returnValue;
    }


}