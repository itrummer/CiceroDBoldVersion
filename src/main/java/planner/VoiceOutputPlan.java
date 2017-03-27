package planner;

import java.util.ArrayList;

/**
 * Representation of a VoiceOutputPlan
 */
public class VoiceOutputPlan {

    public ArrayList<Scope> scopes;

    /**
     * Formats this VoiceOutputPlan to speech text suitable for a VoiceGenerator
     * @return The String representation of the speech output for this plan
     */
    public String toSpeechText() {
        return "";
    }

}
