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

    public Scope(Context context) {
        this(context, new ArrayList<Tuple>());
    }

    /**
     * Constructor for a scope without a context
     */
    public Scope() {
        this(new ArrayList<Tuple>());
    }

    public void addMatchingTuple(Tuple t) {
        tuples.add(t);
    }

    public Context getContext() {
        return context;
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

        cachedResult = "";
        if (context == null) {
            for (Tuple tuple : tuples) {
                cachedResult += tuple + "\n";
            }
        } else {
            cachedResult = "Entries for ";
            cachedResult += context.toSpeechText();
        }

        return cachedResult;
    }
}
