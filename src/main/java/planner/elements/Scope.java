package planner.elements;

import java.util.ArrayList;

/**
 * A Scope represents an optional context with a set tuples that match the context
 */
public class Scope {
    static final String PRECONTEXT_PHRASE = "Entries for ";
    static final String POSTCONTEXT_PHRASE = ": ";

    Context context;
    ArrayList<Tuple> tuples;
    String cachedLongFormResult;
    String cachedShortResult;

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
     * Constructor for a Scope with one Context
     * @param context
     */
    public Scope(Context context) {
        this(context, new ArrayList<Tuple>());
    }

    /**
     * Constructor for a scope with an empty context
     */
    public Scope() {
        this(new ArrayList<Tuple>());
    }

    /**
     * Adds Tuple t to this Scope as a Tuple that matches this Scope's Contexts
     * @param t A matching Tuple
     */
    public void addMatchingTuple(Tuple t) {
        tuples.add(t);
    }

    /**
     * Returns this Scope's Context. May be null
     */
    public Context getContext() {
        return context;
    }

    /**
     *
     * @return
     */
    public static int contextOverheadCost() {
        return PRECONTEXT_PHRASE.length() + POSTCONTEXT_PHRASE.length();
    }

    /**
     * Returns the String representation of this Scope. Caches the result so later calls
     * do not recalculate the result
     * @return The String representation of this Scope
     */
    public String toSpeechText(boolean inLongForm) {
        if (cachedLongFormResult != null && inLongForm) {
            return cachedLongFormResult;
        } else if (cachedShortResult != null && !inLongForm) {
            return cachedShortResult;
        }

        String result = "";

        result += context == null ? "" : PRECONTEXT_PHRASE + context.toSpeechText(inLongForm) + POSTCONTEXT_PHRASE;

        for (int i = 0; i < tuples.size(); i++) {
            Tuple t = tuples.get(i);
            if (tuples.size() > 1 && i == tuples.size()-1) {
                result += "and ";
            }
            boolean firstAttribute = true;
            for (String attribute : t.getAttributes()) {
                if (context == null || !context.isAttributeFixed(attribute)) {
                    Value v = t.valueForAttribute(attribute);
                    if (firstAttribute) {
                        result += v.toSpeechText(inLongForm);
                    } else {
                        result += ", " + v.toSpeechText(inLongForm) + " " + attribute;
                    }
                    firstAttribute = false;
                }
            }
            result += i == tuples.size()-1 ? ".  " : ", ";
        }

        if (inLongForm) {
            cachedLongFormResult = result;
        } else {
            cachedShortResult = result;
        }

        return result;
    }

    public int numberTuples() {
        return tuples.size();
    }
}
