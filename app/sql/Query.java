package sql;

import planner.elements.TupleCollection;
import util.DatabaseUtilities;

import java.sql.SQLException;

public class Query {
//    QUERY_1("model, dollars, pounds, inch_display", "macbooks"),
//    QUERY_2("restaurant, price, user_rating, cuisine", "restaurants"),
//    QUERY_3("restaurant, price, cuisine", "restaurants"),
//    QUERY_4("model, gigabytes_of_memory, gigabytes_of_storage, dollars", "macbooks"),
//    QUERY_5("restaurant, user_rating, area, category", "yelp", 50),
//    QUERY_6("restaurant, user_rating, price, reviews, area, category", "yelp"),
//    QUERY_7("team, wins, touchdowns, conference, total_points_against", "football", 20),
//    QUERY_8("model, operating_system, gigabytes_of_storage, gigabytes_of_ram", "phones", 20),
//    QUERY_9("model, core_processors, operating_system, grams, gigabytes_of_storage, gigabytes_of_ram", "phones", 20);

    private String[] attributes;
    private String relation;
    private String condition;
    private Integer limit;

    public Query(String[] attributes, String relation, String condition, Integer limit) {
        this.attributes = attributes;
        this.relation = relation;
        this.condition = condition;
        this.limit = limit;
    }

    public Query(String[] attributes, String relation) {
        this(attributes, relation, null, null);
    }

    public Query(String[] attributes, String relation, int limit) {
        this(attributes, relation, null, limit);
    }

    public String getQuery() {
        String attributeList = attributes[0];
        for (int i = 1; i < attributes.length; i++) {
            attributeList += ", " + attributes[i];
        }
        return "SELECT " + attributeList + " FROM " + relation + (condition != null ? " WHERE " + condition : "") + (limit != null ? " LIMIT " + limit : "");
    }

    public String getRelation() {
        return relation;
    }

    public int getColumns() {
        return attributes.length;
    }

    public TupleCollection getTupleCollection() throws SQLException {
        return DatabaseUtilities.executeQuery(getQuery());
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

}
