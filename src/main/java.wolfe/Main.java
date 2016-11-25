//package java.wolfe;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Scanner;

import static java.lang.System.exit;

/*
 * Created by Jeremy on 11/17/2016.
 */

public class Main {

    static Scanner stringScanner = new Scanner(System.in);
    static Scanner numberScanner = new Scanner(System.in);


    // setup the database driver
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    //http://stackoverflow.com/questions/34189756/warning-about-ssl-connection-when-connecting-to-mysql-database
    static final String DB_CONNECTION_URL = "jdbc:mysql://localhost:3306/java_api?autoReconnect=true&useSSL=false";
    static final String USER = "myrlin";
    static final String PASSWORD = "password";

    public static void main(String[] args) throws Exception { //TODO handle exceptions properly

        FileSearch fileSearch = new FileSearch();

        deleteTables();

        searchForFiles(fileSearch); // copied entirely from:
                                    // https://www.mkyong.com/java/search-directories-recursively-for-file-in-java/

        int items = 0;
        for (String dir : fileSearch.getResult()){
            items++;
            System.out.println(items + "  processing dir = " + dir);

            String packageFK = loadPackageTable(dir);

            loadKlassTable(packageFK, dir);

            }

    }




    private static String loadPackageTable(String dir) throws Exception {

        Class.forName(JDBC_DRIVER);

        Connection connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
        Statement statement = connection.createStatement();

        java.sql.PreparedStatement pstmt = connection.prepareStatement("INSERT INTO package VALUES (?,?,?)");
        java.sql.PreparedStatement sstmt = connection.prepareStatement("SELECT * FROM package WHERE name = ?");

        //File input = new File("C:/Users/myrlin/Desktop/Java/JavaDocs/docs/api/java/util/package-summary.html");
        File input = new File(dir);
        Document doc = Jsoup.parse(input, "UTF-8");


        String foreignKey = null;

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
            pstmt.setString(2, nameField);
            pstmt.setString(3, description);
            pstmt.executeUpdate();


            //ResultSet rs = statement.executeQuery("SELECT * FROM package");
            sstmt.setString(1, nameField);
            ResultSet selectRS = sstmt.executeQuery();

            int i = 0;
            while (selectRS.next()) {
                i++;
            }

            System.out.println("package row count off select statement = " + i);

            selectRS.first();
                System.out.println("ID: " + selectRS.getString(1));
                foreignKey = selectRS.getString(1);
    //            System.out.println("Name: " + rs.getString(2));
    //            System.out.println("Description: " + rs.getString(3));
    //            System.out.println();

            selectRS.close();


        } catch (Exception e) {
            System.out.println("in loadPackage method");
            e.printStackTrace();
            System.out.println();
        }


        statement.close();
        connection.close();

        return foreignKey;

    } // end loadPackageTable



    private static void loadKlassTable(String packageFK, String dir) throws Exception {

        Connection connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
        Statement statement = connection.createStatement();

        java.sql.PreparedStatement pstmt = connection.prepareStatement("INSERT INTO klass VALUES (?,?,?,?,?)");
        java.sql.PreparedStatement sstmt = connection.prepareStatement("SELECT * FROM klass WHERE name = ?");


        //File input = new File("C:/Users/myrlin/Desktop/Java/JavaDocs/docs/api/java/util/package-summary.html");
        File input = new File(dir);
        Document doc = Jsoup.parse(input, "UTF-8");



        // Element table = doc.select("table[summary=Interface Summary table, listing interfaces, and an explanation]").first();
        Element table = doc.select("table[summary=Class Summary table, listing classes, and an explanation]").first();


        String inputName = null;
        try {
            if (table.hasText()) {

                Iterator<Element> iterator = table.select("td").iterator();
                int count = 1;
                while(iterator.hasNext()) {

                    inputName = iterator.next().text();
                    if (inputName.length() > 200) {
                        inputName = inputName.substring(0, 199);
                    }
                    String inputDescription = iterator.next().text();
                    if (inputDescription.length() > 400) {
                        inputDescription = inputDescription.substring(0, 399);
                    }

                    pstmt.setString(1, null);
                    pstmt.setString(2, "1");
                    pstmt.setString(3, inputName);
                    pstmt.setString(4, inputDescription);
                    pstmt.setString(5, packageFK);
                    pstmt.executeUpdate();
                }


                // ResultSet rs = statement.executeQuery("SELECT * FROM klass");
                sstmt.setString(1, inputName);
                ResultSet selectRS = sstmt.executeQuery();

                File currentDir = new File(dir);

                while (selectRS.next()) {
                    System.out.println("Klass ID: " + selectRS.getString(1));
//                    System.out.println("Type: " + rs.getString(2));
                    System.out.println("Klass Name: " + selectRS.getString(3));
//                    System.out.println("Description: " + rs.getString(4));
//                    System.out.println("Foreign Key: " + rs.getString(5));
//                    System.out.println();

                    loadMethodTable(selectRS.getString(3), selectRS.getString(1), currentDir);
                }
            }
        }
        catch (Exception e) {
            System.out.println("in loadKlass method");
            e.printStackTrace();
            System.out.println();
        }

//        rs.close();
        statement.close();
        connection.close();

    } // end loadKlassTable




    private static void loadMethodTable(String searchname, String klassFK, File directory) throws Exception {

        Connection connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
        Statement statement = connection.createStatement();

        java.sql.PreparedStatement pstmt = connection.prepareStatement("INSERT INTO method VALUES (?,?,?,?,?,?)");

        File newDir = directory.getParentFile();
        System.out.println("in loadMethodTable: parent directory = " + newDir);
        String methodFile = newDir + "\\" + searchname + ".html";
        File testFile = new File(methodFile);

        System.out.println("in loadMethodTable: filepath = " + methodFile);


        try {
            if (testFile.isFile()) {
                File input = new File(methodFile);
                //File input = new File("C:/Users/myrlin/Desktop/Java/JavaDocs/docs/api/java/util/Arraylist.html");
                Document doc = Jsoup.parse(input, "UTF-8");


                Element table = doc.select("table[summary=Method Summary table, listing methods, and an explanation]").first();
                Iterator<Element> iterator = table.select("td[class=colFirst], td[class=colLast]").iterator(); //, div[class=block]

                String type = null;                       // type should be called modifier
                String name = null;
                String trimmed = null;

                while (iterator.hasNext()) {
                    type = iterator.next().text();
                    name = iterator.next().text();

                    trimmed = name.split("\\)", 2)[0];   // concept from:http://stackoverflow.com/questions/18220022/how-to-trim-a-string-after-a-specific-character-in-java
                    trimmed = trimmed + ")";

                    pstmt.setString(1, null);                 // ID
                    pstmt.setString(2, type);                 // type should be called modifier
                    pstmt.setString(3, trimmed);              // name
                    pstmt.setString(4, name);                 // summary
                    pstmt.setString(5, null);                 // detail
                    pstmt.setString(6, klassFK);              // klass FK
                    pstmt.executeUpdate();
                }

                statement.close();
                connection.close();
            } else {
                System.out.println("no matching method html file found");
            }


        } catch (Exception e) {
            System.out.println("in loadMethod");
            e.printStackTrace();
            System.out.println();
        }

    }



    private static void deleteTables() throws Exception {

        Class.forName(JDBC_DRIVER);
        Connection connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);

        Statement statement = connection.createStatement();

        statement.execute("DELETE FROM constant");
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
        connection.close();

    }

    private static void holdStuff() throws Exception{

        Class.forName(JDBC_DRIVER);
        Connection connection = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);

        Statement statement = connection.createStatement();

        /*
        statement.execute("CREATE TABLE IF NOT EXISTS CubeInfo (THING VARCHAR(50), SECSOLVE FLOAT)");
        statement.execute("INSERT INTO CubeInfo VALUES ('Cubestormer II robot', 5.27) ");
        statement.execute("INSERT INTO CubeInfo VALUES ('Fakhri Raihaan (using his feet)', 27.93) ");
        statement.execute("INSERT INTO CubeInfo VALUES ('Ruxin Liu (age 3)', 99.33) ");
        statement.execute("INSERT INTO CubeInfo VALUES ('Mats Valk (human record holder)', 6.27) ");
        */

        ResultSet rs = statement.executeQuery("SELECT * FROM package");
        while (rs.next()) {
            System.out.println("ID: " + rs.getString(1));
            System.out.println("Name: " + rs.getString(2));
            System.out.println("Description: " + rs.getString(3));
            System.out.println();
        }

        rs = statement.executeQuery("SELECT * FROM klass");
        while (rs.next()) {
            System.out.println("ID: " + rs.getString(1));
            System.out.println("Name: " + rs.getString(2));
            System.out.println("Description: " + rs.getString(3));
            System.out.println("FK ID: " + rs.getString(4));
            System.out.println();
        }

        /*
        System.out.println("Enter the thing's name: ");
        String name = stringScanner.nextLine();
        System.out.println("Enter the thing's time to solve in seconds: ");
        float solveTime = numberScanner.nextFloat();

        // https://www.tutorialspoint.com/javaexamples/jdbc_prepared_statement.htm
        java.sql.PreparedStatement pstmt = connection.prepareStatement("INSERT INTO CubeInfo VALUES (?,?)");
        pstmt.setString(1, name );
        pstmt.setFloat(2, solveTime );
        pstmt.executeUpdate();

        pstmt = connection.prepareStatement("UPDATE CubeInfo SET SECSOLVE=5.55 WHERE THING='Mats Valk (human record holder)'");
        pstmt.executeUpdate();


        rs = statement.executeQuery("SELECT * FROM CubeInfo");
        while (rs.next()) {
            System.out.println("The Thing is " + rs.getString(1));
            System.out.println("The Time taken is " + rs.getFloat(2));
            System.out.println();
        }

        statement.execute("DROP TABLE CubeInfo");
        */



        // statement.execute("DROP TABLE klass");
        // statement.execute("DROP TABLE package");
        statement.execute("DELETE FROM package");
        statement.execute("DELETE FROM klass");


        rs.close();
        statement.close();
        connection.close();



    }


    // copied entirely from:
    // https://www.mkyong.com/java/search-directories-recursively-for-file-in-java/
    private static void searchForFiles(FileSearch fileSearch) {

        //FileSearch fileSearch = new FileSearch();

        //try different directory and filename :)
        //  fileSearch.searchDirectory(new File("/Users/mkyong/websites"), "post.php");
        searchDirectory(new File("C:\\Users\\myrlin\\Desktop\\Java\\JavaDocs\\docs\\api\\java"), "package-summary.html", fileSearch);
        //searchDirectory(new File("C:\\Users\\myrlin\\Desktop\\Java\\JavaDocs\\docs\\api"), "package-summary.html", fileSearch);

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

        // FileSearch fileSearch = new FileSearch();

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

        //  FileSearch fileSearch = new FileSearch();

        if (file.isDirectory()) {
            System.out.println("Searching directory ... " + file.getAbsoluteFile());

            //do you have permission to read this directory?
            if (file.canRead() && file.listFiles() != null) {
                for (File temp : file.listFiles()) {
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


    private static File getDirectory(File file) {

        File newDir = file.getParentFile();
        String newFileName = file.getName();

        System.out.println("directory = " + newDir.toString());
        System.out.println("file name = " + newFileName);

        return newDir;
    }

} // end class main

