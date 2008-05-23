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
 * (c) 2002-2005 by Mathias Lux (mathias@juggle.at)
 * Inffeldgasse 21a, 8010 Graz, Austria
 * http://www.know-center.at
 */
package at.lux.imageanalysis.db;

import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.sql.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;

import at.lux.imageanalysis.ColorLayout;
import at.lux.imageanalysis.EdgeHistogram;
import at.lux.imageanalysis.ScalableColor;
import at.lux.imageanalysis.JDomVisualDescriptor;

/**
 * Date: 27.10.2005
 * Time: 20:42:12
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class DerbyTest extends TestCase {
    public String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private Connection conn;
    private BufferedImage img;
    private File file;
    private SQLGenerator gen;

    public void setUp() {
        try {
            Class.forName(driver).newInstance();
            System.out.println("Loaded the appropriate driver.");
            conn = DriverManager.getConnection("jdbc:derby:imageDB;create=true");
            String name = "testdata/P1040588.JPG";
            file = new File(name);
            img = ImageIO.read(new FileInputStream(file));
            gen = SQLGeneratorFactory.getSQLGenerator(SQLGeneratorFactory.Database.Derby);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        conn.close();
        try {
            DriverManager.getConnection("jdbc:derby:imageDB;shutdown=true");
        } catch (SQLException e) {
            System.out.println("Shutdown successful!");
        }
    }

    public void testCreateTable() throws SQLException {

        Statement statement = conn.createStatement();

        String tableCreate = gen.getCreateTableStatement(JDomVisualDescriptor.Type.ColorLayout);
        System.out.println(tableCreate);
        int i = statement.executeUpdate(tableCreate);
        assertTrue(i == 0);

        tableCreate = gen.getCreateTableStatement(JDomVisualDescriptor.Type.EdgeHistogram);
        System.out.println(tableCreate);
        i = statement.executeUpdate(tableCreate);
        assertTrue(i == 0);

        tableCreate = gen.getCreateTableStatement(JDomVisualDescriptor.Type.ScalableColor);
        System.out.println(tableCreate);
        i = statement.executeUpdate(tableCreate);
        assertTrue(i == 0);
    }

    public void testInsert() throws IOException, SQLException {
        Statement statement = conn.createStatement();

        ColorLayout cl = new ColorLayout(img);
        String sql = gen.getInsertStatement(file.getName(), cl);
        System.out.println(sql);
        int rowCount = statement.executeUpdate(sql);
        assertTrue(rowCount == 1);

        EdgeHistogram eh = new EdgeHistogram(img);
        sql = gen.getInsertStatement(file.getName(), eh);
        System.out.println(sql);
        rowCount = statement.executeUpdate(sql);
        assertTrue(rowCount == 1);

        ScalableColor sc = new ScalableColor(img);
        sql = gen.getInsertStatement(file.getName(), sc);
        System.out.println(sql);
        rowCount = statement.executeUpdate(sql);
        assertTrue(rowCount == 1);

    }

    public void testIndexer() throws IOException, SQLException {
        // Adding all the images to the database:
        String[] images = getAllImages(new File("testdata/I-Know 02"), true);
        System.out.println("Found " + images.length + " images.");

        Statement statement = conn.createStatement();

        for (int i = 0; i < images.length; i++) {
            try {
                BufferedImage img = ImageIO.read(new FileInputStream(images[i]));

                ScalableColor sc = new ScalableColor(img);
                String sql = gen.getInsertStatement(images[i], sc);
                int rowCount = statement.executeUpdate(sql);
                assertTrue(rowCount == 1);

                EdgeHistogram eh = new EdgeHistogram(img);
                sql = gen.getInsertStatement(images[i], eh);
                rowCount = statement.executeUpdate(sql);
                assertTrue(rowCount == 1);

                ColorLayout cl = new ColorLayout(img);
                sql = gen.getInsertStatement(images[i], cl);
                rowCount = statement.executeUpdate(sql);
                assertTrue(rowCount == 1);

                System.out.print(".");
            } catch (IOException e) {
                System.out.println("Could not insert " + images[i] + ": " + e.toString());
            } catch (SQLException e) {
                System.out.println("Could not insert " + images[i] + ": " + e.toString());
            }
        }
    }

    public void testSearch() throws SQLException {
        ScalableColor sc = new ScalableColor(img);
        doSearch(gen.getSearchSelectStatement(sc));

        EdgeHistogram eh = new EdgeHistogram(img);
        doSearch(gen.getSearchSelectStatement(eh));

        ColorLayout cl = new ColorLayout(img);
        doSearch(gen.getSearchSelectStatement(cl));
    }

    private void doSearch(String sql) throws SQLException {
        Statement statement = conn.createStatement();
        long ms = System.currentTimeMillis();
        ResultSet rs = statement.executeQuery(sql);
        System.out.println("Search results (" + (System.currentTimeMillis() - ms) + "):");
        int count = 0;
        while (rs.next()) {
            System.out.println(++count + " / " + rs.getString("distance") + ": " + rs.getString("fileName"));
        }
        System.out.println("-----------------------------");
    }

    public static String[] getAllImages(File directory, boolean descendIntoSubDirectories) throws IOException {
        ArrayList<String> v = new ArrayList();
        File[] f = directory.listFiles();
        for (int i = 0; i < f.length; i++) {
            File file = f[i];
            if (file != null && file.getName().toLowerCase().endsWith(".jpg") && !file.getName().startsWith("tn_")) {
                v.add(file.getCanonicalPath());
            }

            if (descendIntoSubDirectories && file.isDirectory()) {
                String[] tmp = getAllImages(file, true);
                if (tmp != null) {
                    for (int j = 0; j < tmp.length; j++) {
                        v.add(tmp[j]);
                    }
                }
            }
        }
        if (v.size() > 0)
            return v.toArray(new String[1]);
        else
            return null;
    }

}
