package com.merltech.tictactoe.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.merltech.tictactoe.TicTacToe;
import com.merltech.tictactoe.network.BluetoothService;
import com.merltech.tictactoe.network.Message;

public class LobbyScreen implements Screen {
    private final TicTacToe game;
    private final BluetoothService bluetoothService;
    private Skin skin;
    private Stage stage;

    // UI
    private Table peerTable;

    private final String Tag = "Lobby";

    public LobbyScreen(TicTacToe game) {
        this.game = game;
        this.skin = game.skin;
        this.bluetoothService = game.bluetoothService;
        setupUi();
    }

    private void setupUi() {
        stage = new Stage(new ScreenViewport());

        final TextButton bluetoothButton = new TextButton("Bluetooth", skin);
        final TextButton scanButton = new TextButton("Scan", skin);
        final TextButton hostButton = new TextButton("Host", skin);

        hostButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                bluetoothService.makeDiscoverable(60);
                game.setScreen(game.gameScreen);
            }
        });

        scanButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                bluetoothService.scan();
            }
        });

        bluetoothButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                bluetoothService.enable();
            }
        });

        Table rootTable = new Table(skin);
        rootTable.setDebug(true);
        rootTable.setFillParent(true);

        rootTable.add(new Label("Tic Tac Toe", skin)).center().expandX();

        rootTable.row().expandY().top();
        peerTable = new Table(skin);
        peerTable.add(new Label("Name", skin)).left().expandX();
        peerTable.add(new Label("Address", skin)).right();
        peerTable.setDebug(true);
        rootTable.add(peerTable).fillX();

        rootTable.row().bottom();
        Table buttonTable = new Table(skin);
        buttonTable.add(scanButton).center().expandX();
        buttonTable.add(hostButton).center().expandX();
        buttonTable.add(bluetoothButton).center().expandX();
        rootTable.add(buttonTable).fillX();

        stage.addActor(rootTable);
    }

    private void processMessages() {
        Message message = null;
        while((message = bluetoothService.getMessage()) != null) {
            Gdx.app.log(Tag, "received message: " + message.code.name());
            switch(message.code) {
                case BLUETOOTH_CONNECTED:
                    Gdx.app.log(Tag, "bluetooth connected");
                    game.setScreen(game.gameScreen);
                    break;
                case BLUETOOTH_DEVICE_FOUND:
                    Gdx.app.log(Tag, "bluetooth device found");
                    addPeer((BluetoothService.BluetoothPeer)message.data);
                    break;
                case BLUETOOTH_ERROR:
                    Gdx.app.log(Tag, "ERROR: " + message.data);
                default:
                    Gdx.app.log(Tag, "message not handled, discarded");
            }
        }
    }

    private void addPeer(BluetoothService.BluetoothPeer peer) {
        peerTable.row();
        peerTable.add(peer.Name);
        peerTable.add(peer.Address);
        TextButton connectButton = new TextButton("connect", skin);
        final String buttonAddress = peer.Address;
        connectButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log(Tag, "connecting to: " + buttonAddress);
                bluetoothService.connect(buttonAddress);
            }
        });
        peerTable.add(connectButton);
    }

    @Override
    public void show() {
        Gdx.app.log(Tag, "showing");
        game.inputMultiplexer.addProcessor(stage);
    }

    @Override
    public void render(float delta) {
        processMessages();
        Gdx.gl.glClearColor(0.8f, 0.8f, 0.8f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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

    }

    @Override
    public void dispose() {

    }
}
