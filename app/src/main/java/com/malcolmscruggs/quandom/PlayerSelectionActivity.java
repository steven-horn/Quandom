package com.malcolmscruggs.quandom;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import utils.GameModel;
import utils.Player;

import static com.malcolmscruggs.quandom.McqActivity.MODEL_EXTRA_KEY;

public class PlayerSelectionActivity extends BaseActivity {

    private int numPlayers;
    private int numPoints;
    private ArrayList<Player> players;
    private Button playButton;
    private ArrayList<Integer> colors = new ArrayList<>();

    private final static int TIMEOUT_DURATION = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_selection);

        colors.add(R.color.materialIndigo);
        colors.add(R.color.materialCyan);
        colors.add(R.color.materialGreen);
        colors.add(R.color.materialGreenYellow);
        colors.add(R.color.materialRed);
        colors.add(R.color.materialPink);
        colors.add(R.color.materialPurple);
        colors.add(R.color.materialAmber);
        colors.add(R.color.materialGrey);

        // set default values for # of players and points
        numPlayers = 2;
        numPoints = 5;

        // Set music value
        music = getIntent().getBooleanExtra("Music", false);
        Log.d("MusicSERV", "selection: " + music);

        // Set music switch
        Switch musicSwitch = findViewById(R.id.musicSwitch);
        setupMusicSwitch(musicSwitch);

        //Set default cache values
        useCache = false;
        Switch cacheSwitch = findViewById(R.id.cacheSwitch);
        cacheSwitch.setChecked(useCache);

        // get number pickers and set up values and listeners
        NumberPicker numPickerPlayers = findViewById(R.id.playersPicker);
        NumberPicker numPickerPoints = findViewById(R.id.pointsPicker);

        numPickerPlayers.setMinValue(1);
        numPickerPlayers.setMaxValue(4);
        numPickerPlayers.setValue(2);
        numPickerPlayers.setWrapSelectorWheel(false);

        numPickerPoints.setMinValue(1);
        numPickerPoints.setMaxValue(10);
        numPickerPoints.setValue(5);
        numPickerPoints.setWrapSelectorWheel(false);

        numPickerPlayers.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                numPlayers = numberPicker.getValue();
                switch (numPlayers) {
                    case 1:
                        findViewById(R.id.p2Layout).setVisibility(View.GONE);
                        findViewById(R.id.p3Layout).setVisibility(View.GONE);
                        findViewById(R.id.p4Layout).setVisibility(View.GONE);
                        break;
                    case 2:
                        findViewById(R.id.p2Layout).setVisibility(View.VISIBLE);
                        findViewById(R.id.p3Layout).setVisibility(View.GONE);
                        findViewById(R.id.p4Layout).setVisibility(View.GONE);
                        break;
                    case 3:
                        findViewById(R.id.p2Layout).setVisibility(View.VISIBLE);
                        findViewById(R.id.p3Layout).setVisibility(View.VISIBLE);
                        findViewById(R.id.p4Layout).setVisibility(View.GONE);
                        break;
                    case 4:
                        findViewById(R.id.p2Layout).setVisibility(View.VISIBLE);
                        findViewById(R.id.p3Layout).setVisibility(View.VISIBLE);
                        findViewById(R.id.p4Layout).setVisibility(View.VISIBLE);
                    default:
                        break;
                }
            }
        });

        numPickerPoints.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                numPoints = numberPicker.getValue();
            }
        });

        // create Players
        players = new ArrayList<>(4);
        players.add(new Player(getString(R.string.player1_placeholder), colors.remove(0)));
        players.add(new Player(getString(R.string.player2_placeholder), colors.remove(0)));
        players.add(new Player(getString(R.string.player3_placeholder), colors.remove(0)));
        players.add(new Player(getString(R.string.player4_placeholder), colors.remove(0)));

        // get player name editors and set up listeners
        final EditText playerNameChange1 = findViewById(R.id.p1NameText);
        final EditText playerNameChange2 = findViewById(R.id.p2NameText);
        final EditText playerNameChange3 = findViewById(R.id.p3NameText);
        final EditText playerNameChange4 = findViewById(R.id.p4NameText);

        setupPlayerEditText(0, playerNameChange1);
        setupPlayerEditText(1, playerNameChange2);
        setupPlayerEditText(2, playerNameChange3);
        setupPlayerEditText(3, playerNameChange4);

        // get player color editors and set up listeners
        final Button playerColor1 = findViewById(R.id.p1Color);
        final Button playerColor2 = findViewById(R.id.p2Color);
        final Button playerColor3 = findViewById(R.id.p3Color);
        final Button playerColor4 = findViewById(R.id.p4Color);

        setupPlayerColor(0, playerColor1);
        setupPlayerColor(1, playerColor2);
        setupPlayerColor(2, playerColor3);
        setupPlayerColor(3, playerColor4);

        // get button and set button listener
        playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Player> gamePlayer = new ArrayList<>(players);
                if (numPlayers == 1) {
                    gamePlayer.remove(3);
                    gamePlayer.remove(2);
                    gamePlayer.remove(1);
                } else if (numPlayers == 2) {
                    gamePlayer.remove(3);
                    gamePlayer.remove(2);
                } else if (numPlayers == 3) {
                    gamePlayer.remove(3);
                }

                Bundle extras = getIntent().getExtras();
                String type = extras.getString("Type");
                if (type != null && type.equals("quick")) {
                    populateQuestions(useCache, PlayerSelectionActivity.this, gamePlayer, numPoints,
                            9, "easy", true);
                } else {
                    populateQuestions(useCache, PlayerSelectionActivity.this, gamePlayer, numPoints,
                            extras.getInt("Category"), extras.getString("Difficulty"), true);
                }
            }
        });

        // set on checked for cache
        cacheSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                useCache = b;
                compoundButton.setChecked(useCache);
            }
        });
    }
  
    private void setupPlayerEditText(final int playerIdx, final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                players.get(playerIdx).setPlayerName(editText.getText().toString().trim());
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });
    }

    private void setupPlayerColor(final int playerIdx, final Button button) {
        setPlayerColor(players.get(playerIdx), button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPlayerColor(players.get(playerIdx), button);
            }
        });
    }

    private void setPlayerColor(Player player, Button button) {
        int oldColor = player.getPlayerColor();
        colors.add(oldColor);
        int newColor = colors.remove(0);
        player.setPlayerColor(newColor);
        ViewCompat.setBackgroundTintList(button, ContextCompat.getColorStateList(this, newColor));
    }
}
