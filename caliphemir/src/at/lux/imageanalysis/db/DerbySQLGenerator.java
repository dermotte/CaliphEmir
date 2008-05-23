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
 * (c) 2002-2005 by Mathias Lux (mathias@juggle.at) and the Know-Center Graz
 * Inffeldgasse 21a, 8010 Graz, Austria
 * http://www.know-center.at
 */
package at.lux.imageanalysis.db;

import at.lux.imageanalysis.ColorLayout;
import at.lux.imageanalysis.EdgeHistogram;
import at.lux.imageanalysis.ScalableColor;
import at.lux.imageanalysis.JDomVisualDescriptor;

/**
 * This file is part of Caliph & Emir.
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class DerbySQLGenerator implements SQLGenerator {
    private static DerbySQLGenerator instance = null;

    // needed for edgehistogram similarity ...
    private static double[][] QuantTable =
            {{0.010867, 0.057915, 0.099526, 0.144849, 0.195573, 0.260504, 0.358031, 0.530128},
                    {0.012266, 0.069934, 0.125879, 0.182307, 0.243396, 0.314563, 0.411728, 0.564319},
                    {0.004193, 0.025852, 0.046860, 0.068519, 0.093286, 0.123490, 0.161505, 0.228960},
                    {0.004174, 0.025924, 0.046232, 0.067163, 0.089655, 0.115391, 0.151904, 0.217745},
                    {0.006778, 0.051667, 0.108650, 0.166257, 0.224226, 0.285691, 0.356375, 0.450972}};


    private DerbySQLGenerator() {
        // nothing
    }

    /**
     * generates a SQL formula calculating the difference between two images
     * based on the ColorLayout VisualImageDescriptor
     *
     * @param yc YCoeffs of the ColorLayout VisualImageDescriptor
     * @param cb CBCoeffs of the ColorLayout VisualImageDescriptor
     * @param cr CRCoeffs of the ColorLayout VisualImageDescriptor
     * @return a String containing the formula
     */
    private static String getColorLayoutFormula(int[] yc, int[] cb, int[] cr) {
        String statement = "";
        String ycoeffs = "", cbcoeffs = "", crcoeffs = "";
        int numYCoeff = 0;
        int numCCoeff = 0;
        numYCoeff = yc.length;
        numCCoeff = cb.length;
/*
        // this one works with mySQL:
        ycoeffs = ycoeffs + "POW(Y1 - " + yc[0] + ",2)*2 + ";
        ycoeffs = ycoeffs + "POW(Y2 - " + yc[1] + ",2)*2 + ";
        ycoeffs = ycoeffs + "POW(Y3 - " + yc[2] + ",2)*2";

        cbcoeffs = cbcoeffs + "POW(CB1 - " + cb[0] + ",2)*2 + ";
        cbcoeffs = cbcoeffs + "POW(CB2 - " + cb[1] + ",2) + ";
        cbcoeffs = cbcoeffs + "POW(CB3 - " + cb[2] + ",2)";

        crcoeffs = crcoeffs + "POW(CR1 - " + cr[0] + ",2)*4 + ";
        crcoeffs = crcoeffs + "POW(CR2 - " + cr[1] + ",2)*2 + ";
        crcoeffs = crcoeffs + "POW(CR3 - " + cr[2] + ",2)*2";

        for (int i = 3; i < numYCoeff; i++) {
            ycoeffs = ycoeffs + " + POW(Y" + (i + 1) + " - " + yc[i] + ",2)";
        }
        if (numCCoeff > 3) {
            for (int i = 3; i < numCCoeff; i++) {
                crcoeffs = crcoeffs + " + POW(CR" + (i + 1) + " - " + cr[i] + ",2)";
                cbcoeffs = cbcoeffs + " + POW(CB" + (i + 1) + " - " + cb[i] + ",2)";
            }
        }
*/
        ycoeffs = ycoeffs + "(Y1 - " + yc[0] + ")*(Y1 - " + yc[0] + ")*2 + ";
        ycoeffs = ycoeffs + "(Y2 - " + yc[1] + ")*(Y2 - " + yc[1] + ")*2 + ";
        ycoeffs = ycoeffs + "(Y3 - " + yc[2] + ")*(Y3 - " + yc[2] + ")*2";

        cbcoeffs = cbcoeffs + "(CB1 - " + cb[0] + ")*(CB1 - " + cb[0] + ")*2 + ";
        cbcoeffs = cbcoeffs + "(CB2 - " + cb[1] + ")*(CB2 - " + cb[1] + ") + ";
        cbcoeffs = cbcoeffs + "(CB3 - " + cb[2] + ")*(CB3 - " + cb[2] + ")";

        crcoeffs = crcoeffs + "(CR1 - " + cr[0] + ")*(CR1 - " + cr[0] + ")*4 + ";
        crcoeffs = crcoeffs + "(CR2 - " + cr[1] + ")*(CR2 - " + cr[1] + ")*2 + ";
        crcoeffs = crcoeffs + "(CR3 - " + cr[2] + ")*(CR3 - " + cr[2] + ")*2";

        for (int i = 3; i < Math.min(numYCoeff, 15); i++) {
            ycoeffs = ycoeffs + " + (Y" + (i + 1) + " - " + yc[i] + ")*(Y" + (i + 1) + " - " + yc[i] + ")";
        }
        if (numCCoeff > 3) {
            for (int i = 3; i < Math.min(numCCoeff, 15); i++) {
                crcoeffs = crcoeffs + " + (CR" + (i + 1) + " - " + cr[i] + ")*(CR" + (i + 1) + " - " + cr[i] + ")";
                cbcoeffs = cbcoeffs + " + (CB" + (i + 1) + " - " + cb[i] + ")*(CB" + (i + 1) + " - " + cb[i] + ")";
            }
        }

        statement = "(SQRT(" + ycoeffs + ") + SQRT(" + cbcoeffs + ") + SQRT(" + crcoeffs + "))";

        return statement;
    }

    /**
     * Generates an SQL formula (MySQL) for comparing EdgeHistogram Descriptors in a database
     *
     * @param histogram the histogram to match
     * @return a String containing the SQL statement
     */
    private static String getEdgeHistogramFormula(int[] histogram) {
        StringBuilder sb = new StringBuilder();
        sb.append("(ABS(E0 - " + histogram[0] + ")/7)*" + QuantTable[0][7]);
        for (int i = 1; i < 80; i++) {
            sb.append(" + (ABS(E" + i + " - " + histogram[i] + ")/7)*" + QuantTable[i % 5][7]);
        }
        for (int i = 0; i <= 4; i++) {
            sb.append(" + 5 * ABS(E" + i + " - " + histogram[i] + ")");
        }
        for (int i = 5; i < 80; i++) {
            sb.append(" + ABS(E" + i + " - " + histogram[i] + ")");
        }
        return sb.toString();
    }

    /**
     * Generates an SQL formula (MySQL) for comparing ScalableColor Descriptors in a database
     *
     * @param histogram the histogram to match
     * @return a String containing the SQL statement
     */
    private static String getScalableColorFormula(int[] histogram) {
        StringBuilder sb = new StringBuilder();
        sb.append(" ABS(C0 - " + histogram[0] + ") ");
        for (int i = 1; i < Math.min(256, histogram.length); i++) {
            sb.append(" + ABS(C" + i + " - " + histogram[i] + ")");
        }
        return sb.toString();
    }

    private static String createTableForColorLayoutStatement() {
        StringBuilder stmt = new StringBuilder(256);
        String ycoeffs = "", cbcoeffs = "", crcoeffs = "";
        for (int i = 1; i < 16; i++) {
            ycoeffs = ycoeffs + " Y" + i + " INT, ";
            cbcoeffs = cbcoeffs + " CB" + i + " INT, ";
            crcoeffs = crcoeffs + " CR" + i + " INT, ";
        }
        stmt.append("CREATE TABLE colorlayout (fileName VARCHAR(256) NOT NULL, ");
        stmt.append(ycoeffs);
        stmt.append(cbcoeffs);
        stmt.append(crcoeffs);
        stmt.append("PRIMARY KEY (fileName))");
        return stmt.toString();
    }

    private static String createTableForEdgeHistogramStatement() {
        StringBuilder stmt = new StringBuilder(256);
        StringBuilder ycoeffs = new StringBuilder(800);
        for (int i = 0; i < 80; i++) {
            ycoeffs.append(' ');
            ycoeffs.append('E');
            ycoeffs.append(i);
            ycoeffs.append(" SMALLINT NOT NULL, ");
        }
        stmt.append("CREATE TABLE edgehistogram (fileName VARCHAR(256) NOT NULL, ");
        stmt.append(ycoeffs);
        stmt.append("PRIMARY KEY (fileName))");
        return stmt.toString();
    }

    private static String createTableForScalableColor() {
        StringBuilder stmt = new StringBuilder(1000);
        StringBuilder ycoeffs = new StringBuilder(800);
        for (int i = 0; i < 256; i++) {
            ycoeffs.append(' ');
            ycoeffs.append('C');
            ycoeffs.append(i);
            ycoeffs.append(" INT, ");
        }
        stmt.append("CREATE TABLE scalablecolor (fileName VARCHAR(256) NOT NULL, numBitplanesDiscarded INT NOT NULL default 0, numCoefficients INT NOT NULL default 256, ");
        stmt.append(ycoeffs);
        stmt.append("PRIMARY KEY (fileName))");
        return stmt.toString();
    }

    private static String prepareInsertStatement(String fileName, ScalableColor sc) {
        StringBuilder sb = new StringBuilder(2048);
        sb.append("INSERT INTO scalablecolor ( fileName , numBitplanesDiscarded, numCoefficients");
        for (int i = 0; i < 256; i++) {
            sb.append(',');
            sb.append(' ');
            sb.append('C');
            sb.append(i);
        }
        sb.append(") VALUES ('");
        sb.append(fileName);
        sb.append('\'');
        sb.append(", 0, 256");
        int[] hist = sc.getHaarTransformedHistogram();
        for (int i = 0; i < hist.length; i++) {
            sb.append(',');
            sb.append(' ');
            sb.append(hist[i]);
        }
        sb.append(')');
        return sb.toString();
    }

    private static String prepareInsertStatement(String fileName, EdgeHistogram eh) {
        StringBuffer sb = new StringBuffer("INSERT INTO edgehistogram ( fileName , E0 , E1 , E2 , E3 , E4 , E5 , E6 , E7 , E8 , E9 , E10 , E11 , E12 , E13 , E14 , E15 , E16 , E17 , E18 , E19 , E20 , E21 , E22 , E23 , E24 , E25 , E26 , E27 , E28 , E29 , E30 , E31 , E32 , E33 , E34 , E35 , E36 , E37 , E38 , E39 , E40 , E41 , E42 , E43 , E44 , E45 , E46 , E47 , E48 , E49 , E50 , E51 , E52 , E53 , E54 , E55 , E56 , E57 , E58 , E59 , E60 , E61 , E62 , E63 , E64 , E65 , E66 , E67 , E68 , E69 , E70 , E71 , E72 , E73 , E74 , E75 , E76 , E77 , E78 , E79 ) VALUES ('");
        sb.append(fileName);
        sb.append('\'');
        int[] hist = eh.getHistogram();
        for (int i = 0; i < hist.length; i++) {
            sb.append(',');
            sb.append(' ');
            sb.append(hist[i]);

        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * Currently stripped down to 15.
     *
     * @param fileName
     * @param cl
     * @return the SQL String for inserting the ColorLayout descriptor into the database.
     */
    private static String prepareInsertStatement(String fileName, ColorLayout cl) {
        StringBuffer result = new StringBuffer(256);
        result.append("INSERT INTO colorlayout ( fileName , Y1 , Y2 , Y3 , Y4 , Y5 , Y6 , Y7 , Y8 , Y9 , Y10 , Y11 , Y12 , Y13 , Y14 , Y15 , CB1 , CB2 , CB3 , CB4 , CB5 , CB6 , CB7 , CB8 , CB9 , CB10 , CB11 , CB12 , CB13 , CB14 , CB15 , CR1 , CR2 , CR3 , CR4 , CR5 , CR6 , CR7 , CR8 , CR9 , CR10 , CR11 , CR12 , CR13 , CR14 , CR15 ) VALUES ( ");
        int[] ycoeffs = cl.getYCoeff();
        int[] cbcoeffs = cl.getCbCoeff();
        int[] crcoeffs = cl.getCrCoeff();
        result.append('\'');
        result.append(fileName);
        result.append('\'');
        for (int i = 0; i < 15; i++) {
            result.append(", ");
            result.append(ycoeffs[i]);

        }
        for (int i = 0; i < 15; i++) {
            result.append(", ");
            result.append(cbcoeffs[i]);
        }
        for (int i = 0; i < 15; i++) {
            result.append(", ");
            result.append(crcoeffs[i]);
        }
        result.append(" )");
        return result.toString();
    }

    public String getConnectionURL() {
        return "jdbc:derby:imageDB;create=true";
    }

    public String getDriverClassName() {
        return "org.apache.derby.jdbc.EmbeddedDriver";
    }

    public String getCreateTableStatement(JDomVisualDescriptor.Type descriptor) {
        if (descriptor == JDomVisualDescriptor.Type.EdgeHistogram) {
            return createTableForEdgeHistogramStatement();
        } else if (descriptor == JDomVisualDescriptor.Type.ScalableColor) {
            return createTableForScalableColor();
        } else if (descriptor == JDomVisualDescriptor.Type.ColorLayout) {
            return createTableForColorLayoutStatement();
        } else {
            throw new UnsupportedOperationException("Not implemented yet!");
        }
    }

    public String getInsertStatement(String filename, JDomVisualDescriptor descriptor) {
        if (descriptor instanceof EdgeHistogram) {
            return prepareInsertStatement(filename, (EdgeHistogram) descriptor);
        } else if (descriptor instanceof ScalableColor) {
            return prepareInsertStatement(filename, (ScalableColor) descriptor);
        } else if (descriptor instanceof ColorLayout) {
            return prepareInsertStatement(filename, (ColorLayout) descriptor);
        } else {
            throw new UnsupportedOperationException("Not implemented yet!");
        }
    }

    public String getSearchSelectStatement(JDomVisualDescriptor descriptor) {
        if (descriptor instanceof EdgeHistogram) {
            String edgeHistogramFormula = getEdgeHistogramFormula(((EdgeHistogram) descriptor).getHistogram());
            return "SELECT fileName, " + edgeHistogramFormula + " as distance from edgehistogram ORDER BY 2";
        } else if (descriptor instanceof ScalableColor) {
            String scalableColorFormula = getScalableColorFormula(((ScalableColor) descriptor).getHaarTransformedHistogram());
            return "SELECT fileName, " + scalableColorFormula + " as distance from scalablecolor ORDER BY 2";
        } else if (descriptor instanceof ColorLayout) {
            ColorLayout colorLayout = (ColorLayout) descriptor;
            String colorLayoutFormula = getColorLayoutFormula(colorLayout.getYCoeff(), colorLayout.getCbCoeff(), colorLayout.getCrCoeff());
            return "SELECT fileName, " + colorLayoutFormula + " as distance from colorlayout ORDER BY 2";
        } else {
            throw new UnsupportedOperationException("Not implemented yet!");
        }
    }

    protected static DerbySQLGenerator getInstance() {
        if (instance == null) {
            instance = new DerbySQLGenerator();
        }
        return instance;
    }
}
