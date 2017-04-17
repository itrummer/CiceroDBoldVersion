package voice;

import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.AudioFormat;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;
import com.ibm.watson.developer_cloud.text_to_speech.v1.util.WaveUtils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.InputStream;

/**
 * A VoiceGenerator implemented using IBM's Watson service to generate and play audio representation of text.
 */
public class WatsonVoiceGenerator extends VoiceGenerator {
    TextToSpeech service;
    Clip currentClip;

    public WatsonVoiceGenerator() {
        service = new TextToSpeech();
        service.setUsernameAndPassword("896e2e12-6a72-4ad9-908b-13d0ea6adc67", "ofpV28BqPARy");
    }

    @Override
    public void generateSpeech(String text) {
        stopSpeech();
        try {
            InputStream stream = service.synthesize(text, Voice.EN_ALLISON, AudioFormat.WAV).execute();
            InputStream in = WaveUtils.reWriteWaveHeader(stream);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(in);
            currentClip = AudioSystem.getClip();
            currentClip.open(audioStream);
            currentClip.start();
            audioStream.close();
            in.close();
            stream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopSpeech() {
        if (currentClip != null) {
            currentClip.stop();
        }
    }
}
