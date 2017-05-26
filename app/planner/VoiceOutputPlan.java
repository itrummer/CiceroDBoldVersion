package planner;

import planner.elements.Scope;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a VoiceOutputPlan
 */
public class VoiceOutputPlan implements Speakable {
    List<Scope> scopes;
    String longFormCachedResult;
    String shortFormCachedResult;

    public VoiceOutputPlan(List<Scope> scopes) {
        this.scopes = scopes;
    }

    public VoiceOutputPlan() {
        this(new ArrayList<>());
    }

    public void addScope(Scope scope) {
        scopes.add(scope);
    }

    /**
     * Formats this VoiceOutputPlan to speech text suitable for a VoiceGenerator
     * @return The String representation of the speech output for this plan
     */
    public String toSpeechText(boolean inLongForm) {
        if (inLongForm && longFormCachedResult != null) {
            return longFormCachedResult;
        } else if (!inLongForm && shortFormCachedResult != null) {
            return shortFormCachedResult;
        }

        StringBuilder builder = new StringBuilder("");

        boolean firstScope = true;

        for (Scope scope : scopes) {
            if (scope.getContext() == null) {
                builder.append(scope.toSpeechText(inLongForm));
                firstScope = false;
            }
        }

        for (Scope scope : scopes) {
            if (scope.getContext() != null) {
                if (!firstScope) {
                    builder.append(" ");
                    firstScope = false;
                }
                builder.append(scope.toSpeechText(inLongForm));
            }
        }

        if (inLongForm) {
            longFormCachedResult = builder.toString();
            return longFormCachedResult;
        } else {
            shortFormCachedResult= builder.toString();
            return shortFormCachedResult;
        }
    }

    public int speechCost() {
        return toSpeechText(true).length();
    }

}
