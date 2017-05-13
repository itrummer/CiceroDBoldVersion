package planner.hybrid;

import planner.elements.Context;
import planner.elements.Tuple;
import planner.elements.TupleCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

/**
 * Filters candidate Contexts by calculating the number of Tuples each Context matches, then
 * returns the k Contexts that match the highest number of Tuples.
 */
public class TopKPruner extends ContextPruner {
    int k;

    public TopKPruner(int k) {
        this.k = k;
    }

    /**
     * Filters candidate Contexts by selecting the k Contexts that match the most Tuples from TupleCollection
     * @param candidateContexts The collection of Contexts from which to filter out only the top
     * @param tupleCollection The collection of Tuples from which to compare quality of Contexts
     * @return The top k Contexts from a collection of candidate Contexts
     */
    @Override
    public Collection<Context> prune(Collection<Context> candidateContexts, TupleCollection tupleCollection) {
        ContextNode[] sortedNodeList = contextListSortedByMatchingTupleCount(candidateContexts, tupleCollection);

        Collection<Context> result = new ArrayList<>();
        for (int c = 0; c < candidateContexts.size() && c < k; c++) {
            result.add(sortedNodeList[c].getContext());
        }

        return result;
    }

    public ContextNode[] calculateMatchingCounts(Collection<Context> candidateContexts, TupleCollection tupleCollection) {
        int i = 0;
        ContextNode[] nodeList = new ContextNode[candidateContexts.size()];
        for (Context c : candidateContexts) {
            int numberMatched = 0;
            for (Tuple t : tupleCollection) {
                if (c.matches(t)) {
                    numberMatched++;
                }
            }
            nodeList[i] = new ContextNode(c, numberMatched);
            i++;
        }
        return nodeList;
    }

    public ContextNode[] contextListSortedByMatchingTupleCount(Collection<Context> candidateContext, TupleCollection tupleCollection) {
        ContextNode[] nodeList = calculateMatchingCounts(candidateContext, tupleCollection);
        Arrays.sort(nodeList, new Comparator<ContextNode>() {
            @Override
            public int compare(ContextNode o1, ContextNode o2) {
                return o1.getMatchCount().compareTo(o2.getMatchCount());
            }
        });
        return nodeList;
    }

}
