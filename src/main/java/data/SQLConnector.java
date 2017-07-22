package data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import planning.elements.Tuple;
import planning.elements.TupleCollection;
import planning.elements.Value;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLConnector {
    final static Logger logger = LoggerFactory.getLogger(SQLConnector.class);

    public SQLConnector() {

    }

    private Connection getConnection() throws SQLException {
        // TODO: database connection should be configurable based on environment
        return DriverManager.getConnection("jdbc:postgresql://localhost/audiolization_db",
                "postgres",
                "admin");
    }

    private TupleCollection buildTupleCollectionFromResultSet(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();

        List<String> attributes = new ArrayList<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            attributes.add(metaData.getColumnName(i));
        }

        TupleCollection tupleCollection = new TupleCollection(attributes);

        while (resultSet.next()) {
            Tuple tuple = new Tuple(attributes);
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                tuple.addValueAssignment(metaData.getColumnName(i), Value.createValueObject(resultSet.getObject(i)));
            }
            tupleCollection.addTuple(tuple);
        }

        return  tupleCollection;
    }

    public TupleCollection buildTupleCollectionFromQuery(String sql) throws SQLException {
        Connection connection = null;
        ResultSet resultSet = null;
        Statement statement = null;
        SQLException exception = null;
        TupleCollection tupleCollection = null;

        try {
            connection = getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            tupleCollection = buildTupleCollectionFromResultSet(resultSet);
        } catch (SQLException sqlException) {
            logger.error("Exception while executing query");
            exception = sqlException;
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    logger.error("Unexpected exception while closing ResultSet", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.error("Unexpected exception while closing Statement", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error("Unexpected exception while closing Connection", e);
                }
            }
        }

        if (exception != null) {
            throw exception;
        }

        return tupleCollection;
    }
}
