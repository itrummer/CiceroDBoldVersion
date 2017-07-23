package util;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;

/**
 * General utility class
 */
public class Utilities {

    public static int audioLengthInSeconds(String path) {
        File file = new File(path);
        AudioInputStream audioInputStream = null;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        AudioFormat format = audioInputStream.getFormat();
        long frames = audioInputStream.getFrameLength();
        int durationInSeconds = (int) ((frames+0.0) / format.getFrameRate());
        return durationInSeconds;
    }

}
