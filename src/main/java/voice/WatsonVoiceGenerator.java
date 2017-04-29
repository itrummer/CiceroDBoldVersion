package voice;

import com.ibm.watson.developer_cloud.http.ServiceCall;
import com.ibm.watson.developer_cloud.http.ServiceCallback;
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
    ServiceCall<InputStream> serviceCall;
    Clip currentClip;

    public WatsonVoiceGenerator() {
        service = new TextToSpeech();
        service.setUsernameAndPassword("896e2e12-6a72-4ad9-908b-13d0ea6adc67", "ofpV28BqPARy");
    }

    /**
     *
     */
    public void synthesizeSpeech(String text) {

    }

    public void startSpeech() {

    }

    @Override
    public void generateSpeech(String text) {
        stopSpeech();
            serviceCall = service.synthesize(text, Voice.EN_ALLISON, AudioFormat.WAV);
            serviceCall.enqueue(new ServiceCallback<InputStream>() {
                public void onResponse(InputStream inputStream) {
                    try {
                        InputStream in = WaveUtils.reWriteWaveHeader(inputStream);
                        currentClip = AudioSystem.getClip();
                        AudioInputStream audioStream = AudioSystem.getAudioInputStream(in);
                        currentClip.open(audioStream);
                        currentClip.start();
                        audioStream.close();
                        in.close();
                        inputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                public void onFailure(Exception e) {
                    e.printStackTrace();
                }
            });
    }

    @Override
    public void stopSpeech() {
        if (currentClip != null) {
            currentClip.stop();
        }
    }
}
