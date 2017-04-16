package gui;

import db.DatabaseUtilities;
import db.TupleCollection;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import planner.NaiveVoicePlanner;
import planner.VoiceOutputPlan;
import planner.VoicePlanner;

import java.sql.SQLException;


public class Demo extends Application {
    VoicePlanner planner;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JavaFX Welcome");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text scenetitle = new Text("CiceroDB Demo");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0);

        Label queryLabel = new Label("Enter query:");
        grid.add(queryLabel, 0, 1);

        final TextField queryInput = new TextField();
        grid.add(queryInput, 0, 2);

        Button button = new Button("Run Query");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.CENTER);
        hbBtn.getChildren().add(button);
        grid.add(hbBtn, 0, 3);

        final TextArea output = new TextArea();
        output.setFont(Font.font("Avenir Next", 12));
        output.setEditable(false);
        output.setWrapText(true);
        grid.add(output, 0, 4);


        planner = new NaiveVoicePlanner();

        button.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                try {
                    TupleCollection results = DatabaseUtilities.executeQuery(queryInput.getText());
                    if (results != null) {
                        VoiceOutputPlan outputPlan = planner.plan(results);
                        if (outputPlan != null) {
                            String speechText = outputPlan.toSpeechText();
                            output.setText(speechText);
                        } else {
                            output.setText("Error: output plan was null");
                        }
                    }
                } catch (SQLException e) {
                    output.setText(e.getMessage());
                }

            }
        });

        Scene scene = new Scene(grid, 700, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
