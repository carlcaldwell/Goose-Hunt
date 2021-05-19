package com.example.goosehunt;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Hunt extends AppCompatActivity {

    ImageView gooseImg;
    final int maxX=20, minX=5, maxY=10, minY=20; // the min/max angles the goose can move
    int hits=0, misses=0; // the scores on the top of the play area
    final int allowedMissed=5; // how many misses until game over
    int width, height; // height/width of the players screen in pixels
    final int[] currentX = {0}; // current x value of the goose
    final int[] currentY = {0}; // current y value of the goose
    // how far to move each tick
    int xIncrement, yIncrement; // angles of the movement
    final boolean[] goingUp = {true};  // defines if the goose is going up or down
    final boolean[] goingRight = {true}; // defines if the goose is going left or right
    final int speed = 15; // speed the bird is redrawn. lower number means faster
    TextView hitsScore, missesScore, hitOrMiss;
    float[] lastTouchDownXY = new float[2]; // the location of the last touch. This is used to display the hit/miss text when the player clicks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunt);

        // hook widgets
        gooseImg = findViewById(R.id.gooseImg);
        hitsScore = findViewById(R.id.hits);
        missesScore = findViewById(R.id.misses);
        hitOrMiss = findViewById(R.id.missOutput);
        View bg = findViewById(R.id.background);

        // get the height and width
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width=dm.widthPixels - 150;
        height= dm.heightPixels - 100;

        // set an initial position for the goose.
        currentX[0] = (int)(Math.random()*(width+1)); // set the x so it's at a random allowable position
        currentY[0] = height; // set the y value to the max, so it's at the bottom
        // set the goose to the coordinates
        gooseImg.setX(currentX[0]);
        gooseImg.setY(currentY[0]);

        // get new movement incrementer
        // this sets the angle of the birds movement
        xIncrement = (int)(Math.random()*(maxX-minX+1)+minX);
        yIncrement = (int)(Math.random()*(maxY-minY+1)+minY);

        // update the score to be 0 hits and 0 misses
        updateScore();
        // start the goose thread
        startGoose();

        // create a on touch listener for the background. pass it to the onTouch method
        bg.setOnTouchListener(this::onTouch);
    }

    // this method moves the goose.
    public void startGoose(){
        // create a new handler/runner to handle moving the goose for performance reasons
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                // if the goose is going right, we need to add the incrementer.
                if(goingRight[0]){
                    // if the goose will go past the edge of the screen, stop it and reverse it's direction
                    if(currentX[0] + xIncrement > width){
                        currentX[0] = width;
                        goingRight[0] = false;
                        gooseImg.setScaleX(1); // set the goose image to face left
                    }else{
                        // the goose has room to move. move it
                        currentX[0] = currentX[0] + xIncrement;
                    }
                }else{
                    // the goose is going left. we need to subtract the incrementer
                    if(currentX[0] - xIncrement < 0){
                        // the goose will go past the left side of the device screen, stop and reverse it's direction.
                        currentX[0] = 0;
                        goingRight[0] = true;
                        gooseImg.setScaleX(-1); // flip the goose to face right
                    }else{
                        // the goose has room to move. move it
                        currentX[0] = currentX[0] - xIncrement;
                    }
                }

                if(goingUp[0]){
                    // going up
                    if(currentY[0] + yIncrement > height){
                        currentY[0] = height;
                        goingUp[0] = false;
                    }else{
                        currentY[0] = currentY[0] + yIncrement;
                    }
                }else{
                    // going down
                    if(currentY[0] - yIncrement < 0){
                        currentY[0] = 0;
                        goingUp[0] = true;
                    }else{
                        currentY[0] = currentY[0] - yIncrement;
                    }
                }

                // now that the location is defined, we need to apply it to the imageview
                gooseImg.setX(currentX[0]);
                gooseImg.setY(currentY[0]);

                // run the loop again on a delay. the speed is set above
                handler.postDelayed(this, speed);
            }
        };
        handler.post(runnable);

    }


    // this method is called when the player touches a goose.
    // we need to update the score, reset the goose to the bottom of the screen, and display the hit text
    public void hit() {

        // reset the location of the goose
        currentX[0] = (int)(Math.random()*(width+1)); // random x location
        currentY[0] = height; // at the bottom
        // set the goose to its new home
        gooseImg.setX(currentX[0]);
        gooseImg.setY(currentY[0]);

        // get new angle incrementer
        xIncrement = (int)(Math.random()*(maxX-minX+1)+minX);
        yIncrement = (int)(Math.random()*(maxY-minY+1)+minY);

        // display the "hit" text where the player touched the goose
        hitOrMiss.setX(lastTouchDownXY[0]-80); // x value - 1/2 the width of the goose
        hitOrMiss.setY(lastTouchDownXY[1]-50); // y value - the height of the finger tip
        hitOrMiss.setText("Hit!"); // change the text from "miss" or null to "Hit!"

        // create the animations. Fade in super quick and out slower
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(hitOrMiss, "alpha", 0f, 1f);
        fadeIn.setDuration(10);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(hitOrMiss, "alpha",  1f, 0f);
        fadeOut.setDuration(500);
        final AnimatorSet mAnimationSet = new AnimatorSet();
        mAnimationSet.play(fadeOut).after(fadeIn);
        mAnimationSet.start();

        hits++; // increase the score
        updateScore(); // update the scores on the screen

    }

    // this method is called with the player misses the goose.
    // when the player misses, we need to update the score, display the miss text, and check if the game is over
    public void miss(){
        misses++; // increment the hit score

        // the player is only allowed so many hits. Check if the game is over
        if(misses > allowedMissed-1){
            // the game is over. We need to change the activity to the game over activity and pass it the score
            Intent i = new Intent(this, GameOver.class);
            i.putExtra("Score", hits); // put extra the hits
            startActivity(i);
            finish(); // close this activity so the phone can rest
            return;
        }

        // the game is not over.
        updateScore(); // update the scores on the screen

        // need to display the miss text where the player touched last
        hitOrMiss.setX(lastTouchDownXY[0]-80);
        hitOrMiss.setY(lastTouchDownXY[1]-50);
        hitOrMiss.setText("Miss!");

        // create the animations. Fade in super quick and out slower
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(hitOrMiss, "alpha", 0f, 1f);
        fadeIn.setDuration(10);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(hitOrMiss, "alpha",  1f, 0f);
        fadeOut.setDuration(500);
        final AnimatorSet mAnimationSet = new AnimatorSet();
        mAnimationSet.play(fadeOut).after(fadeIn);
        mAnimationSet.start();

    }

    // this method updates that hits and misses text at the top of the screen
    private void updateScore(){
        missesScore.setText("Miss:"+misses);
        hitsScore.setText("Hits:"+hits);
    }

    // this method is called every time the player touches above the background.
    // this method stores the last touched location. the hit / miss methods will display hit or miss text in this location
    private boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // a touch down action was received. ignore action_up
            lastTouchDownXY[0] = event.getX();
            lastTouchDownXY[1] = event.getY();

            // check if the player hit the goose or missed
            if ((lastTouchDownXY[0] >= gooseImg.getX() && lastTouchDownXY[0] <= gooseImg.getWidth() + gooseImg.getX()) && (lastTouchDownXY[1] >= gooseImg.getY() && lastTouchDownXY[1] <= gooseImg.getHeight() + gooseImg.getY())) {
                hit();
            } else {
                miss();
            }
        }
        return false;
    }
}