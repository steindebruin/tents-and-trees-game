package org.example;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class TentsAndTrees extends Application {
    private static final int CELL_SIZE = 60;

    private Difficulty selectedDifficulty = Difficulty.Easy;
    private GameBoard board;
    private Button[][] gridButtons;
    private Label[] rowLabels, colLabels;
    private VBox rulesPanel;
    private boolean rulesVisible = false;
    private Stage mainStage;
    private Image treeImage, tentImage, flowerImage;

    /**
     * Called automatically when the JavaFX application starts.
     * @param stage the primary stage for this application, provided by JavaFX
     */
    @Override
    public void start(Stage stage) {
        mainStage = stage;
        loadImages();
        showStartScreen();
    }

    /**
     * Loads the images for trees, tents, and flowers. Assumes images are located in resources directory.
     */
    private void loadImages() {
        treeImage = new Image(getClass().getResourceAsStream("/tree.png"));
        tentImage = new Image(getClass().getResourceAsStream("/tent.png"));
        flowerImage = new Image(getClass().getResourceAsStream("/flower.png"));
    }

    /**
     * Builds and shows the game's start screen: the title, subtitle, difficulty selection, and start button.
     * Also sets up the layout using VBox and HBox containers.
     */
    private void showStartScreen() {
        VBox titleBlock = createTitleBlock();
        VBox layout = new VBox(10, titleBlock, createSpacer(40), createDifficultyBlock());
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("title-screen-bg");
        Scene scene = new Scene(layout, 600, 400);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        mainStage.setScene(scene);
        mainStage.show();
    }

    /**
     * Helper method for constructing the title block of the start screen.
     * @return the title block
     */
    private VBox createTitleBlock() {
        Label title = new Label("Tents and Trees");
        title.getStyleClass().add("label-title");
        Label subtitle = new Label("A Puzzle Game of Logic and Nature");
        subtitle.getStyleClass().add("label-subtitle");
        VBox titleBlock = new VBox(2, title, subtitle);
        titleBlock.setAlignment(Pos.CENTER);
        return titleBlock;
    }

    /**
     * Helper method for creating space in the layout.
     * @param height the amount of space
     * @return a region of empty space
     */
    private Region createSpacer(double height) {
        Region spacer = new Region();
        spacer.setPrefHeight(height);
        return spacer;
    }

    /**
     * Helper method for constructing the difficulty selection block, containing the three difficulty buttons.
     * @return the difficulty block
     */
    private VBox createDifficultyBlock() {
        Label diffLabel = new Label("Select difficulty:");
        diffLabel.getStyleClass().add("label-difficulty");
        HBox diffButtons = new HBox(15,
                difficultyButton("Easy", Difficulty.Easy),
                difficultyButton("Medium", Difficulty.Medium),
                difficultyButton("Hard", Difficulty.Hard));
        diffButtons.setAlignment(Pos.CENTER);
        Button startButton = createButton("Start Game");
        startButton.getStyleClass().addAll("button", "button-start");
        startButton.setOnAction(new StartGameHandler());
        VBox diffBlock = new VBox(10, diffLabel, diffButtons, startButton);
        diffBlock.setAlignment(Pos.CENTER);
        return diffBlock;
    }

    /**
     * Creates a new GameBoard instance by generating a new puzzle for the selected difficulty, using the
     * PuzzleGenerator class.
     * @param d the difficulty selected by the player
     */
    private void setupBoard(Difficulty d) {
        board = PuzzleGenerator.generate(d);
    }

    /**
     * Constructs and displays the main game interface, including the board grid, rule panel, and game control buttons.
     * The layout slightly adapts for different difficulty levels due to the size of the board.
     */
    private void showGameScreen() {
        GridPane grid = createBoardGrid(selectedDifficulty.size);
        rulesPanel = createRulesPanel();
        rulesVisible = true;
        rulesPanel.setVisible(true);

        Button showRules = controlButton(rulesVisible ? "Hide Rules" : "Show Rules", "button-rules");
        showRules.setOnAction(new ShowRulesHandler(showRules));

        Button resetButton = controlButton("Reset Game", "button-reset");
        resetButton.setOnAction(new ResetHandler(resetButton));

        Button exitButton = controlButton("Exit Game", "button-exit");
        exitButton.setOnAction(new ExitHandler(exitButton));

        Scene scene;
        // Different layout for difficulty 'Hard' as this grid is larger (works the best in full screen play)
        if (selectedDifficulty == Difficulty.Hard) {
            grid.setScaleX(0.85);
            grid.setScaleY(0.85);
            VBox rightPanel = createRightPanel(rulesPanel, resetButton, exitButton, showRules);
            Group gridGroup = new Group(grid);
            StackPane gridContainer = new StackPane(gridGroup);
            gridContainer.setAlignment(Pos.TOP_LEFT);
            gridContainer.setPadding(new Insets(15, 0, 0, 15));
            HBox mainContent = new HBox(10, gridContainer, rightPanel);
            mainContent.setAlignment(Pos.TOP_LEFT);
            BorderPane root = new BorderPane(mainContent);
            root.setStyle("-fx-background-color: white;");
            scene = new Scene(root);
        } else {
            HBox controls = new HBox(15, resetButton, exitButton, showRules);
            controls.setAlignment(Pos.CENTER);
            VBox gameArea = new VBox(10, grid, controls);
            gameArea.setAlignment(Pos.CENTER);
            gameArea.setPadding(new Insets(15, 10, 15, 10));
            BorderPane root = new BorderPane(gameArea);
            root.setRight(rulesPanel);
            BorderPane.setMargin(rulesPanel, new Insets(10, 10, 10, 10));
            scene = new Scene(root);
        }

        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        mainStage.setScene(scene);
        mainStage.show();
        updateBoardUI();
    }

    /**
     * Constructs the right side of the interface if the Hard difficulty is chosen as all buttons should move to the
     * right side of the puzzle in order to make the grid fit.
     * @param rulesPanel
     * @param resetButton
     * @param exitButton
     * @param showRules
     * @return
     */
    private VBox createRightPanel(VBox rulesPanel, Button resetButton, Button exitButton, Button showRules) {
        VBox controlButtons = new VBox(10, resetButton, exitButton, showRules);
        controlButtons.setAlignment(Pos.BOTTOM_LEFT);
        controlButtons.setPadding(new Insets(15, 0, 0, 0));
        VBox rightPanel = new VBox(20, rulesPanel, controlButtons);
        rightPanel.setAlignment(Pos.TOP_LEFT);
        rightPanel.setPadding(new Insets(15, 10, 10, 10));
        rightPanel.setPrefWidth(280);
        VBox.setVgrow(rulesPanel, Priority.ALWAYS);
        return rightPanel;
    }

    /**
     * Creates the main grid of buttons of the puzzle. Each button corresponds to one cell and can be clicked to place
     * a tent (left-click) or flowers (right-click). It also creates labels for the row and column limits.
     * @param size the size of the board depending on the difficulty level
     * @return a fully constructed grid ready for display
     */
    private GridPane createBoardGrid(int size) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(20));
        grid.setHgap(2);
        grid.setVgap(2);
        grid.getStyleClass().add("game-grid");

        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();
        for (int i = 0; i <= size; i++) {
            grid.getColumnConstraints().add(new ColumnConstraints(CELL_SIZE));
            grid.getRowConstraints().add(new RowConstraints(CELL_SIZE));
        }

        rowLabels = new Label[size];
        colLabels = new Label[size];
        gridButtons = new Button[size][size];

        for (int c = 0; c < size; c++) {
            colLabels[c] = countLabel(board.getColLimit(c));
            grid.add(colLabels[c], c + 1, 0);
        }
        for (int r = 0; r < size; r++) {
            rowLabels[r] = countLabel(board.getRowLimit(r));
            grid.add(rowLabels[r], 0, r + 1);
            for (int c = 0; c < size; c++) {
                Button cell = new Button();
                cell.getStyleClass().add("cell-button");
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                cell.setMinSize(CELL_SIZE, CELL_SIZE);
                cell.setMaxSize(CELL_SIZE, CELL_SIZE);
                cell.setPadding(Insets.EMPTY);
                cell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                cell.setOnMouseClicked(new BoardCellHandler(r, c));
                gridButtons[r][c] = cell;
                grid.add(cell, c + 1, r + 1);
            }
        }
        return grid;
    }

    /**
     * Builds the rules panel shown beside the puzzle.
     * @return a VBox representing the rules section
     */
    private VBox createRulesPanel() {
        Label title = new Label("Game Rules");
        title.getStyleClass().add("rules-title");
        Label body = new Label(
                """
                • Each tent must be placed adjacent to a tree (above, below, left, or right).
                • One tent can be adjacent to many trees, but it will be related to just one of them: a one-to-one relation.
                • No two tents can be adjacent to each other, not even diagonally.
                • The number of tents placed in each row and column must match the numbers given at the edges of the grid.
                • Tip: mark cells that cannot contain tents as flowers with right-click. This helps you narrow down possible tent placements and prevents mistakes.
                """);
        body.setWrapText(true);
        body.getStyleClass().add("rules-body");
        VBox box = new VBox(10, title, body);
        box.setPadding(new Insets(15));
        box.setPrefWidth(250);
        box.getStyleClass().add("rules-panel");
        return box;
    }

    /**
     * UI helper method for constructing a button with a specific style.
     * @param text the button label
     * @return a styled button
     */
    private Button createButton(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("button");
        return b;
    }

    /**
     * Creates a button for difficulty selection.
     * @param text name of the difficulty (Easy, Medium, or Hard)
     * @param diff the corresponding difficulty enum value
     * @return a button that sets game difficulty when clicked
     */
    private Button difficultyButton(String text, Difficulty diff) {
        Button b = createButton(text);
        b.getStyleClass().add("button-difficulty");
        b.setOnAction(new DifficultyButton(diff, b));
        return b;
    }

    /**
     * Creates one of the control buttons used during gameplay (Reset Game, Exit Game, Show/Hide rules).
     * @param text the label of the button
     * @param styleClass the specific style of the button
     * @return the specific control button
     */
    private Button controlButton(String text, String styleClass) {
        Button b = createButton(text);
        b.getStyleClass().addAll("button-control", styleClass);
        return b;
    }

    /**
     * Creates a label showing the required number of tents for a row or column.
     * @param val the numeric limit
     * @return a styled label displaying the number
     */
    private Label countLabel(int val) {
        Label l = new Label(String.valueOf(val));
        l.setPrefSize(CELL_SIZE, CELL_SIZE);
        l.setMinSize(CELL_SIZE, CELL_SIZE);
        l.setMaxSize(CELL_SIZE, CELL_SIZE);
        l.setAlignment(Pos.CENTER);
        l.getStyleClass().add("label-count");
        return l;
    }

    /**
     * Refreshes the visual grid of the puzzle. It updates the images of the cells, the hint label colours,
     * and checks for win conditions. Called after every user action that changes the board state.
     */
    private void updateBoardUI() {
        int size = selectedDifficulty.size;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                updateCellVisuals(r, c);
            }
        }
        updateHintLabels();

        if (board.isSolved()) {
            for (Button[] row : gridButtons) {
                for (Button cell : row) {
                    cell.setDisable(true);
                    cell.setOpacity(0.6);
                }
            }
            showWinOverlay();
        }
    }

    /**
     * Updates the visual representation of one cell: the background, image, and highlight state.
     */
    private void updateCellVisuals(int r, int c) {
        Button cell = gridButtons[r][c];
        cell.getStyleClass().removeAll("cell-tree", "cell-tent", "cell-flower", "cell-invalid", "cell-empty");
        cell.setGraphic(null);

        // Set image based on cell state
        CellState s = board.getState(r, c);
        ImageView icon = null;
        if (s == CellState.TREE) {
            icon = new ImageView(treeImage);
            cell.getStyleClass().add("cell-tree");
        } else if (s == CellState.TENT) {
            icon = new ImageView(tentImage);
            if (board.isTentInvalid(r, c)) {
                cell.getStyleClass().add("cell-invalid");
            } else {
                cell.getStyleClass().add("cell-tent");
            }
        } else if (s == CellState.FLOWER) {
            icon = new ImageView(flowerImage);
            cell.getStyleClass().add("cell-flower");
        } else {
            cell.getStyleClass().add("cell-empty");
        }
        if (icon != null) {
            icon.setFitWidth(40);
            icon.setFitHeight(40);
            icon.setPreserveRatio(true);
            icon.setSmooth(true);
            cell.setGraphic(icon);
        }
    }

    /**
     * Updates the colours of the hint labels to show whether row/column tent counts are correct.
     */
    private void updateHintLabels() {
        int size = selectedDifficulty.size;
        for (int r = 0; r < size; r++) {
            int tents = board.countTentsInRow(r);
            int limit = board.getRowLimit(r);
            rowLabels[r].getStyleClass().removeAll("label-hint-red", "label-hint-green", "label-hint-black");
            if (tents > limit) {
                rowLabels[r].getStyleClass().add("label-hint-red");
            } else if (tents == limit) {
                rowLabels[r].getStyleClass().add("label-hint-green");
            } else {
                rowLabels[r].getStyleClass().add("label-hint-black");
            }
        }

        for (int c = 0; c < size; c++) {
            int tents = board.countTentsInCol(c);
            int limit = board.getColLimit(c);
            colLabels[c].getStyleClass().removeAll("label-hint-red", "label-hint-green", "label-hint-black");
            if (tents > limit) {
                colLabels[c].getStyleClass().add("label-hint-red");
            } else if (tents == limit) {
                colLabels[c].getStyleClass().add("label-hint-green");
            } else {
                colLabels[c].getStyleClass().add("label-hint-black");
            }
        }
    }

    /**
     * Displays a translucent congratulatory message on top of the board when the puzzle is solved.
     */
    private void showWinOverlay() {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("win-overlay");

        Label message = new Label("Congratulations! You solved the puzzle!");
        message.getStyleClass().add("win-overlay-message");

        Button playAgain = new Button("Play Again");
        playAgain.getStyleClass().addAll("button", "button-playagain");
        playAgain.setOnAction(new ExitHandler(playAgain));

        VBox box = new VBox(20, message, playAgain);
        box.setAlignment(Pos.CENTER);
        overlay.getChildren().add(box);

        Scene scene = mainStage.getScene();
        StackPane root = new StackPane(scene.getRoot(), overlay);
        root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        mainStage.setScene(new Scene(root));
    }

    /**
     * These inner Listener classes handle all user-initiated events in the game. Each one implements the
     * {@link javafx.event.EventHandler} interface for a specific type of event such as button clicks or mouse actions.
     * They provide a clear separation between the user interface and the underlying game logic by responding to input
     * and then calling the appropriate methods to update the game state or the UI.
     */
    private class StartGameHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent e) {
            setupBoard(selectedDifficulty);
            showGameScreen();
        }
    }

    private class DifficultyButton implements EventHandler<ActionEvent> {
        private final Difficulty diff;
        private final Button btn;

        DifficultyButton(Difficulty d, Button b) {
            diff = d;
            btn = b;
        }

        @Override
        public void handle(ActionEvent e) {
            selectedDifficulty = diff;
            for (Node node : ((HBox) btn.getParent()).getChildren()) {
                if (node instanceof Button) {
                    node.getStyleClass().remove("button-difficulty-selected");
                }
            }
            btn.getStyleClass().add("button-difficulty-selected");
        }
    }

    private class ResetHandler implements EventHandler<ActionEvent> {
        private boolean confirmReset = false;
        private final Button resetButton;

        ResetHandler(Button resetButton) {
            this.resetButton = resetButton;
        }

        @Override
        public void handle(ActionEvent e) {
            if (!confirmReset) {
                confirmReset = true;
                resetButton.setText("Are you sure?");
                PauseTransition delay = new PauseTransition(Duration.seconds(5));
                delay.setOnFinished(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent ev) {
                        confirmReset = false;
                        resetButton.setText("Reset Game");
                    }
                });
                delay.play();
            } else {
                board.clearAllPlacements();
                updateBoardUI();
                confirmReset = false;
                resetButton.setText("Reset Game");
            }
        }
    }

    private class ExitHandler implements EventHandler<ActionEvent> {
        private boolean confirmExit = false;
        private final Button exitButton;

        ExitHandler(Button exitButton) {
            this.exitButton = exitButton;
        }

        @Override
        public void handle(ActionEvent e) {
            if (!confirmExit) {
                confirmExit = true;
                exitButton.setText("Are you sure?");
                PauseTransition delay = new PauseTransition(Duration.seconds(5));
                delay.setOnFinished(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent ev) {
                        confirmExit = false;
                        exitButton.setText("Exit Game");
                    }
                });
                delay.play();
            } else {
                showStartScreen();
                confirmExit = false;
                exitButton.setText("Exit Game");
            }
        }
    }

    private class ShowRulesHandler implements EventHandler<ActionEvent> {
        private final Button button;

        ShowRulesHandler(Button button) {
            this.button = button;
        }

        @Override
        public void handle(ActionEvent e) {
            rulesVisible = !rulesVisible;
            rulesPanel.setVisible(rulesVisible);
            button.setText(rulesVisible ? "Hide Rules" : "Show Rules");
        }
    }

    private class BoardCellHandler implements EventHandler<MouseEvent> {
        private final int row, col;

        BoardCellHandler(int r, int c) {
            this.row = r;
            this.col = c;
        }

        @Override
        public void handle(MouseEvent e) {
            if (e.getButton() == MouseButton.PRIMARY) {
                board.placeTent(row, col);
            } else if (e.getButton() == MouseButton.SECONDARY) {
                board.placeFlowers(row, col);
            }
            updateBoardUI();
            e.consume();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
