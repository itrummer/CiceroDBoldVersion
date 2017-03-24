
import java.sql.*;
import java.util.ArrayList;

/**
 * Convenience methods for setting up connections to the PostgreSQL database and extracting result sets
 */
public class DatabaseUtilities {
    static String url = "jdbc:postgresql://localhost/audiolization_db";
    static String user = "postgres";
    static String password = "admin";
    static int MAX_ROWS = 100;

    public static ArrayList<String[]> executeQuery(String sql) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        ArrayList<String[]> resultList = new ArrayList();

        SQLException sqlException = null;

        try {
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();
            statement.setMaxRows(MAX_ROWS);
            resultSet = statement.executeQuery(sql);
            ResultSetMetaData metaData = resultSet.getMetaData();

            // TODO: implement a Row class to handle ResultSet and the mapping of SQL to Java types
            while (resultSet.next()) {
                String[] row = new String[metaData.getColumnCount()];
                for (int i = 0; i < metaData.getColumnCount(); i++) {
                    String value = resultSet.getString(i);
                    row[i] = value;
                }
                resultList.add(row);
            }
        } catch (SQLException e) {
            printSQLException(e);
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException s) {}
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException s) {}
            try {
                if (statement != null) {
                    connection.close();
                }
            } catch (SQLException s) {}
        }

        if (sqlException != null) {
            throw sqlException;
        }
        return resultList;
    }

    public static void printSQLException(SQLException ex) {

        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                if (!ignoreSQLException(((SQLException)e).getSQLState())) {
                    e.printStackTrace(System.err);
                    System.err.println("SQLState: " + ((SQLException)e).getSQLState());
                    System.err.println("Error Code: " + ((SQLException)e).getErrorCode());
                    System.err.println("Message: " + e.getMessage());

                    Throwable t = ex.getCause();
                    while(t != null) {
                        System.out.println("Cause: " + t);
                        t = t.getCause();
                    }
                }
            }
        }
    }

    public static boolean ignoreSQLException(String sqlState) {
        if (sqlState == null) {
            System.out.println("The SQL state is not defined!");
            return false;
        }
        // X0Y32: Jar file already exists in schema
        if (sqlState.equalsIgnoreCase("X0Y32"))
            return true;
        // 42Y55: Table already exists in schema
        if (sqlState.equalsIgnoreCase("42Y55"))
            return true;
        return false;
    }
}
