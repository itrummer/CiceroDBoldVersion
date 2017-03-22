package edu.cornell;

import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.AudioFormat;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;
import com.ibm.watson.developer_cloud.text_to_speech.v1.util.WaveUtils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.InputStream;
import java.util.Scanner;


public class App 
{
    public static void main( String[] args )
    {
        Scanner scanner = new Scanner(System.in);
        TextToSpeech service = new TextToSpeech();
        service.setUsernameAndPassword("896e2e12-6a72-4ad9-908b-13d0ea6adc67", "ofpV28BqPARy");

        while(true) {
            System.out.print("audiolizer> ");
            String input = scanner.nextLine();
            if (input.length() == 0) {
                continue;
            }

            try {
                InputStream stream = service.synthesize(input, Voice.EN_ALLISON, AudioFormat.WAV).execute();
                InputStream in = WaveUtils.reWriteWaveHeader(stream);
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(in);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();
                audioStream.close();
                in.close();
                stream.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
