package planning;

/**
 * Interface to be implemented by objects that can be represented as speech
 */
public interface Speakable {
    String toSpeechText(boolean inLongForm);
}
