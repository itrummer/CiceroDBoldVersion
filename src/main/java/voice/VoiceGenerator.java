package voice;

/**
 * An abstract class for generating speech from an input text
 */
public abstract class VoiceGenerator {

    /**
     * Outputs text as speech
     * @param text The text to be converted to speech.
     */
    public abstract void generateSpeech(String text);
}
