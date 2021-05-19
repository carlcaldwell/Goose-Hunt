package com.example.goosehunt;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class GameOver extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        // hook the button and setup a listener
        Button b = findViewById(R.id.mainMenuBtn);
        b.setOnClickListener(e->{
            startActivity(new Intent(this, MainMenu.class));
            finish();
        });

        // get/display the players score
        TextView scoreTv = findViewById(R.id.finalScoreTv);
        try {
            int score = getIntent().getExtras().getInt("Score");
            scoreTv.setText("Score: " + score);
        }catch(Exception e){
            scoreTv.setText("Error loading score.");
        }

    }
}