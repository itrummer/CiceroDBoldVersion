package planner.hybrid;

import planner.elements.Context;
import planner.elements.Scope;
import planner.elements.Tuple;
import planner.elements.TupleCollection;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Implementation of a ContextPruner to select only the Contexts that are useful, or not useless. A Context
 * is useless if
 */
public class UsefulPruner extends ContextPruner {

    @Override
    public Collection<Context> prune(Collection<Context> candidateContexts, TupleCollection tupleCollection) {
        Collection<Context> pruned = new ArrayList<>();
        for (Context c : candidateContexts) {
            if (!useless(c, tupleCollection)) {
                pruned.add(c);
            }
        }
        return pruned;
    }

    boolean useless(Context c, TupleCollection tupleCollection) {
        int totalSavings = 0;
        for (Tuple t : tupleCollection) {
            if (c.matches(t)) {
                int tWithContext = t.toSpeechText(c, true).length();
                int tWithoutContext = t.toSpeechText(true).length();
                totalSavings += tWithoutContext - tWithContext;
            }
        }
        return c.toSpeechText(true).length() + Scope.contextOverheadCost() >= totalSavings;
    }
}
