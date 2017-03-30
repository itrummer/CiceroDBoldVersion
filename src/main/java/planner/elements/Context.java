package planner.elements;

import db.Tuple;

/**
 * A set of value assignments
 */
public class Context {

    /**
     * Determines if a given tuple matches this context. A Tuple matches a Context if it has the same ValueAssignments
     * for all ValueAssignments in this Context.
     * @param tuple The Tuple to check for a match
     * @return True if tuple matches this context, else false.
     */
    public boolean match(Tuple tuple) {
        return false;
    }
}
