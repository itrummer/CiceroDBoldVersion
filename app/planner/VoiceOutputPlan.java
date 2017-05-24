package planner;

import planner.elements.Scope;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a VoiceOutputPlan
 */
public class VoiceOutputPlan implements Speakable {
    List<Scope> scopes;
    String cachedResult;

    public VoiceOutputPlan(ArrayList<Scope> scopes) {
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
        if (cachedResult != null) {
            return cachedResult;
        }

        StringBuilder builder = new StringBuilder("");

        for (Scope scope : scopes) {
            if (scope.getContext() == null) {
                builder.append(scope.toSpeechText(inLongForm) + " ");
            }
        }

        for (Scope scope : scopes) {
            if (scope.getContext() != null) {
                builder.append(scope.toSpeechText(inLongForm) + " ");
            }
        }

        return builder.toString();
    }

    public int speechCost() {
        return toSpeechText(true).length();
    }

}
