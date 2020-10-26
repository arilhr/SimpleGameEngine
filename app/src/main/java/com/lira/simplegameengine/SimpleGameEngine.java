package com.lira.simplegameengine;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SimpleGameEngine extends AppCompatActivity {

    GameView gameView;

    int screenX;
    int screenY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        screenX = size.x;
        screenY = size.y;

        gameView = new GameView(this);
        setContentView(gameView);
    }

    class GameView extends SurfaceView implements Runnable {
        Thread gameThread = null;

        SurfaceHolder ourHolder;

        volatile boolean playing;

        Canvas canvas;
        Paint paint;

        long fps;

        private long timeThisFrame;

        Bitmap bitmapCharacter;

        boolean isMoving = false;
        float walkSpeedPerSeconds = 150;
        float bobXPosition = 10;

        public GameView(Context context) {
            super(context);

            ourHolder = getHolder();
            paint = new Paint();

            bitmapCharacter = BitmapFactory.decodeResource(this.getResources(), R.drawable.raden);
        }

        @Override
        public void run() {
            while(playing) {
                long startFrameTime = System.currentTimeMillis();

                update();

                draw();

                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame > 0) {
                    fps = 1000 / timeThisFrame;
                }
            }
        }

        public void update() {
            if (isMoving) {
                if (bobXPosition > screenX-100 || bobXPosition < 0) {
                    walkSpeedPerSeconds = -walkSpeedPerSeconds;
                }
                bobXPosition = bobXPosition + (walkSpeedPerSeconds / fps);
            }
        }

        public void draw() {
            if (ourHolder.getSurface().isValid()) {
                canvas = ourHolder.lockCanvas();

                canvas.drawColor(Color.argb(255,26,128,182));

                paint.setColor(Color.argb(255,249,129,0));

                paint.setTextSize(45);

                canvas.drawText("FPS: " + fps, 20, 40, paint);
                canvas.drawText("Screen X: " + screenX, 20, 75, paint);
                canvas.drawText("Character Position: " + bobXPosition, 20, 115, paint);

                canvas.drawBitmap(bitmapCharacter, bobXPosition, 200, paint);

                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error: ", "joining thread");
            }
        }

        public void resume() {
            playing= true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    isMoving = true;
                    break;
                case MotionEvent.ACTION_UP:
                    isMoving = false;
                    break;
            }
            return true;
        }
    }

    @Override
    protected  void onResume() {
        super.onResume();
        gameView.resume();
    }

    @Override
    protected  void onPause() {
        super.onPause();
        gameView.pause();
    }
}