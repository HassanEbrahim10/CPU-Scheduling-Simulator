package Os.project;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.Animation.Status;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SchedulerApp extends Application {
    private Scheduler currentScheduler;
    private double currentTime = 0.0; // Changed to double
    private final ObservableList<Process> processList = FXCollections.observableArrayList();
    private final HBox ganttTimeline = new HBox(0.0);
    private final TableView<Process> table = new TableView<>();
    private final Label statsLabel = new Label("Avg Wait: 0.00 | Avg Turnaround: 0.00");
    private final Label timeLabel = new Label("Total Time: 0.0");
    private Timeline simulationTimeline;
    private boolean isInitialized = false;
    private final TableColumn<Process, Integer> priorityCol = new TableColumn<>("Priority");

    public void start(Stage primaryStage) {
        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton staticRadio = new RadioButton("Static Mode");
        staticRadio.setToggleGroup(modeGroup);
        staticRadio.setSelected(true);
        RadioButton liveRadio = new RadioButton("Live Simulation");
        liveRadio.setToggleGroup(modeGroup);

        ComboBox<String> algoChoice = new ComboBox<>();
        algoChoice.getItems().addAll("FCFS", "SJF (Preemptive)", "SJF (Non-Preemptive)", "Priority (Preemptive)", "Priority (Non-Preemptive)", "Round Robin");
        algoChoice.setValue("FCFS");

        HBox modeBox = new HBox(15.0, new Label("Mode:"), staticRadio, liveRadio, new Label("   Algorithm:"), algoChoice);
        modeBox.setAlignment(Pos.CENTER_LEFT);

        TextField idInput = new TextField();
        idInput.setPromptText("ID");
        idInput.setPrefWidth(50.0);

        TextField arrivalInput = new TextField();
        arrivalInput.setPromptText("Arrival");
        arrivalInput.setPrefWidth(60.0);

        TextField burstInput = new TextField();
        burstInput.setPromptText("Burst");
        burstInput.setPrefWidth(60.0);

        TextField priorityInput = new TextField();
        priorityInput.setPromptText("Prio");
        priorityInput.setPrefWidth(50.0);

        TextField quantumInput = new TextField();
        quantumInput.setPromptText("Q");
        quantumInput.setPrefWidth(40.0);

        priorityInput.setDisable(true);
        quantumInput.setDisable(true);

        algoChoice.setOnAction((e) -> {
            boolean isPriority = algoChoice.getValue().contains("Priority");
            priorityInput.setDisable(!isPriority);
            this.priorityCol.setVisible(isPriority);
            quantumInput.setDisable(!algoChoice.getValue().equals("Round Robin"));
        });

        arrivalInput.disableProperty().bind(liveRadio.selectedProperty());

        Button addBtn = new Button("Add Process");
        addBtn.setStyle("-fx-background-color: #698fee; -fx-text-fill: white; -fx-font-weight: bold;");

        HBox inputBar = new HBox(10.0, idInput, arrivalInput, burstInput, priorityInput, quantumInput, addBtn);
        inputBar.setAlignment(Pos.CENTER_LEFT);

        Button runStaticBtn = new Button("Run Static Chart");
        runStaticBtn.setStyle("-fx-background-color: #27b050; -fx-text-fill: white; -fx-font-weight: bold;");

        Button playLiveBtn = new Button("Play Simulation");
        playLiveBtn.setStyle("-fx-background-color: #4ff321; -fx-text-fill: white; -fx-font-weight: bold;");

        Button pauseLiveBtn = new Button("Pause");
        pauseLiveBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;");

        Button clearBtn = new Button("Clear All");
        clearBtn.setStyle("-fx-background-color: #eb4f4f; -fx-text-fill: white; -fx-font-weight: bold;");

        runStaticBtn.visibleProperty().bind(staticRadio.selectedProperty());
        runStaticBtn.managedProperty().bind(staticRadio.selectedProperty());
        playLiveBtn.visibleProperty().bind(liveRadio.selectedProperty());
        playLiveBtn.managedProperty().bind(liveRadio.selectedProperty());
        pauseLiveBtn.visibleProperty().bind(liveRadio.selectedProperty());
        pauseLiveBtn.managedProperty().bind(liveRadio.selectedProperty());

        HBox controlBar = new HBox(10.0, runStaticBtn, playLiveBtn, pauseLiveBtn, clearBtn);
        controlBar.setAlignment(Pos.CENTER_LEFT);

        VBox topSection = new VBox(15.0, modeBox, inputBar, controlBar);
        topSection.setPadding(new Insets(15.0));
        topSection.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ccc; -fx-border-width: 0 0 1 0;");

        this.setupTable();
        this.statsLabel.setStyle("-fx-text-fill: #2a713f; -fx-font-weight: bold; -fx-font-size: 16px;");
        this.timeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        HBox resultsBar = new HBox(30.0, this.timeLabel, this.statsLabel);
        resultsBar.setPadding(new Insets(10.0));
        resultsBar.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ddd; -fx-border-width: 1 0 1 0;");
        resultsBar.setAlignment(Pos.CENTER_LEFT);

        modeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> this.clearAllState());

        addBtn.setOnAction((e) -> {
            try {
                // Parse as Double
                double arrTime = liveRadio.isSelected() ? this.currentTime : Double.parseDouble(arrivalInput.getText());
                double burst = Double.parseDouble(burstInput.getText());
                int prio = priorityInput.isDisabled() ? 0 : Integer.parseInt(priorityInput.getText());

                Process p = new Process(idInput.getText(), arrTime, burst, prio);
                this.processList.add(p);

                if (liveRadio.isSelected() && this.isInitialized && this.currentScheduler != null) {
                    this.currentScheduler.addProcess(p);
                }

                idInput.clear();
                if (staticRadio.isSelected()) arrivalInput.clear();
                burstInput.clear();
                priorityInput.clear();
            } catch (Exception ex) {
                new Alert(AlertType.ERROR, "Invalid input. Use numbers (e.g., 0.5)").showAndWait();
            }
        });

        runStaticBtn.setOnAction((e) -> {
            if (!this.processList.isEmpty()) {
                this.prepareSimulation(algoChoice.getValue(), quantumInput.getText());

                while(!this.currentScheduler.isComplete()) {
                    this.renderTick(this.currentScheduler.executeTick(this.currentTime), this.currentTime);
                    this.currentTime += 0.1; // Step by 0.1 for precision
                    this.currentTime = Math.round(this.currentTime * 10.0) / 10.0;
                }

                this.renderFinalTimeMarker();
                this.timeLabel.setText(String.format("Total Time: %.1f", this.currentTime));
                this.updateStats();
                this.table.refresh();
            }
        });

        playLiveBtn.setOnAction((e) -> {
            if (!this.isInitialized) {
                this.prepareSimulation(algoChoice.getValue(), quantumInput.getText());
                this.isInitialized = true;
                this.startTimeline();
            } else if (this.simulationTimeline != null && this.simulationTimeline.getStatus() != Status.RUNNING) {
                this.simulationTimeline.play();
            }
        });

        pauseLiveBtn.setOnAction((e) -> {
            if (this.simulationTimeline != null && this.simulationTimeline.getStatus() == Status.RUNNING) {
                this.simulationTimeline.pause();
            }
        });

        clearBtn.setOnAction((e) -> this.clearAllState());

        ScrollPane ganttScroll = new ScrollPane(this.ganttTimeline);
        ganttScroll.setPrefHeight(135.0);
        ganttScroll.setPannable(true);

        VBox root = new VBox(10.0, topSection, new Label(" Gantt Chart:"), ganttScroll, resultsBar, this.table);
        root.setPadding(new Insets(10.0));
        VBox.setVgrow(this.table, Priority.ALWAYS);

        primaryStage.setTitle("CPU Scheduler (Decimal Support)");
        primaryStage.setScene(new Scene(root, 1100.0, 700.0));
        primaryStage.show();
    }

    private void prepareSimulation(String algo, String q) {
        this.ganttTimeline.getChildren().clear();
        this.currentTime = 0.0;
        this.timeLabel.setText("Total Time: 0.0");
        double quantum = q.isEmpty() ? 1.0 : Double.parseDouble(q);

        switch (algo) {
            case "FCFS" -> this.currentScheduler = new FCFS();
            case "SJF (Preemptive)" -> this.currentScheduler = new SJF_Preemptive();
            case "SJF (Non-Preemptive)" -> this.currentScheduler = new SJF_NonPreemptive();
            case "Priority (Preemptive)" -> this.currentScheduler = new Priority_Preemptive();
            case "Priority (Non-Preemptive)" -> this.currentScheduler = new Priority_NonPreemptive();
            case "Round Robin" -> this.currentScheduler = new RoundRobin(quantum);
        }

        for(Process p : this.processList) {
            p.setRemainingTime(p.getBurstTime());
            p.setStartTime(-1.0);
            p.setCompletionTime(0.0);
            this.currentScheduler.addProcess(p);
        }
    }

    private void startTimeline() {
        if (this.simulationTimeline != null) {
            this.simulationTimeline.stop();
        }

        // Timeline still runs every second, but increments logic by 0.1
        this.simulationTimeline = new Timeline(new KeyFrame(Duration.seconds(0.1), (e) -> {
            if (this.currentScheduler.isComplete()) {
                this.simulationTimeline.stop();
                this.renderFinalTimeMarker();
                this.updateStats();
                this.table.refresh();
            } else {
                this.renderTick(this.currentScheduler.executeTick(this.currentTime), this.currentTime);
                this.currentTime += 0.1;
                this.currentTime = Math.round(this.currentTime * 10.0) / 10.0;
                this.timeLabel.setText(String.format("Total Time: %.1f", this.currentTime));
                this.table.refresh();
                this.updateStats();
            }
        }));
        this.simulationTimeline.setCycleCount(Timeline.INDEFINITE);
        this.simulationTimeline.play();
    }

    private void renderTick(Process active, double tickTime) {
        VBox unit = new VBox(2.0);
        unit.setAlignment(Pos.TOP_LEFT);
        StackPane block = new StackPane();
        Rectangle r = new Rectangle(45.0, 50.0);
        r.setFill(active == null ? Color.LIGHTGRAY : Color.web("#1976D2"));
        r.setStroke(Color.WHITE);
        Label idL = new Label(active == null ? "IDLE" : active.getId());
        idL.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        block.getChildren().addAll(r, idL);
        Label startL = new Label(String.format("%.1f", tickTime));
        startL.setStyle("-fx-font-size: 10px; -fx-text-fill: #333; -fx-font-weight: bold;");
        unit.getChildren().addAll(block, startL);
        this.ganttTimeline.getChildren().add(unit);
    }

    private void renderFinalTimeMarker() {
        Label finalTime = new Label(String.format("%.1f", this.currentTime));
        finalTime.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #333;");
        VBox finalBox = new VBox(finalTime);
        finalBox.setPadding(new Insets(52.0, 0, 0, 0));
        finalTime.setTranslateX(-4.0);
        this.ganttTimeline.getChildren().add(finalBox);
    }

    private void clearAllState() {
        if (this.simulationTimeline != null) this.simulationTimeline.stop();
        this.processList.clear();
        this.ganttTimeline.getChildren().clear();
        this.currentTime = 0.0;
        this.currentScheduler = null;
        this.isInitialized = false;
        this.timeLabel.setText("Total Time: 0.0");
        this.statsLabel.setText("Avg Wait: 0.00 | Avg Turnaround: 0.00");
    }

    private void updateStats() {
        if (!this.processList.isEmpty()) {
            double totalW = 0.0;
            double totalT = 0.0;
            for(Process p : this.processList) {
                totalW += p.getWaitingTime();
                totalT += p.getTurnaroundTime();
            }
            this.statsLabel.setText(String.format("Avg Wait: %.2f | Avg Turnaround: %.2f",
                    totalW / this.processList.size(), totalT / this.processList.size()));
        }
    }

    private void setupTable() {
        TableColumn<Process, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Changed to allow Double values
        TableColumn<Process, Double> arrCol = new TableColumn<>("Arrival");
        arrCol.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));

        TableColumn<Process, Double> brstCol = new TableColumn<>("Burst");
        brstCol.setCellValueFactory(new PropertyValueFactory<>("burstTime"));

        this.priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        this.priorityCol.setVisible(false);

        TableColumn<Process, Double> remCol = new TableColumn<>("Remaining");
        remCol.setCellValueFactory(new PropertyValueFactory<>("remainingTime"));

        this.table.getColumns().addAll(idCol, arrCol, brstCol, this.priorityCol, remCol);
        this.table.setItems(this.processList);
    }

    public static void main(String[] args) {
        launch(args);
    }
}