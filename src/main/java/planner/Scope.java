package planner;

import db.Tuple;

import java.util.ArrayList;

/**
 * A Scope represents an optional context with a set tuples that match the context
 */
public class Scope {
    public Context context;
    public ArrayList<Tuple> tuples;

    /**
     * Constructor for a Scope with a context
     * @param context The context that the tuples match
     * @param tuples The tuples in this scope
     */
    public Scope(Context context, ArrayList<Tuple> tuples) {
        this.context = context;
        this.tuples = tuples;
    }

    /**
     * Constructor for a Scope without a context
     * @param tuples The tuples in this scope
     */
    public Scope(ArrayList<Tuple> tuples) {
        this(null, tuples);
    }
}
