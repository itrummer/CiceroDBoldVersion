package util;

import planning.elements.TupleCollection;

import java.sql.*;
import java.util.Map;

/**
 * Convenience methods for setting up connections to the PostgreSQL database and extracting result sets
 */
public class DatabaseUtilities {
    static int MAX_ROWS = 100;

    /**
     * For now, returns a String representation of the ResultSet after executing the query
     * @param sql The query to execute
     * @return String representation of the results of executing sql
     * @throws SQLException
     */
    public static TupleCollection executeQuery(String sql) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        TupleCollection tupleCollection = null;

        SQLException sqlException = null;

        try {
            Map<String, String> env = System.getenv();

            // default for local environment
            String url = "jdbc:postgresql://localhost/audiolization_db";
            String user = "postgres";
            String password = "admin";

            // override for Heroku
            if (env.containsKey("JDBC_DATABASE_URL")) {
                url = env.get("JDBC_DATABASE_URL");
            }
            if (env.containsKey("JDBC_DATABASE_USERNAME")) {
                user = env.get("JDBC_DATABASE_USERNAME");
            }
            if (env.containsKey("JDBC_DATABASE_PASSWORD")) {
                password = env.get("JDBC_DATABASE_PASSWORD");
            }

            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();
            statement.setMaxRows(MAX_ROWS);
            resultSet = statement.executeQuery(sql);

            tupleCollection = TupleCollection.rowCollectionFromResultSet(resultSet);

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

        return tupleCollection;
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
