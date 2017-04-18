package gui;

import concurrent.PlanningTask;
import javafx.scene.layout.ColumnConstraints;
import util.DatabaseUtilities;
import planner.elements.TupleCollection;
import javafx.application.Application;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import planner.*;
import voice.VoiceGenerator;
import voice.WatsonVoiceGenerator;
import java.sql.SQLException;


public class Demo extends Application {
    VoiceGenerator voiceGenerator;
    int nextRow = 0;

    // UI Elements
    Text scenetitle;
    ChoiceBox contextSizeOptions;
    TextField numericalDomainSizeField;
    ChoiceBox categoricalDomainSizeOptions;
    TextField queryInput;
    ChoiceBox sampleQueryChoices;
    TextArea naiveOutput;
    Button playNaiveButton;
    Button stopNaiveButton;
    Label naiveCostLabel;
    Label cplexCostLabel;
    Label linearOutputLabel;
    TextArea cplexOutput;
    Button playCplexButton;
    Button stopCplexButton;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        voiceGenerator = new WatsonVoiceGenerator();
        primaryStage.setTitle("CiceroDB Demo");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(40, 40, 40, 40));
        grid.getColumnConstraints().add(new ColumnConstraints(750));

        scenetitle = new Text("CiceroDB Demo");
        scenetitle.setStyle("-fx-font-family: Roboto, sans-serif; -fx-font-size: 40");
        grid.addRow(getNextRow(), scenetitle);

        // Describe the schemas of the test tables for clarity

        Label macbooksLabel = new Label("macbooks : (model, inches, memory, storage, dollars, gigahertz, processor, \n\t\thours_battery_life, trackpad, pounds)");
        macbooksLabel.setStyle("-fx-font-family: Inconsolata, monospace; -fx-font-size: 16;");
        Label restaurantsLabel = new Label("restaurants : (restaurant, rating, price, cuisine)");
        restaurantsLabel.setStyle("-fx-font-family: Inconsolata, monospace; -fx-font-size: 16;");
        Label footballLabel = new Label("football : (team, wins, losses, win_percentage, total_points_for, \n\t\ttotal_points_against, net_points_scored, touchdowns, conference)");
        footballLabel.setStyle("-fx-font-family: Inconsolata, monospace; -fx-font-size: 16;");
        grid.addRow(getNextRow(),macbooksLabel);
        grid.addRow(getNextRow(), restaurantsLabel);
        grid.addRow(getNextRow(), footballLabel);

        // CONFIG OPTIONS

        contextSizeOptions = new ChoiceBox();
        contextSizeOptions.getItems().addAll(1, 2, 3, 4);
        contextSizeOptions.getSelectionModel().select(2);
        HBox box1 = new HBox(8, new Label("Maximum Context Size:"), contextSizeOptions);
        box1.alignmentProperty().set(Pos.CENTER_LEFT);
        grid.addRow(getNextRow(), box1);

        numericalDomainSizeField = new TextField();
        numericalDomainSizeField.setText("2.0");
        HBox box2 = new HBox(8, new Label("Maximum Allowable Upperbound:"), numericalDomainSizeField);
        box2.setAlignment(Pos.CENTER_LEFT);
        grid.addRow(getNextRow(), box2);

        categoricalDomainSizeOptions = new ChoiceBox();
        categoricalDomainSizeOptions.getItems().addAll(1, 2, 3);
        categoricalDomainSizeOptions.getSelectionModel().select(1);
        HBox box3 = new HBox(8, new Label("Maximum Categorical Domain Size:"), categoricalDomainSizeOptions);
        grid.addRow(getNextRow(), box3);

        // INPUT ELEMENTS

        grid.addRow(getNextRow(), new Label("Query:"));

        queryInput = new TextField();
        queryInput.setStyle("-fx-font-family: Inconsolata, monospace; -fx-font-size: 24;");

        sampleQueryChoices = new ChoiceBox();
        sampleQueryChoices.getItems().addAll(
                "SELECT * FROM RESTAURANTS;",
                "SELECT team, wins, touchdowns FROM football;",
                "SELECT model, memory, storage, dollars FROM macbooks;"
        );
        sampleQueryChoices.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if (sampleQueryChoices.getValue() != null) {
                    queryInput.setText((String) sampleQueryChoices.getValue());
                }
            }
        });

        grid.addRow(getNextRow(), sampleQueryChoices);
        grid.addRow(getNextRow(), queryInput);

        Button button = new Button("Run Query");
        HBox hbBtn = new HBox(10, button);
        hbBtn.setAlignment(Pos.CENTER);
        grid.addRow(getNextRow(),hbBtn);

        // OUTPUT ELEMENTS

        grid.addRow(getNextRow(), new Label("Naive Plan"));

        naiveOutput = new TextArea();
        naiveOutput.setEditable(false);
        naiveOutput.setWrapText(true);
        grid.addRow(getNextRow(), naiveOutput);

        playNaiveButton = new Button("Play");
        stopNaiveButton = new Button("Stop");
        HBox box4 = new HBox(8, playNaiveButton, stopNaiveButton);
        box4.setAlignment(Pos.CENTER_LEFT);
        grid.addRow(getNextRow(), box4);

        naiveCostLabel = new Label("Cost: ");
        grid.addRow(getNextRow(), naiveCostLabel);

        linearOutputLabel = new Label("Linear Programming Plan");
        grid.addRow(getNextRow(), linearOutputLabel);

        cplexOutput = new TextArea();
        cplexOutput.setEditable(false);
        cplexOutput.setWrapText(true);
        grid.addRow(getNextRow(), cplexOutput);

        playCplexButton = new Button("Play");
        stopCplexButton = new Button("Stop");

        HBox box5 = new HBox(8, playCplexButton, stopCplexButton);
        box5.setAlignment(Pos.CENTER_LEFT);
        grid.addRow(getNextRow(), box5);

        cplexCostLabel = new Label("Cost: ");
        grid.addRow(getNextRow(), cplexCostLabel);

        playNaiveButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                voiceGenerator.generateSpeech(naiveOutput.getText());
            }
        });

        stopNaiveButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                voiceGenerator.stopSpeech();
            }
        });

        playCplexButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                voiceGenerator.generateSpeech(cplexOutput.getText());
            }
        });

        stopCplexButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                voiceGenerator.stopSpeech();
            }
        });

        button.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                // CHECK CONFIG PARAMETERS

                double mW;
                try {
                    mW = Double.parseDouble(numericalDomainSizeField.getText());
                } catch (NumberFormatException e) {
                    System.err.println("Error: numerical domain parameter is not a double");
                    return;
                }

                Integer mS = null;
                try {
                    mS = (Integer) contextSizeOptions.getValue();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                Integer mC = null;
                try {
                    mC = (Integer) categoricalDomainSizeOptions.getValue();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                if (mS == null || mC == null) {
                    return;
                }

                final TupleCollection results;
                try {
                    results = DatabaseUtilities.executeQuery(queryInput.getText());
                } catch (SQLException e) {
                    naiveOutput.setText(e.getMessage());
                    return;
                }

                if (results == null) {
                    naiveOutput.setText("Error: null result");
                    return;
                }

                naiveOutput.setText("Evaluating...");
                cplexOutput.setText("Evaluating...");

                // run the naive planning in a background thread

                final PlanningTask naiveTask = new PlanningTask(results, new NaiveVoicePlanner());
                naiveTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    public void handle(WorkerStateEvent event) {
                        VoiceOutputPlan plan = naiveTask.getValue();
                        String speechText = plan.toSpeechText();
                        naiveOutput.setText(speechText);
                        naiveCostLabel.setText("Cost: " + speechText.length());
                    }
                });

                naiveTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
                    public void handle(WorkerStateEvent event) {
                        cplexOutput.setText("Error while running Naive Planner");
                    }
                });

                Thread naiveThread = new Thread(naiveTask);
                naiveThread.setDaemon(true);
                naiveThread.start();

                // run the linear planning in a background thread

                final PlanningTask linearTask = new PlanningTask(results, new LinearProgrammingPlanner(mS, mW, mC));
                linearTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    public void handle(WorkerStateEvent event) {
                        VoiceOutputPlan plan = linearTask.getValue();
                        String speechText = plan.toSpeechText();
                        cplexOutput.setText(speechText);
                        cplexCostLabel.setText("Cost: " + speechText.length());
                    }
                });

                linearTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
                    public void handle(WorkerStateEvent event) {
                        cplexOutput.setText("Error while running Linear Programming Planner");
                    }
                });

                Thread linearThread = new Thread(linearTask);
                linearThread.setDaemon(true);
                linearThread.start();
            }
        });

        Scene scene = new Scene(grid);
        scene.getStylesheets().add("https://fonts.googleapis.com/css?family=Inconsolata\n");
        scene.getStylesheets().add("https://fonts.googleapis.com/css?family=Roboto\n");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    public int getNextRow() {
        int result = nextRow;
        nextRow++;
        return result;
    }
}
