package com.merltech.tictactoe;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.merltech.tictactoe.Screens.GameScreen;
import com.merltech.tictactoe.Screens.LobbyScreen;
import com.merltech.tictactoe.network.BluetoothService;

public class TicTacToe extends Game {
	public Skin skin;
    public InputMultiplexer inputMultiplexer;
    public BluetoothService bluetoothService;

    public LobbyScreen lobbyScreen;
    public GameScreen gameScreen;

    private final String Tag = "TicTacToe";

    public TicTacToe(BluetoothService bluetoothService) {
        this.bluetoothService = bluetoothService;
    }

	@Override
	public void create () {
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        inputMultiplexer = new InputMultiplexer();
		Gdx.input.setInputProcessor(inputMultiplexer);

        // create screens
        lobbyScreen = new LobbyScreen(this);
        gameScreen = new GameScreen(this);

        this.setScreen(lobbyScreen);
	}


	@Override
	public void render () {
        super.render();
	}


    @Override
	public void dispose () {
        bluetoothService.dispose();
	}
}
