import java.util.Scanner;

public class App
{
    public static String QUIT_COMMAND = "\\q";
    public static String HELP_COMMAND = "\\h";

    public static void main( String[] args )
    {
        Scanner scanner = new Scanner(System.in);
        VoiceGenerator voiceGenerator = new WatsonVoiceGenerator();

        while(true) {
            System.out.print("audiolizer> ");
            String input = scanner.nextLine();
            if (input.length() == 0) {
                continue;
            }

            // define some special commands and check if user has input one of them
            if (input.equals(QUIT_COMMAND)) {
                scanner.close();
                System.out.println("Bye");
                System.exit(0);
            } else if (input.equals(HELP_COMMAND)) {
                System.out.println("Help: Insert help information here...");
                continue;
            }

            voiceGenerator.generateSpeech(input);
        }
    }
}
