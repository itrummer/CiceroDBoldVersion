import java.sql.SQLException;
import java.util.ArrayList;
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
                String results = DatabaseUtilities.executeQuery(input);
                System.out.println("Results: " + results);
                // TODO: output results as speech
                // voiceGenerator.generateSpeech(input);
            } catch (SQLException e) {}
        }
    }
}
