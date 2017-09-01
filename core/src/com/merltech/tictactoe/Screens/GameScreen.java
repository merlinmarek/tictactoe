package com.merltech.tictactoe.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.merltech.tictactoe.TicTacToe;
import com.merltech.tictactoe.network.BluetoothService;
import com.merltech.tictactoe.network.Message;

public class GameScreen implements Screen {
    private final TicTacToe game;
    private final BluetoothService bluetoothService;
    private Skin skin;
    private Stage stage;

    private int playerValue;
    private final int[][] field;
    private final TextButton[][] buttons;
    private Dialog endDialog;
    private Dialog errorDialog;
    private Label errorDialogLabel;

    private final String Tag = "Game";

    public GameScreen(TicTacToe game) {
        this.game = game;
        this.skin = game.skin;
        this.bluetoothService = game.bluetoothService;
        playerValue = 0;
        field = new int[3][3];
        buttons = new TextButton[3][3];

        setupUi();
    }

    private int getWinner() {
        int row_sum = 0;
        int column_sum = 0;
        int diag_sum_1 = 0;
        int diag_sum_2 = 0;
        for(int a = 0; a < 3; ++a) {
            diag_sum_1 += field[a][a];
            diag_sum_2 += field[2-a][a];
        }

        for(int a = 0; a < 3; ++a) {
            row_sum = 0;
            column_sum = 0;
            for(int b = 0; b < 3; ++b) {
                row_sum += field[b][a];
                column_sum += field[a][b];
            }
            if(Math.min(row_sum, Math.min(column_sum, Math.min(diag_sum_1, diag_sum_2))) == -3) {
                return -1;
            }
            if(Math.max(row_sum, Math.max(column_sum, Math.max(diag_sum_1, diag_sum_2))) == 3) {
                return 1;
            }
        }
        return 0;
    }

    private void setupUi() {
        stage = new Stage(new ScreenViewport());

        Table rootTable = new Table(skin);
        rootTable.setFillParent(true);

        Label titleLabel = new Label("Tic Tac Toe", skin, "big-font", Color.BLACK);
        rootTable.add(titleLabel).center().expandX();

        rootTable.row().expandY().top();
        Table gameTable = new Table(skin);
        for(int column = 0; column < 3; ++column) {
            gameTable.row();
            for(int row = 0; row < 3; ++row) {
                final TextButton button = new TextButton("", skin);
                button.getLabel().setFontScale(2.0f);
                final int buttonColumn = column;
                final int buttonRow = row;
                button.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if(field[buttonColumn][buttonRow] != 0) {
                            // already clicked
                            return;
                        }
                        field[buttonColumn][buttonRow] = playerValue;
                        buttons[buttonColumn][buttonRow].setText(playerValue == 1 ? "X" : "O");
                        disableButtons();
                        String position = String.valueOf(buttonColumn) + String.valueOf(buttonRow);
                        String data = playerValue == 1 ? position + "X" : position + "O";
                        bluetoothService.write(data.getBytes());
                        checkWinner();
                    }
                });
                gameTable.add(button).width(Value.percentWidth(0.333333f, gameTable));
                buttons[column][row] = button;
            }
        }

        endDialog = new Dialog("End Game", skin) {
            protected void result(Object object) {
                bluetoothService.disconnect();
                game.setScreen(game.lobbyScreen);
            };
        };
        endDialog.button("Okay", 1);

        errorDialog = new Dialog("Error", skin) {
            protected void result(Object object) {
                bluetoothService.disconnect();
                game.setScreen(game.lobbyScreen);
            }
        };
        errorDialogLabel = new Label("error", skin);
        errorDialog.text(errorDialogLabel);
        errorDialog.button("Okay");

        rootTable.add(gameTable).grow();

        rootTable.row().bottom();
        Table buttonTable = new Table(skin);
        rootTable.add(buttonTable).fillX();

        stage.addActor(rootTable);
    }

    private void processMessages() {
        Message message = null;
        while((message = bluetoothService.getMessage()) != null) {
            Gdx.app.log(Tag, "received message: " + message.code.name());
            switch(message.code) {
                case BLUETOOTH_CONNECTED:
                    Gdx.app.log(Tag, "we have a connection");
                    break;
                case BLUETOOTH_ADAPTER_DISABLED:
                    // this is bad, we have to stop the game
                    showError("bluetooth was disabled");
                    break;
                case BLUETOOTH_DISCONNECTED:
                    showError("bluetooth was disconnected");
                    break;
                case BLUETOOTH_ERROR:
                    showError("bluetooth error");
                    break;
                case BLUETOOTH_SENT:
                    Gdx.app.log(Tag, "bluetooth was sent");
                    break;
                case BLUETOOTH_RECEIVED:
                    String received = new String((byte[])message.data);
                    int column = Integer.parseInt(received.substring(0, 1));
                    int row = Integer.parseInt(received.substring(1, 2));
                    String opponentString = received.substring(2,3);
                    int opponentValue = opponentString.equals("X") ? 1 : -1;
                    field[column][row] = opponentValue;
                    buttons[column][row].setText(opponentString);
                    checkWinner();
                    enableButtons();
                    break;
                default:
                    Gdx.app.log(Tag, "message not handled, discarded");
            }
        }
    }

    private void checkWinner() {
        int winner = getWinner();
        if(winner == 0) {
            boolean draw = true;
            for(int column = 0; column < 3; ++column) {
                for(int row = 0; row < 3; ++row) {
                    if(field[column][row] != 0)
                        draw = false;
                }
            }
            if(draw) {
                endDialog.getTitleLabel().setText("It is a draw");
                endDialog.show(stage);
            }
            return;
        }
        if(winner == playerValue) {
            endDialog.getTitleLabel().setText("You won :)");
            endDialog.show(stage);
            return;
        }
        endDialog.getTitleLabel().setText("You lost like a noob :(");
        endDialog.show(stage);
    }

    private void showError(String s) {
        errorDialogLabel.setText(s);
        errorDialog.show(stage);
    }

    @Override
    public void show() {
        Gdx.app.log(Tag, "showing");
        game.inputMultiplexer.addProcessor(stage);
        // reset field and buttons
        for(int column = 0; column < 3; ++column) {
            for(int row = 0; row < 3; ++row) {
                field[column][row] = 0;
                buttons[column][row].setText("");
            }
        }

        // if we are not already connected we came here to host a game
        if(!bluetoothService.isConnected()) {
            bluetoothService.listen();
            // we are the host, so our id is -1
            playerValue = -1;
            // the client always begins
            disableButtons();

            // schedule a timeout because we are only visible for 30 seconds
            Timer.schedule(new Timer.Task() {
                int n = 0;
                @Override
                public void run() {
                    ++n;
                    if(n > 30) {
                        showError("no one connected");
                    }
                }
            }, 30);
        } else {
            // we are just a client so our id is 1
            playerValue = 1;
            enableButtons();
        }
    }

    private void enableButtons() {
        for(int column = 0; column < 3; ++column) {
            for(int row = 0; row < 3; ++row) {
                buttons[column][row].setTouchable(Touchable.enabled);
                buttons[column][row].setColor(1f, 1f, 1f, 1.0f);
            }
        }
    }

    private void disableButtons() {
        for(int column = 0; column < 3; ++column) {
            for(int row = 0; row < 3; ++row) {
                buttons[column][row].setTouchable(Touchable.disabled);
                buttons[column][row].setColor(0.5f, 0.5f, 0.5f, 1.0f);
            }
        }
    }

    @Override
    public void render(float delta) {
        if(Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            Gdx.app.log(Tag, "back key was pressed, returning to lobby");
            bluetoothService.disconnect();
            game.setScreen(game.lobbyScreen);
        }
        processMessages();
        Gdx.gl.glClearColor(0.8f, 0.8f, 0.8f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        Gdx.app.log(Tag, "hiding");
        game.inputMultiplexer.clear();
    }

    @Override
    public void dispose() {

    }
}
