package planner.elements;

import db.Tuple;
import planner.elements.Context;

import java.util.ArrayList;

/**
 * A Scope represents an optional context with a set tuples that match the context
 */
public class Scope {
    public Context context;
    public ArrayList<Tuple> tuples;
    String cachedResult;

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

    /**
     * Returns the String representation of this Scope. Caches the result so later calls
     * do not recalculate the result
     * @return The String representation of this Scope
     */
    public String toSpeechText() {
        if (cachedResult != null) {
            return cachedResult;
        }

        StringBuilder builder = new StringBuilder("");
        if (context == null) {
            for (Tuple tuple : tuples) {
                builder.append(tuple.toString());
            }
        } else {
//            TODO: won't be called yet, but implement for non-null contexts
        }

        cachedResult = builder.toString();
        return cachedResult;
    }
}
