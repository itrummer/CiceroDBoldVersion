package planner.hybrid;

import planner.elements.Context;
import planner.elements.Tuple;
import planner.elements.TupleCollection;

import java.util.*;

public class TupleCoveringPruner extends TopKPruner {

    public TupleCoveringPruner(int k) {
        super(k);
    }

    @Override
    public Collection<Context> prune(Collection<Context> candidateContexts, TupleCollection tupleCollection) {
        Map<Integer, Tuple> tupleMap = new HashMap<>();
        for (int i = 0; i < tupleCollection.tupleCount(); i++) {
            tupleMap.put(i, tupleCollection.getTuple(i));
        }

        Collection<Context> result = new ArrayList<>();
        Collection<Tuple> uncovered = new ArrayList<>(tupleCollection.getTuples());
        Collection<Context> remainingCandidates = new ArrayList<>(candidateContexts);

        while (result.size() < k && !uncovered.isEmpty() && remainingCandidates.size() > 0) {
            ContextNode[] nodeList = contextListSortedByMatchingTupleCount(remainingCandidates, uncovered);
            Context c = nodeList[0].getContext();
            remainingCandidates.remove(c);
            result.add(c);

            Iterator<Tuple> tupleIterator = uncovered.iterator();
            while (tupleIterator.hasNext()) {
                Tuple t = tupleIterator.next();
                if (c.matches(t)) {
                    tupleIterator.remove();
                }
            }
        }

        return result;
    }

    @Override
    public String getName() {
        return super.getName() + "-tuple-covering";
    }
    
}
