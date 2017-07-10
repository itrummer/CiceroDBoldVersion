package planner.hybrid;

import planner.elements.Context;
import planner.elements.TupleCollection;

import java.util.Collection;

/**
 * Represents the pruning stage of the Apriori Algorithm applied to Context generation. After each Context
 * generation stage, we apply a pruning method to the generated Contexts and filter Contexts according
 * to some criteria related to the TupleCollection they were generated for. Subclasses should implement
 * the abstract method prune() which represents this criteria.
 */
public abstract class ContextPruner {

    /**
     * Performs the pruning stage of the Apriori Algorithm in which we select only the top Contexts from
     * a collection of candidate Contexts and prune out the others. Subclasses should implement their own
     * method of pruning in this abstract method.
     *
     * @param candidateContexts The collection of Contexts from which to filter out only the top
     * @param tupleCollection The collection of Tuples from which to compare quality of Contexts
     * @return A filtered collection of Contexts according to this ContextPruner's pruning method
     */
    public abstract Collection<Context> prune(Collection<Context> candidateContexts, TupleCollection tupleCollection);

    public abstract String getName();
}
