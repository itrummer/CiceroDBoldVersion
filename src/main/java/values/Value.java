package values;

import planner.Speakable;

/**
 *
 */
public abstract class Value implements Speakable {
    public abstract String toSpeechText();
}
