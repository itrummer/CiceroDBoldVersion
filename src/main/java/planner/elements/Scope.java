package planner.elements;

import planner.Speakable;

import java.util.ArrayList;
import java.util.List;

/**
 * A Scope represents an optional context with a set tuples that match the context
 */
public class Scope implements Speakable {
    static final String PRECONTEXT_PHRASE = "Entries with ";
    static final String POSTCONTEXT_PHRASE = ": ";
    static final String SCOPE_END_STRING = ".";
    static final String TUPLE_SEPARATOR_STRING = ", ";

    Context context;
    List<Tuple> tuples;
    String cachedLongFormResult;
    String cachedShortResult;

    /**
     * Constructor for a Scope with a context
     * @param context The context that the tuples match
     * @param tuples The tuples in this scope
     */
    public Scope(Context context, List<Tuple> tuples) {
        this.context = context;
        this.tuples = tuples;
    }

    /**
     * Constructor for a Scope without a context
     * @param tuples The tuples in this scope
     */
    public Scope(List<Tuple> tuples) {
        this(null, tuples);
    }

    /**
     * Constructor for a Scope with one Context
     * @param context
     */
    public Scope(Context context) {
        this(context, new ArrayList<>());
    }

    /**
     * Constructor for a scope with an empty context
     */
    public Scope() {
        this(new ArrayList<>());
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
     * Returns the speech cost of using a context
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

        StringBuilder result = new StringBuilder(context == null ? "" : PRECONTEXT_PHRASE + context.toSpeechText(inLongForm) + POSTCONTEXT_PHRASE);

        int i = 0;
        for (Tuple t : tuples) {
            result.append(t.toSpeechText(context, inLongForm));
            result.append(i == tuples.size()-1 ? SCOPE_END_STRING : TUPLE_SEPARATOR_STRING);
            i++;
        }

        if (inLongForm) {
            cachedLongFormResult = result.toString();
        } else {
            cachedShortResult = result.toString();
        }

        return (inLongForm ? cachedLongFormResult : cachedShortResult);
    }

    public int numberTuples() {
        return tuples.size();
    }
}
