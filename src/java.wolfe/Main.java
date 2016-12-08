//package java.wolfe;

/*
*   Java 2545 MCTC Final Project - Part One of Two Programs
*   Database Loader
*   Jeremy Wolfe, November 28, 2016
*
*   This program loads the Java_API database with data scrapped from
*   the Java SE 8 API Documentation (downloaded version).
*   Uses Library JSoup to select and extract information from
*   .html files.
*
*   Java API package-summary.html files have been created at the package level and
*   drive the loading process. Summaries are scanned through and tables loaded with
*   names and descriptions/summaries. Level 3 tables are loaded from .html files
*   built from the class name.
*
*  See the following Oracle link for a detailed description of the API structure and format:
*  https://docs.oracle.com/javase/8/docs/api/help-doc.html
*  The database is modelled based on the API structure.
*
*   7 tables are loaded:
*   package         level 1
*   class (klass)   level 2 - points back to package table
*   exception       level 2
*   errors          level 2
*   method          level 3 - points back to class table
*   field           level 3
*   constructor     level 3
*
*   This database is used by a Java GUI program to search and access the data
*   (the GUI is part two of two applications).
*
*
 */


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;



public class Main {

    // counters for stats at end of program execution
    static int totalPackageRows = 0;
    static int totalKlassRows = 0;
    static int totalExceptionRows = 0;
    static int totalErrorsRows = 0;
    static int totalMethodRows = 0;
    static int totalConstructorRows = 0;
    static int totalFieldRows = 0;
    static int totalNullPointerExceptions = 0;
    static int totalOtherExceptions = 0;
    static int totalSQLExceptions = 0;
    static int totalIOExceptions = 0;

    // setup the database driver
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    //http://stackoverflow.com/questions/34189756/warning-about-ssl-connection-when-connecting-to-mysql-database
    static final String DB_CONNECTION_URL = "jdbc:mysql://localhost:3306/java_api?autoReconnect=true&useSSL=false";
    static final String USER = "myrlin";
    static final String PASSWORD = "password";

    public static void main(String[] args) {


        long start = System.currentTimeMillis();

        // strings used in JSoup select statements
        String classSumSearch = "table[summary=Class Summary table, listing classes, and an explanation]";
        String interfaceSumSearch = "table[summary=Interface Summary table, listing interfaces, and an explanation]";
        String exceptionSumSearch = "table[summary=Exception Summary table, listing exceptions, and an explanation]";
        String errorsSumSearch = "table[summary=Error Summary table, listing errors, and an explanation]";

        Connection connection = null;

        try {

            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);

        } catch (ClassNotFoundException cnfe) {
            System.out.println("in main method");
            cnfe.printStackTrace();
            System.out.println();
            totalOtherExceptions++;
        } catch (SQLException sqle) {
            System.out.println("in main method");
            sqle.printStackTrace();
            System.out.println();
            totalSQLExceptions++;
        }


        // instantiate a filesearch object to scan the API directories
        FileSearch fileSearch = new FileSearch();

//        deleteTables(connection);   // comment out when running second job with javax lib !!!!

//        System.exit(0);

        // find all package-summary.html files for the /java or /javax directories
        // the searchForFiles getResult method returns path and file name in a pathfile string containing
        // the package-summary.html file for that particular directory. The filepath string is later passed to
        // the 3rd level Method methods where the file name is stripped off and a new filepath string is built to
        // access the <classname>.html file for method data in that respective directory.
        searchForFiles(fileSearch); // copied entirely from:
                                    // https://www.mkyong.com/java/search-directories-recursively-for-file-in-java/


        // main loop steps through each package-summary.html file and calls load table methods
        int items = 0;
        for (String dir : fileSearch.getResult()){
            items++;
            System.out.println(items + "  processing dir = " + dir);

            try {
                String packageFK = loadPackageTable(dir, connection);

                loadKlassTable(packageFK, dir, connection, classSumSearch, "1");
                loadKlassTable(packageFK, dir, connection, interfaceSumSearch, "2");

                loadExceptionTable(packageFK, dir, connection, exceptionSumSearch);
                loadErrorsTable(packageFK, dir, connection, errorsSumSearch);

            } catch (NullPointerException npe) {
                System.out.println("in main method");
                npe.printStackTrace();
                System.out.println();
                totalNullPointerExceptions++;
            }

            System.out.println();
            System.out.println("******* end of package ********");
            System.out.println("********* Total Package Rows = " + totalPackageRows);
            System.out.println("********* Total Klass Rows = " + totalKlassRows);
            System.out.println("********* Total Exception Rows = " + totalExceptionRows);
            System.out.println("********* Total Errors Rows = " + totalErrorsRows);
            System.out.println("********* Total Method Rows = " + totalMethodRows);
            System.out.println("********* Total Field Rows = " + totalFieldRows);
            System.out.println("********* Total Constructor Rows = " + totalConstructorRows);

        }

        System.out.println("*****************************************");
        System.out.println("********* Total Package Rows = " + totalPackageRows);
        System.out.println("********* Total Klass Rows = " + totalKlassRows);
        System.out.println("********* Total Exception Rows = " + totalExceptionRows);
        System.out.println("********* Total Errors Rows = " + totalErrorsRows);
        System.out.println("********* Total Method Rows = " + totalMethodRows);
        System.out.println("********* Total Field Rows = " + totalFieldRows);
        System.out.println("********* Total Constructor Rows = " + totalConstructorRows);
        System.out.println("*****************************************");
        System.out.println("********* Total NullPointerExceptions = " + totalNullPointerExceptions);
        System.out.println("********* Total SQLExceptions = " + totalSQLExceptions);
        System.out.println("********* Total IOExceptions = " + totalIOExceptions);
        System.out.println("********* Total OtherExceptions = " + totalOtherExceptions);
        System.out.println("*****************************************");

        try {
            connection.close();
        } catch (SQLException sqle) {
            System.out.println("in main method");
            sqle.printStackTrace();
            System.out.println();
            totalSQLExceptions++;
        }
        // http://stackoverflow.com/questions/5204051/how-to-calculate-the-running-time-of-my-program
        long end = System.currentTimeMillis();
        NumberFormat formatter = new DecimalFormat("#0.00000");
        System.out.print("Execution time is " + formatter.format((end - start) / 1000d) + " seconds");

    }

    // each package-summary.html file drives this method where sub-items are selected
    // using the JSoup library to pull information from the html. Database tables are
    // loaded from the scrapped info.
    private static String loadPackageTable(String dir, Connection connection) {

        int packageRows = 0;

        Statement statement = null;
        java.sql.PreparedStatement pstmt = null;
        java.sql.PreparedStatement sstmt = null;
        Document doc = null;

        try {

            statement = connection.createStatement();

            pstmt = connection.prepareStatement("INSERT INTO package VALUES (?,?,?)");
            sstmt = connection.prepareStatement("SELECT * FROM package WHERE name = ?");

            // example of what the dir string looks like immediately follows this comment:
            //File input = new File("C:/Users/myrlin/Desktop/Java/JavaDocs/docs/api/java/util/package-summary.html");
            File input = new File(dir);
            // this sets the input file to be scanned by JSoup
            doc = Jsoup.parse(input, "UTF-8");

        } catch (SQLException sqle) {
            System.out.println("in loadPackage method");
            sqle.printStackTrace();
            System.out.println();
            totalSQLExceptions++;
        } catch (IOException ioe) {
            System.out.println("in loadPackage method");
            ioe.printStackTrace();
            System.out.println();
            totalIOExceptions++;
        }

        String foreignKey = null;


        // JSoup doc.select statement scans the html file for selected items and returns them in an Elements data
        // structure. Further refinements are made picking off the desired data as needed.
        try {
            Elements items = doc.select("div[class=header]");
            Iterator<Element> iterator = items.select("h1").iterator();
            String nameField = null;
            nameField = iterator.next().text();
            nameField = nameField.replace("Package", "");
            System.out.println("name = " + nameField);

            items = doc.select("div[class=docSummary]");
            String description = null;
            iterator = items.select("div[class=block]").iterator();
            description = iterator.next().text();
            System.out.println("description = " + description);

            pstmt.setString(1, null);
            if (nameField.length() > 200) {
                nameField = nameField.substring(0, 199);
            }
            pstmt.setString(2, nameField);
            if (description.length() > 400) {
                description = description.substring(0, 399);
            }
            pstmt.setString(3, description);
            pstmt.executeUpdate();

            packageRows++;

            sstmt.setString(1, nameField);
            ResultSet selectRS = sstmt.executeQuery();

            int i = 0;
            while (selectRS.next()) {
                i++;
            }

            selectRS.first();
                System.out.println("ID: " + selectRS.getString(1));
                foreignKey = selectRS.getString(1);
    //            System.out.println("Name: " + rs.getString(2));
    //            System.out.println("Description: " + rs.getString(3));
    //            System.out.println();

            selectRS.close();
            statement.close();

        } catch (NullPointerException npe) {
            System.out.println("in loadPackage method");
            npe.printStackTrace();
            System.out.println();
            totalNullPointerExceptions++;
        } catch (SQLException sqle) {
            System.out.println("in loadPackage method");
            sqle.printStackTrace();
            System.out.println();
            totalSQLExceptions++;
        }  catch (Exception e) {
            System.out.println("in loadPackage method");
            e.printStackTrace();
            System.out.println();
            totalOtherExceptions++;
        }


        totalPackageRows = totalPackageRows + packageRows;
        //sleep(500);
        System.out.println();
        System.out.println("Package: rows added = " + packageRows);
        System.out.println();
        return foreignKey;

    } // end loadPackageTable



    private static void loadKlassTable(String packageFK,
                                       String dir,
                                       Connection connection,
                                       String searchOn,
                                       String classType) {

        int klassRows = 0;

        Statement statement = null;
        java.sql.PreparedStatement pstmt = null;
        java.sql.PreparedStatement sstmt = null;
        Document doc = null;

        try {
            statement = connection.createStatement();

            pstmt = connection.prepareStatement("INSERT INTO klass VALUES (?,?,?,?,?)");
            sstmt = connection.prepareStatement("SELECT * FROM klass WHERE k_package_ID_fk = ?");


            //File input = new File("C:/Users/myrlin/Desktop/Java/JavaDocs/docs/api/java/util/package-summary.html");
            File input = new File(dir);
            doc = Jsoup.parse(input, "UTF-8");
        } catch (SQLException sqle) {
            System.out.println("in loadKlass method");
            sqle.printStackTrace();
            System.out.println();
            totalSQLExceptions++;
        } catch (IOException ioe) {
            System.out.println("in loadKlass method");
            ioe.printStackTrace();
            System.out.println();
            totalIOExceptions++;
        }


        // Element table = doc.select("table[summary=Interface Summary table, listing interfaces, and an explanation]").first();
        // Element table = doc.select("table[summary=Class Summary table, listing classes, and an explanation]").first();
        Element table = doc.select(searchOn).first();


        String inputName = null;
        try {
            if (table != null && table.hasText()) {

                Iterator<Element> iterator = table.select("td").iterator();
                int count = 1;
                while(iterator.hasNext()) {

                    inputName = iterator.next().text();
                    if (inputName.length() > 200) {
                        inputName = inputName.substring(0, 199);
                    }
                    String inputSummary = iterator.next().text();
                    if (inputSummary.length() > 400) {
                        inputSummary = inputSummary.substring(0, 399);
                    }

                    pstmt.setString(1, null);
                    pstmt.setString(2, classType);
                    pstmt.setString(3, inputName);
                    pstmt.setString(4, inputSummary);
                    pstmt.setString(5, packageFK);
                    pstmt.executeUpdate();

                    klassRows++;
                }

                sstmt.setString(1, packageFK);
                System.out.println("loadKlassTable: m_klass_ID_fk = " + packageFK);
                ResultSet selectRS = sstmt.executeQuery();

                File currentDir = new File(dir);

                while (selectRS.next()) {
                    System.out.println("Klass ID: " + selectRS.getString(1));
//                    System.out.println("Type: " + rs.getString(2));
                    System.out.println("Klass Name: " + selectRS.getString(3));
//                    System.out.println("Description: " + rs.getString(4));
//                    System.out.println("Foreign Key: " + rs.getString(5));
//                    System.out.println();

                    loadMethodTable(selectRS.getString(3), selectRS.getString(1), currentDir, connection);
                    loadFieldTable(selectRS.getString(3), selectRS.getString(1), currentDir, connection);
                    loadConstructorTable(selectRS.getString(3), selectRS.getString(1), currentDir, connection);
                }

                selectRS.close();
                statement.close();

            }
        } catch (NullPointerException npe) {
            System.out.println("in loadKlass method");
            npe.printStackTrace();
            System.out.println();
            totalNullPointerExceptions++;
        } catch (SQLException sqle) {
            System.out.println("in loadKlass method");
            sqle.printStackTrace();
            System.out.println();
            totalSQLExceptions++;
        }  catch (Exception e) {
            System.out.println("in loadKlass method");
            e.printStackTrace();
            System.out.println();
            totalOtherExceptions++;
        }


        totalKlassRows = totalKlassRows + klassRows;
        System.out.println();
        System.out.println("Klass: rows added = " + klassRows);
        System.out.println();

    } // end loadKlassTable



    private static void loadExceptionTable(String packageFK, String dir, Connection connection, String searchOn) {

        int exceptionRows = 0;

        Statement statement = null;
        java.sql.PreparedStatement pstmt = null;
        java.sql.PreparedStatement sstmt = null;
        Document doc = null;

        try {
            statement = connection.createStatement();

            pstmt = connection.prepareStatement("INSERT INTO exception VALUES (?,?,?,?,?)");
            sstmt = connection.prepareStatement("SELECT * FROM exception WHERE x_package_ID_fk = ?");


            //File input = new File("C:/Users/myrlin/Desktop/Java/JavaDocs/docs/api/java/util/package-summary.html");
            File input = new File(dir);
            doc = Jsoup.parse(input, "UTF-8");
        } catch (SQLException sqle) {
            System.out.println("in loadException method");
            sqle.printStackTrace();
            System.out.println();
            totalSQLExceptions++;
        } catch (IOException ioe) {
            System.out.println("in loadException method");
            ioe.printStackTrace();
            System.out.println();
            totalIOExceptions++;
        }


        // Element table = doc.select("table[summary=Interface Summary table, listing interfaces, and an explanation]").first();
        // Element table = doc.select("table[summary=Class Summary table, listing classes, and an explanation]").first();
        Element table = doc.select(searchOn).first();


        String inputName = null;
        try {
            if (table != null && table.hasText()) {

                Iterator<Element> iterator = table.select("td").iterator();
                int count = 1;
                while(iterator.hasNext()) {

                    inputName = iterator.next().text();
                    if (inputName.length() > 200) {
                        inputName = inputName.substring(0, 199);
                    }
                    String inputSummary = iterator.next().text();
                    if (inputSummary.length() > 400) {
                        inputSummary = inputSummary.substring(0, 399);
                    }

                    pstmt.setString(1, null);
                    pstmt.setString(2, inputName);
                    pstmt.setString(3, inputSummary);
                    pstmt.setString(4, null);
                    pstmt.setString(5, packageFK);
                    pstmt.executeUpdate();

                    exceptionRows++;
                }

                sstmt.setString(1, packageFK);
                System.out.println("loadException: x_package_ID_fk = " + packageFK);
                ResultSet selectRS = sstmt.executeQuery();

                File currentDir = new File(dir);

                while (selectRS.next()) {
                    System.out.println("Exception ID: " + selectRS.getString(1));
//                    System.out.println("Type: " + rs.getString(2));
                    System.out.println("Exception Name: " + selectRS.getString(3));
//                    System.out.println("Description: " + rs.getString(4));
//                    System.out.println("Foreign Key: " + rs.getString(5));
//                    System.out.println();

                }

                selectRS.close();
                statement.close();

            }
        } catch (NullPointerException npe) {
            System.out.println("in loadException method");
            npe.printStackTrace();
            System.out.println();
            totalNullPointerExceptions++;
        } catch (SQLException sqle) {
            System.out.println("in loadException method");
            sqle.printStackTrace();
            System.out.println();
            totalSQLExceptions++;
        }  catch (Exception e) {
            System.out.println("in loadException method");
            e.printStackTrace();
            System.out.println();
            totalOtherExceptions++;
        }


        totalExceptionRows = totalExceptionRows + exceptionRows;
        System.out.println();
        System.out.println("Exception: rows added = " + exceptionRows);
        System.out.println();

    }

    private static void loadErrorsTable(String packageFK, String dir, Connection connection, String searchOn) {

        int errorsRows = 0;

        Statement statement = null;
        java.sql.PreparedStatement pstmt = null;
        java.sql.PreparedStatement sstmt = null;
        Document doc = null;

        try {
            statement = connection.createStatement();

            pstmt = connection.prepareStatement("INSERT INTO errors VALUES (?,?,?,?,?)");
            sstmt = connection.prepareStatement("SELECT * FROM errors WHERE r_package_ID_fk = ?");


            //File input = new File("C:/Users/myrlin/Desktop/Java/JavaDocs/docs/api/java/util/package-summary.html");
            File input = new File(dir);
            doc = Jsoup.parse(input, "UTF-8");
        } catch (SQLException sqle) {
            System.out.println("in loadException method");
            sqle.printStackTrace();
            System.out.println();
            totalSQLExceptions++;
        } catch (IOException ioe) {
            System.out.println("in loadException method");
            ioe.printStackTrace();
            System.out.println();
            totalIOExceptions++;
        }


        // Element table = doc.select("table[summary=Interface Summary table, listing interfaces, and an explanation]").first();
        // Element table = doc.select("table[summary=Class Summary table, listing classes, and an explanation]").first();
        Element table = doc.select(searchOn).first();


        String inputName = null;
        try {
            if (table != null && table.hasText()) {

                Iterator<Element> iterator = table.select("td").iterator();
                int count = 1;
                while(iterator.hasNext()) {

                    inputName = iterator.next().text();
                    if (inputName.length() > 200) {
                        inputName = inputName.substring(0, 199);
                    }
                    String inputSummary = iterator.next().text();
                    if (inputSummary.length() > 400) {
                        inputSummary = inputSummary.substring(0, 399);
                    }

                    pstmt.setString(1, null);
                    pstmt.setString(2, inputName);
                    pstmt.setString(3, inputSummary);
                    pstmt.setString(4, null);
                    pstmt.setString(5, packageFK);
                    pstmt.executeUpdate();

                    errorsRows++;
                }

                sstmt.setString(1, packageFK);
                System.out.println("loadErrors: r_package_ID_fk = " + packageFK);
                ResultSet selectRS = sstmt.executeQuery();

                File currentDir = new File(dir);

                while (selectRS.next()) {
                    System.out.println("Error ID: " + selectRS.getString(1));
//                    System.out.println("Type: " + rs.getString(2));
                    System.out.println("Error Name: " + selectRS.getString(3));
//                    System.out.println("Description: " + rs.getString(4));
//                    System.out.println("Foreign Key: " + rs.getString(5));
//                    System.out.println();

                }

                selectRS.close();
                statement.close();

            }
        } catch (NullPointerException npe) {
            System.out.println("in loadErrors method");
            npe.printStackTrace();
            System.out.println();
            totalNullPointerExceptions++;
        } catch (SQLException sqle) {
            System.out.println("in loadErrors method");
            sqle.printStackTrace();
            System.out.println();
            totalSQLExceptions++;
        }  catch (Exception e) {
            System.out.println("in loadErrors method");
            e.printStackTrace();
            System.out.println();
            totalOtherExceptions++;
        }


        totalErrorsRows = totalErrorsRows + errorsRows;
        System.out.println();
        System.out.println("Errors: rows added = " + errorsRows);
        System.out.println();

    }


    private static void loadMethodTable(String searchname, String klassID, File directory, Connection connection) {

        int methodRows = 0;

        Statement statement = null;
        java.sql.PreparedStatement pstmt = null;

        try {
            statement = connection.createStatement();

            pstmt = connection.prepareStatement("INSERT INTO method VALUES (?,?,?,?,?,?)");
        } catch (SQLException sqle) {
            System.out.println("in loadMethod method");
            sqle.printStackTrace();
            System.out.println();
            totalSQLExceptions++;
        }

        String trimName = searchname.split("<", 2)[0];

        File newDir = directory.getParentFile();
        System.out.println("in loadMethodTable: parent directory = " + newDir);
        String methodFile = newDir + "\\" + trimName + ".html";
        File testFile = new File(methodFile);

        System.out.println("in loadMethodTable: filepath = " + methodFile);

        try {
            if (testFile.isFile()) {
                File input = new File(methodFile);
                //File input = new File("C:/Users/myrlin/Desktop/Java/JavaDocs/docs/api/java/util/Arraylist.html");
                Document doc = Jsoup.parse(input, "UTF-8");


                Element table = doc.select("table[summary=Method Summary table, listing methods, and an explanation]").first();
                if (table != null) {
                    Iterator<Element> iterator = table.select("td[class=colFirst], td[class=colLast]").iterator(); //, div[class=block]

                    String modifier = null;
                    String name = null;
                    String trimmed = null;

                    while (iterator.hasNext()) {
                        modifier = iterator.next().text();
                        name = iterator.next().text();

                        trimmed = name.split("\\)", 2)[0];   // concept from:http://stackoverflow.com/questions/18220022/how-to-trim-a-string-after-a-specific-character-in-java
                        trimmed = trimmed + ")";

                        pstmt.setString(1, null);                 // ID
                        if (modifier.length() > 100) {
                            modifier = modifier.substring(0, 99);
                        }
                        pstmt.setString(2, modifier);             // modifier
                        if (trimmed.length() > 200) {
                            trimmed = trimmed.substring(0, 199);
                        }
                        pstmt.setString(3, trimmed);              // name
                        if (name.length() > 400) {
                            name = name.substring(0, 399);
                        }
                        pstmt.setString(4, name);                 // summary
                        pstmt.setString(5, null);                 // detail
                        pstmt.setString(6, klassID);              // klass ID
                        pstmt.executeUpdate();

                        methodRows++;

                    }
                }
                statement.close();

            } else {
                System.out.println("no matching method html file found");
            }

        } catch (NullPointerException npe) {
            System.out.println("in loadMethod method");
            npe.printStackTrace();
            System.out.println();
            totalNullPointerExceptions++;
        } catch (SQLException sqle) {
            System.out.println("in loadMethod method");
            sqle.printStackTrace();
            System.out.println();
            totalSQLExceptions++;
        }  catch (Exception e) {
            System.out.println("in loadMethod method");
            e.printStackTrace();
            System.out.println();
            totalOtherExceptions++;
        }



        totalMethodRows = totalMethodRows + methodRows;
        //sleep(500);
        System.out.println();
        System.out.println("Method: rows added = " + methodRows);
        System.out.println();

    }


    private static void loadConstructorTable(String searchname,
                                             String klassID,
                                             File directory,
                                             Connection connection) {

        int constructorRows = 0;

        Statement statement = null;
        java.sql.PreparedStatement pstmt = null;

        try {

            statement = connection.createStatement();
            pstmt = connection.prepareStatement("INSERT INTO constructor VALUES (?,?,?,?,?,?)");

        } catch (SQLException sqle) {
            System.out.println("in loadConstructor method");
            sqle.printStackTrace();
            System.out.println();
            totalSQLExceptions++;
        }


        String trimName = searchname.split("<", 2)[0];

        File newDir = directory.getParentFile();
        System.out.println("in loadConstructorTable: parent directory = " + newDir);
        String methodFile = newDir + "\\" + trimName + ".html";
        File testFile = new File(methodFile);

        System.out.println("in loadConstructorTable: filepath = " + methodFile);



        try {
            if (testFile.isFile()) {
                File input = new File(methodFile);
                //File input = new File("C:/Users/myrlin/Desktop/Java/JavaDocs/docs/api/java/util/Arraylist.html");
                Document doc = Jsoup.parse(input, "UTF-8");


                Element table = doc.select("table[summary=Constructor Summary table, listing constructors, and an explanation]").first();
                if (table != null) {
                    Iterator<Element> iterator = table.select("td[class=colFirst], td[class=colLast]").iterator(); //, div[class=block]

                    String modifier = null;
                    String name = null;
                    String trimmed = null;

                    while (iterator.hasNext()) {
                        modifier = iterator.next().text();
                        name = iterator.next().text();

                        trimmed = name.split("\\)", 2)[0];   // concept from:http://stackoverflow.com/questions/18220022/how-to-trim-a-string-after-a-specific-character-in-java
                        trimmed = trimmed + ")";

                        pstmt.setString(1, null);                 // ID
                        if (modifier.length() > 100) {
                            modifier = modifier.substring(0, 99);
                        }
                        pstmt.setString(2, modifier);             // modifier
                        if (trimmed.length() > 200) {
                            trimmed = trimmed.substring(0, 199);
                        }
                        pstmt.setString(3, trimmed);              // name
                        if (name.length() > 400) {
                            name = name.substring(0, 399);
                        }
                        pstmt.setString(4, name);                 // summary
                        pstmt.setString(5, null);                 // detail
                        pstmt.setString(6, klassID);              // klass ID
                        pstmt.executeUpdate();


                        constructorRows++;

                    }
                }
                statement.close();

            } else {
                System.out.println("no matching constructor html file found");
            }

        } catch (NullPointerException npe) {
            System.out.println("in loadConstructor method");
            npe.printStackTrace();
            System.out.println();
            totalNullPointerExceptions++;
        } catch (SQLException sqle) {
            System.out.println("in loadConstructor method");
            sqle.printStackTrace();
            System.out.println();
            totalSQLExceptions++;
        }  catch (Exception e) {
            System.out.println("in loadConstructor method");
            e.printStackTrace();
            System.out.println();
            totalOtherExceptions++;
        }



        totalConstructorRows = totalConstructorRows + constructorRows;
        //sleep(500);
        System.out.println();
        System.out.println("Constructor rows added = " + constructorRows);
        System.out.println();


    }


    private static void loadFieldTable(String searchname,
                                       String klassID,
                                       File directory,
                                       Connection connection) throws Exception {

        int fieldRows = 0;

        Statement statement = null;
        java.sql.PreparedStatement pstmt = null;

        try {
            statement = connection.createStatement();
            pstmt = connection.prepareStatement("INSERT INTO field VALUES (?,?,?,?,?,?)");
        } catch (SQLException sqle) {
            System.out.println("in loadField method");
            sqle.printStackTrace();
            System.out.println();
            totalSQLExceptions++;
        }


        String trimName = searchname.split("<", 2)[0];

        File newDir = directory.getParentFile();
        System.out.println("in loadFieldTable: parent directory = " + newDir);
        String methodFile = newDir + "\\" + trimName + ".html";
        File testFile = new File(methodFile);

        System.out.println("in loadFieldTable: filepath = " + methodFile);

        try {
            if (testFile.isFile()) {
                File input = new File(methodFile);
                //File input = new File("C:/Users/myrlin/Desktop/Java/JavaDocs/docs/api/java/util/Arraylist.html");
                Document doc = Jsoup.parse(input, "UTF-8");


                Element table = doc.select("table[summary=Field Summary table, listing fields, and an explanation]").first();
                if (table != null) {
                    Iterator<Element> iterator = table.select("td[class=colFirst], td[class=colLast]").iterator(); //, div[class=block]

                    String modifier = null;
                    String name = null;
                    String trimmed = null;

                    while (iterator.hasNext()) {
                        modifier = iterator.next().text();
                        name = iterator.next().text();

                        trimmed = name.split("\\)", 2)[0];   // concept from:http://stackoverflow.com/questions/18220022/how-to-trim-a-string-after-a-specific-character-in-java
                        trimmed = trimmed + ")";

                        pstmt.setString(1, null);                 // ID
                        if (modifier.length() > 100) {
                            modifier = modifier.substring(0, 99);
                        }
                        pstmt.setString(2, modifier);             // modifier
                        if (trimmed.length() > 200) {
                            trimmed = trimmed.substring(0, 199);
                        }
                        pstmt.setString(3, trimmed);              // name
                        if (name.length() > 400) {
                            name = name.substring(0, 399);
                        }
                        pstmt.setString(4, name);                 // summary
                        pstmt.setString(5, null);                 // detail
                        pstmt.setString(6, klassID);              // klass ID
                        pstmt.executeUpdate();

                        fieldRows++;

                    }
                }

                statement.close();

            } else {
                System.out.println("no matching field html file found");
            }

        } catch (NullPointerException npe) {
            System.out.println("in loadField method");
            npe.printStackTrace();
            System.out.println();
            totalNullPointerExceptions++;
        } catch (SQLException sqle) {
            System.out.println("in loadField method");
            sqle.printStackTrace();
            System.out.println();
            totalSQLExceptions++;
        }  catch (Exception e) {
            System.out.println("in loadField method");
            e.printStackTrace();
            System.out.println();
            totalOtherExceptions++;
        }



        totalFieldRows = totalFieldRows + fieldRows;
        //sleep(500);
        System.out.println();
        System.out.println("Field rows added = " + fieldRows);
        System.out.println();

    }


    private static void deleteTables(Connection connection) {


        try {
            Statement statement = connection.createStatement();

            statement.execute("DELETE FROM constructor");
            statement.execute("DELETE FROM field");
            statement.execute("DELETE FROM method");


            statement.execute("DELETE FROM klass");
            statement.execute("DELETE FROM annotation");
            statement.execute("DELETE FROM exception");
            statement.execute("DELETE FROM errors");
            statement.execute("DELETE FROM enums");

            statement.execute("DELETE FROM package");

            statement.close();

        } catch (SQLException sqle) {
            System.out.println("in deleteTables");
            sqle.printStackTrace();
            System.out.println();
            totalSQLExceptions++;
        }
    }



    // copied entirely from:
    // https://www.mkyong.com/java/search-directories-recursively-for-file-in-java/
    private static void searchForFiles(FileSearch fileSearch) {


        // this is where the search directory is specified each time the program is run.
        // first run against the java library, then run against the javax library.
        searchDirectory(new File("C:\\Users\\myrlin\\Desktop\\Java\\JavaDocs\\docs\\api\\javax"), "package-summary.html", fileSearch);

        int count = fileSearch.getResult().size();
        if(count ==0){
            System.out.println("\nNo result found!");
        }else{
            System.out.println("\nFound " + count + " result!\n");
            for (String matched : fileSearch.getResult()){
                System.out.println("Found : " + matched);
            }
        }
    }

    // copied entirely from:
    // https://www.mkyong.com/java/search-directories-recursively-for-file-in-java/
    public static void searchDirectory(File directory, String fileNameToSearch, FileSearch fileSearch) {

        fileSearch.setFileNameToSearch(fileNameToSearch);

        if (directory.isDirectory()) {
            search(directory, fileSearch);
        } else {
            System.out.println(directory.getAbsoluteFile() + " is not a directory!");
        }

    }

    // copied entirely from:
    // https://www.mkyong.com/java/search-directories-recursively-for-file-in-java/
    private static void search(File file, FileSearch fileSearch) {

        if (file.isDirectory()) {
            System.out.println("Searching directory ... " + file.getAbsoluteFile());

            //do you have permission to read this directory?
            if (file.canRead() && file.listFiles() != null) {
                for (File temp : file.listFiles()) {
                    // this is where the recursive call is made
                    if (temp.isDirectory()) {
                        search(temp, fileSearch);
                    } else {
                        if (fileSearch.getFileNameToSearch().equals(temp.getName().toLowerCase())) {
                            fileSearch.result.add(temp.getAbsoluteFile().toString());
                        }

                    }
                }

            } else {
                System.out.println(file.getAbsoluteFile() + "Permission Denied");
            }
        }

    }


    // this was used during testing/delevopment phase
    private static File getDirectory(File file) {

        File newDir = file.getParentFile();
        String newFileName = file.getName();

        System.out.println("directory = " + newDir.toString());
        System.out.println("file name = " + newFileName);

        return newDir;
    }

} // end class main

