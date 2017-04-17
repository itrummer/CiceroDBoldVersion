package gui;

import concurrent.PlanningTask;
import db.DatabaseUtilities;
import db.TupleCollection;
import javafx.application.Application;
import javafx.concurrent.Task;
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

    Task<String> currentLinearProgrammingTask;

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
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text scenetitle = new Text("CiceroDB Demo");
        scenetitle.setStyle("-fx-font-family: Roboto, sans-serif; -fx-font-size: 40");
        grid.add(scenetitle, 0, 0);

        Label queryLabel = new Label("Query:");
        grid.add(queryLabel, 0, 1);

        final TextField queryInput = new TextField();
        queryInput.setStyle("-fx-font-family: Inconsolata, monospace; -fx-font-size: 24;");
        grid.add(queryInput, 0, 2);

        Button button = new Button("Run Query");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.CENTER);
        hbBtn.getChildren().add(button);
        grid.add(hbBtn, 0, 3);

        Label naiveLabel = new Label("Naive Plan");
        grid.add(naiveLabel, 0, 4);

        final TextArea naiveOutput = new TextArea();
        naiveOutput.setEditable(false);
        naiveOutput.setWrapText(true);
        grid.add(naiveOutput, 0, 5);

        final Button playNaiveButton = new Button("Play");
        grid.add(playNaiveButton, 1, 5);
        playNaiveButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                voiceGenerator.generateSpeech(naiveOutput.getText());
            }
        });

        final Button stopNaiveButton = new Button("Stop");
        grid.add(stopNaiveButton, 2, 5);
        stopNaiveButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                voiceGenerator.stopSpeech();
            }
        });

        final Label naiveCostLabel = new Label("Cost: ");
        grid.add(naiveCostLabel, 0, 6);

        final Label linearOutputLabel = new Label("Linear Programming Plan");
        grid.add(linearOutputLabel, 0, 7);

        final TextArea cplexOutput = new TextArea();
        cplexOutput.setEditable(false);
        cplexOutput.setWrapText(true);
        grid.add(cplexOutput, 0, 8);

        final Button playCplexButton = new Button("Play");
        grid.add(playCplexButton, 1, 8);
        playCplexButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                voiceGenerator.generateSpeech(cplexOutput.getText());
            }
        });

        final Button stopCplexButton = new Button("Stop");
        grid.add(stopCplexButton, 2, 8);
        stopCplexButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                voiceGenerator.stopSpeech();
            }
        });

        final Label cplexCostLabel = new Label("Cost: ");
        grid.add(cplexCostLabel, 0, 9);

        button.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
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

                final PlanningTask linearTask = new PlanningTask(results, new LinearProgrammingPlanner());
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

        Scene scene = new Scene(grid, 900, 800);
        scene.getStylesheets().add("https://fonts.googleapis.com/css?family=Inconsolata\n");
        scene.getStylesheets().add("https://fonts.googleapis.com/css?family=Roboto\n");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

}
