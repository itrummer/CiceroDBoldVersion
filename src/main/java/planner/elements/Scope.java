package planner.elements;

import db.Tuple;
import planner.elements.Context;
import values.Value;

import java.util.ArrayList;
import java.util.Map;

/**
 * A Scope represents an optional context with a set tuples that match the context
 */
public class Scope {
    public static final String PRECONTEXT_PHRASE = "Entries for ";
    public static final String POSTCONTEXT_PHRASE = " are: ";

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
            for (int i = 0; i < tuples.size(); i++) {
                Tuple t = tuples.get(i);
                cachedResult += t.toSpeechText();
                if (i != tuples.size()-1) {
                    cachedResult += ", ";
                }
            }
        } else {
            cachedResult += PRECONTEXT_PHRASE + context.toSpeechText() + POSTCONTEXT_PHRASE;
            for (Tuple t : tuples) {
                for (Map.Entry<String, Value> entry : t.getValueAssignments().entrySet()) {
                    if (!context.isAttributeFixed(entry.getKey())) {
                        cachedResult += entry.getKey() + ": " + entry.getValue().toSpeechText() + ", ";
                    }
                }
            }
            cachedResult = cachedResult.substring(0, cachedResult.length() - 2);
        }

        return cachedResult;
    }
}
