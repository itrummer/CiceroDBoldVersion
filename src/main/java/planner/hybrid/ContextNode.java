package planner.hybrid;

import planner.elements.Context;

class ContextNode {
    Context context;
    Integer matchCount;

    public ContextNode(Context context, int matchCount) {
        this.context = context;
        this.matchCount = matchCount;
    }

    public Integer getMatchCount() {
        return matchCount;
    }

    public Context getContext() {
        return context;
    }
}