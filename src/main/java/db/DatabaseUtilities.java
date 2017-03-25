package db;

import java.sql.*;

/**
 * Convenience methods for setting up connections to the PostgreSQL database and extracting result sets
 */
public class DatabaseUtilities {
    static String url = "jdbc:postgresql://localhost/audiolization_db";
    static String user = "postgres";
    static String password = "admin";
    static int MAX_ROWS = 100;

    /**
     * For now, returns a String representation of the ResultSet after executing the query
     * @param sql The query to execute
     * @return String representation of the results of executing sql
     * @throws SQLException
     */
    public static RowCollection executeQuery(String sql) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        RowCollection rowCollection = null;

        SQLException sqlException = null;

        try {
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();
            statement.setMaxRows(MAX_ROWS);
            resultSet = statement.executeQuery(sql);

            rowCollection = RowCollection.rowCollectionFromResultSet(resultSet);

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

        return rowCollection;
    }

    public static void printSQLException(SQLException ex) {

        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                if (!ignoreSQLException(((SQLException)e).getSQLState())) {
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
