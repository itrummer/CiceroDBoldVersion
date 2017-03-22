package edu.cornell;

import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.AudioFormat;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;
import com.ibm.watson.developer_cloud.text_to_speech.v1.util.WaveUtils;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;


public class App 
{
    public static void main( String[] args )
    {
        Scanner scanner = new Scanner(System.in);

        while(true) {
            System.out.print("audiolizer> ");
            String input = scanner.nextLine();
            if (input.length() == 0) {
                continue;
            }

            TextToSpeech service = new TextToSpeech();
            service.setUsernameAndPassword("896e2e12-6a72-4ad9-908b-13d0ea6adc67", "ofpV28BqPARy");

            try {
                InputStream stream = service.synthesize(input, Voice.EN_ALLISON, AudioFormat.WAV).execute();
                InputStream in = WaveUtils.reWriteWaveHeader(stream);
                OutputStream out = new FileOutputStream("/Users/mabryan/Desktop/output.wav");
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
                out.close();
                in.close();
                stream.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
