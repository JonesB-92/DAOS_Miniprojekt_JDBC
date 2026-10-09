package DAOS_Projekt;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Locale;
import java.util.Scanner;

public class DaosProjekt {

    /**
     * @param args
     */
    static Connection minConnection;
    static Statement stmt;
    static BufferedReader inLine;

    private static int yesNoToBit(String input) {
        return input.equalsIgnoreCase("y") ? 1 : 0;
    }

    /**
     * a. Lav et Java-program, der anvender JDBC. Programmet skal oprette et eksamensforsøg for en
     * given studerende for en given afvikling af en eksamen. Via programmet skal du kunne
     * indtaste data for et eksamensforsøg for én studerende. Brugeren skal selv indtaste
     * identifikation på eksamensafviklingen og på den studerende udover
     * karakteren/den administrative bedømmelse. Der forventes rimelige SQL-fejlreaktioner.
     */
    public static void insertKarakterMedPrepStmt() {
        try {
            // indlæsning
            System.out.println("Vi vil nu oprette en ny karakter med prepared statement: ");
            System.out.println("Indtast studienr (personen skal være oprettet på forhånd) (100)");
            String studentNrStr = inLine.readLine();
            System.out.println("Indtast EksamenID (Eksamen skal være oprettet på forhånd) (10000)");
            String eksamenID = inLine.readLine();
            System.out.println("Indtast karakter");
            String karakterStr = inLine.readLine();
            System.out.println("Indtast hvilket forsøg det er (1., 2. eller 3.?) ");
            String forsogStr = inLine.readLine();
            System.out.println("Indtast fraværsårsag (fx 'SY', 'IA' eller 'IM') eller lad stå tom):");
            String fraveraarsagStr = inLine.readLine();
            if (fraveraarsagStr.trim().isEmpty()) {
                fraveraarsagStr = null;
            } else if (
                    !fraveraarsagStr.equalsIgnoreCase("SY") ||
                            !fraveraarsagStr.equalsIgnoreCase("IA") ||
                            !fraveraarsagStr.equalsIgnoreCase("IM")) {
                System.out.println("Det indtastede skal være et af de tre eksempler. ");
            }

            String bestaaetStr;
            if (Integer.parseInt(karakterStr) >= 2) {
                bestaaetStr = "1";
            } else {
                bestaaetStr = "0";
            }

            // Anvendelse af prepared statement
            String sql = "insert into karakter (karakter, antalforsoeg, fravaersaarsag, bestaaet, fk_studienr, fk_eksamenid) "
                    + "values (?, ?, ?, ?, ?, ?)";
            PreparedStatement prestmt = minConnection.prepareStatement(sql);
            prestmt.clearParameters();

            prestmt.setInt(1, Integer.parseInt(karakterStr));
            prestmt.setString(2, forsogStr);
            prestmt.setString(3, fraveraarsagStr);
            prestmt.setInt(4, Integer.parseInt(bestaaetStr));
            prestmt.setInt(5, Integer.parseInt(studentNrStr));
            prestmt.setInt(6, Integer.parseInt(eksamenID));
            // Udf�rer s�tningen
            prestmt.execute();
            // p�nt svar til brugeren
            System.out.println("Karakteren er nu registreret");
            if (!minConnection.isClosed()) minConnection.close();
        } catch (SQLException e) {
            switch (e.getErrorCode()) {
                // fejl-kode 2627 svarer til primary key fejl
                case 2627: {
                    System.out.println("Den pågældende karakter er allerede oprettet");
                    break;
                }
                case 547: {
                    String msg = e.getMessage();
                    String fejlkode = "";
                    if (msg.contains("studienr")) {
                        fejlkode += "Studienr findes ikke";
                    } else if (msg.contains("eksamenId")) {
                        fejlkode += "eksamenID findes ikke";
                    }
                    System.out.println(fejlkode);
                    break;
                }
                default:
                    System.out.println("fejlSQL:  " + e.getMessage());
            }
            ;
        } catch (Exception e) {
            System.out.println("fejl:  " + e.getMessage());
        }
    }


    /**
     * b. Lav et java-program, der anvender JDBC. Programmet skal oprette en eksamensafvikling. Via
     * programmet skal du indtaste data for en eksamensafvikling. Brugeren skal indtaste termin,
     * startdato, slutdato og hvilken eksamen afviklingen hører til. Der forventes rimelige SQLfejlreaktioner.
     */
    public static void insertEksamenMedString() {
        try {
            // indlæsning
//			insert into eksamen(eksamensNavn, startdato, slutdato, termin, stopprove, mundtlig, skriftlig)
//			values('DB Eksamen', '2024-06-01','2024-06-02','S2024',1,1,0);
            System.out.println("Vi vil nu oprette en eksamen med stringmanipulation (eksamensNavn, startdato, slutdato, termin, stopprove, mundtlig, skriftlig)");
            System.out.println("Indtast eksamensnavn");
            String eksamensStr = inLine.readLine();
            // Startdato
            Date startDato = null;
            while (true) {
                System.out.println("Indtast start dato: yyyy-mm-dd");
                String dato = inLine.readLine();

                try {
                    startDato = Date.valueOf(dato);
                    break; // breaker hvis succesfuld
                } catch (IllegalArgumentException ex) {
                    System.out.println("Dato skal være i formatet yyyy-mm-dd.");
                }
            }
            // Slutdato
            Date slutDato = null;
            while (true) {
                System.out.println("Indtast slut dato: yyyy-mm-dd");
                String dato = inLine.readLine();

                try {
                    slutDato = Date.valueOf(dato);
                    if (slutDato.equals(startDato) || slutDato.after(startDato)) {
                        break; // breaker hvis succesfuld
                    } else System.out.println("Slutdato skal være efter startdato.");
                } catch (IllegalArgumentException ex) {
                    System.out.println("Dato skal være i formatet yyyy-mm-dd.");
                }
            }
            // Termin check char(5)
            String terminStr = null;
            while (true) {
                System.out.println("Indtast termin (S/V202X)");
                terminStr = inLine.readLine();

                try {
                    if (terminStr.length() == 5) {
                        break;
                    } else throw new IllegalArgumentException("Termin skal være 5 tegn.");
                } catch (IllegalArgumentException ex) {
                    System.out.println(ex.getMessage());
                }
            }

            System.out.println("Er det en stopprøve? y/n = 1/x");
            int stopprøveStr = yesNoToBit(inLine.readLine());
            System.out.println("Mundtlig? y/n = 1/x");
            int mundtligStr = yesNoToBit(inLine.readLine());
            System.out.println("Skriftlig? y/n = 1/x");
            int skriftligStr = yesNoToBit(inLine.readLine());

//          insert into eksamen(eksamensNavn, startdato, slutdato, termin, stopprove, mundtlig, skriftlig)
//			values('DB Eksamen', '2024-06-01','2024-06-02','S2024',1,1,0);
            // sender insert'en til db-serveren
            String sql = "insert into eksamen (" + "eksamensNavn," + " startdato," + " slutdato," + " termin," + " stopprove," + " mundtlig," + " skriftlig) "
                    + "values ('"
                    + eksamensStr + "', '"
                    + startDato + "', '"
                    + slutDato + "', '"
                    + terminStr + "', "
                    + stopprøveStr + ", "
                    + mundtligStr + ", "
                    + skriftligStr + ");";
            System.out.println("SQL-streng er: \n'" + sql + "'");
            stmt.execute(sql);
            // pænt svar til brugeren
            System.out.println("Eksamen er nu registreret.");
            if (!minConnection.isClosed()) minConnection.close();
        } catch (SQLException e) {
            switch (e.getErrorCode())
            // fejl-kode 547 svarer til en foreign key fejl
            {
                case 547: {
                    System.out.println("ukendt fremmednøglefejl");
                    break;
                }
                // fejl-kode 2627 svarer til primary key fejl
                case 2627: {
                    System.out.println("den pågældende eksamen er allerede oprettet");
                    break;
                }
                default:
                    System.out.println("fejlSQL:  " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("fejl:  " + e.getMessage());
        }

    }

    public static void insertEksamenMedPreparedStmt() {
        try {
            // indlæsning
            // insert into eksamen(eksamensNavn, startdato, slutdato, termin, stopprove, mundtlig, skriftlig)
            // values('DB Eksamen', '2024-06-01','2024-06-02','S2024',1,1,0);
            System.out.println("Vi vil nu oprette en eksamen med prepared statement (eksamensNavn, startdato, slutdato, termin, stopprove, mundtlig, skriftlig)");
            System.out.println("Indtast eksamensnavn");
            String eksamensStr = inLine.readLine();
            // Startdato
            Date startDato = null;
            while (true) {
                System.out.println("Indtast start dato: yyyy-mm-dd");
                String dato = inLine.readLine();

                try {
                    startDato = Date.valueOf(dato);
                    break; // breaker hvis succesfuld
                } catch (IllegalArgumentException ex) {
                    System.out.println("Dato skal være i formatet yyyy-mm-dd.");
                }
            }
            // Slutdato
            Date slutDato = null;
            while (true) {
                System.out.println("Indtast slut dato: yyyy-mm-dd");
                String dato = inLine.readLine();

                try {
                    slutDato = Date.valueOf(dato);
                    if (slutDato.equals(startDato) || slutDato.after(startDato)) {
                        break; // breaker hvis succesfuld
                    } else System.out.println("Slutdato skal være efter startdato.");
                } catch (IllegalArgumentException ex) {
                    System.out.println("Dato skal være i formatet yyyy-mm-dd.");
                }
            }
            // Termin check char(5)
            String terminStr = null;
            while (true) {
                System.out.println("Indtast termin (S/V202X)");
                terminStr = inLine.readLine();

                try {
                    if (terminStr.length() == 5) {
                        break;
                    } else throw new IllegalArgumentException("Termin skal være 5 tegn.");
                } catch (IllegalArgumentException ex) {
                    System.out.println(ex.getMessage());
                }
            }

            System.out.println("Er det en stopprøve? y/n = 1/x");
            int stopprøveStr = yesNoToBit(inLine.readLine());
            System.out.println("Mundtlig? y/n = 1/x");
            int mundtligStr = yesNoToBit(inLine.readLine());
            System.out.println("Skriftlig? y/n = 1/x");
            int skriftligStr = yesNoToBit(inLine.readLine());

            // Anvendelse af prepared statement
            String sql = "insert into eksamen (eksamensNavn, startdato, slutdato, termin, stopprove, mundtlig, skriftlig) " +
                    "values (?,?,?,?,?,?,?)";
            PreparedStatement prestmt = minConnection.prepareStatement(sql);
            prestmt.clearParameters();
            prestmt.setString(1, eksamensStr);
            prestmt.setDate(2, startDato);
            prestmt.setDate(3, slutDato);
            prestmt.setString(4, terminStr);
            prestmt.setInt(5, stopprøveStr);
            prestmt.setInt(6, mundtligStr);
            prestmt.setInt(7, skriftligStr);
            // Udfører sætningen
            prestmt.execute();
            // p�nt svar til brugeren
            System.out.println("Eksamen er nu registreret.");
            if (!minConnection.isClosed()) minConnection.close();
        } catch (SQLException e) {
            switch (e.getErrorCode())
            // fejl-kode 547 svarer til en foreign key fejl
            {
                case 547: {
                    System.out.println("ukendt fremmednøglefejl");
                    break;
                }
                // fejl-kode 2627 svarer til primary key fejl
                case 2627: {
                    System.out.println("den pågældende eksamen er allerede oprettet");
                    break;
                }
                default:
                    System.out.println("fejlSQL:  " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("fejl:  " + e.getMessage());
        }
    }

    /**
     * c. Lav et java-program, der anvender JDBC. Via programmet skal du indtaste navnet på en
     * given eksamen og en given termin. Programmet skal som resultat vise en liste af de
     * studerende, der har deltaget i denne afvikling af denne eksamen i denne termin. Udover
     * den studerendes navn og id, skal karakteren også være med i resultatet.
     */
    public static void selectEksamenMedParm() {
        try {
            // Indlæsning af søgeparametre
            System.out.println("Indtast eksamensnavn ('DB Eksamen' eller 'OS Eksamen') :");
            String eksamensNavn = inLine.readLine();

            // Termin check char(5)
            String terminStr = null;
            while (true) {
                System.out.println("Indtast termin (S2024 hvis DB Eksamen/V2024 hvis OS Eksamen)");
                terminStr = inLine.readLine();
                try {
                    if (terminStr.length() == 5) {
                        break;
                    } else throw new IllegalArgumentException("Termin skal være 5 tegn.");
                } catch (IllegalArgumentException ex) {
                    System.out.println(ex.getMessage());
                }
            }

            // SQL med prepared statement
            String sql = """
                    SELECT s.studienr, s.navn, k.karakter
                    FROM studerende s
                    JOIN karakter k ON s.studienr = k.fk_studienr
                    JOIN eksamen e ON k.fk_eksamenid = e.eksamenId
                    WHERE e.eksamensNavn = ? AND e.termin = ?
                    """;

            PreparedStatement prestmt = minConnection.prepareStatement(sql);
            prestmt.setString(1, eksamensNavn);
            prestmt.setString(2, terminStr);

            ResultSet rs = prestmt.executeQuery();

            boolean found = false;
            System.out.println("\nResultater:");
            System.out.println("------------------------------------");

            while (rs.next()) {
                int studienr = rs.getInt("studienr");
                String navn = rs.getString("navn");
                int karakter = rs.getInt("karakter");

                System.out.printf("Studienr: %d | Navn: %s | Karakter: %d%n",
                        studienr, navn, karakter);
                found = true;
            }

            if (!found) {
                System.out.println("Ingen studerende fundet for denne eksamen/termin.");
            }

            rs.close();
            prestmt.close();
            if (!minConnection.isClosed()) minConnection.close();


        } catch (SQLException e) {
            switch (e.getErrorCode()) {
                case 207: // column not found
                    System.out.println("Kolonnenavn findes ikke");
                    break;
                default:
                    System.out.println("SQL-fejl: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Fejl: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            inLine = new BufferedReader(new InputStreamReader(System.in));
            //generel opsætning
            //via native driver
            String server = "localhost\\SQLEXPRESS"; //virker måske hos dig
            //virker det ikke - prøv kun med localhost
            String dbnavn = "KarakterSystemDB";            //virker måske hos dig
            String login = "sa";                     //skal ikke ændres
            Scanner scanner = new Scanner(System.in);
            System.out.println("Indtast PW:- ) ");
            String password = scanner.next();
//            String password = "PeterFårEtRødtÆbel,HvisViFårEnRigtigGodBedømmelse :- )"; //skal IKKE!! ændres
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            minConnection = DriverManager.getConnection("jdbc:sqlserver://" + server + ";databaseName=" + dbnavn +
                    ";user=" + login + ";password=" + password + ";");
            //minConnection = DriverManager.getConnection("jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=eksempeldb;user=sa;password=torben07;");
            stmt = minConnection.createStatement();
            //Indlæsning og kald af den rigtige metode
            System.out.println("Indtast:");
            System.out.println("Aprep for insert karakter med prepared statement");
            System.out.println("Bstr for insert eksamen med  strengmanipulation");
            System.out.println("Bprep for insert eksamen med prepared statement");
            System.out.println("Cprep for insert med prepared statement");
            String in = inLine.readLine().toLowerCase();
            switch (in) {
                case "aprep": {
                    insertKarakterMedPrepStmt();
                    break;
                }
                case "bstr": {
                    insertEksamenMedString();
                    break;
                }
                case "bprep": {
                    insertEksamenMedPreparedStmt();
                    break;
                }
                case "cprep": {
                    selectEksamenMedParm();
                    break;
                }
                default:
                    System.out.println("ukendt indtastning");
            }
        } catch (Exception e) {
            System.out.println("fejl:  " + e.getMessage());
        }
    }

}

