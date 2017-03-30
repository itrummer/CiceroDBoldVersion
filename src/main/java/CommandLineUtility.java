import db.DatabaseUtilities;
import db.TupleCollection;
import planner.NaiveVoicePlanner;
import planner.VoiceOutputPlan;
import voice.VoiceGenerator;
import voice.WatsonVoiceGenerator;

import java.sql.SQLException;
import java.util.Scanner;

public class CommandLineUtility
{
    public static String PROMPT = "audiolizer> ";
    public static String QUIT_COMMAND = "\\q";
    public static String HELP_COMMAND = "\\h";
    public static String HELP_INFO = "Help: \n\tspecial commands: \\q = quit, \\h = help";

    public static void main( String[] args )
    {
        Scanner scanner = new Scanner(System.in);
        VoiceGenerator voiceGenerator = new WatsonVoiceGenerator();

        while(true) {
            System.out.print(PROMPT);
            String input = scanner.nextLine();
            if (input.length() == 0) {
                continue;
            }

            // check if user has input a command
            if (input.equals(QUIT_COMMAND)) {
                scanner.close();
                System.out.println("Bye");
                System.exit(0);
            } else if (input.equals(HELP_COMMAND)) {
                System.out.println(HELP_INFO);
                continue;
            }

            try {
                TupleCollection results = DatabaseUtilities.executeQuery(input);
                if (results != null) {
                    NaiveVoicePlanner naiveVoicePlanner = new NaiveVoicePlanner();
                    VoiceOutputPlan outputPlan = naiveVoicePlanner.plan(results);
                    String speechText = outputPlan.toSpeechText();
                    System.out.println(speechText);
//                    voiceGenerator.generateSpeech(speechText);
                }
            } catch (SQLException e) {}
        }
    }
}
