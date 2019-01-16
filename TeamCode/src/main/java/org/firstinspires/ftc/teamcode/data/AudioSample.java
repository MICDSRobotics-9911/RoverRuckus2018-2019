package org.firstinspires.ftc.teamcode.data;


import android.app.Activity;
import android.media.MediaPlayer;

import org.firstinspires.ftc.teamcode.R;

import java.io.IOException;

/*
CODE WAS TAKEN FROM ethan-schaffer with some modifications
This code is written as an example only.
Obviously, it was not tested on your team's robot.
Teams who use and reference this code are expected to understand code they use.

If you use our code and see us at competition, come say hello!
*/

public class AudioSample extends Activity {
    MediaPlayer mediaPlayer;
    // String PATH_TO_FILE; // relative path

    public AudioSample() throws IOException {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sickomode);
        //this.PATH_TO_FILE = path;
        /*mediaPlayer.setDataSource();
        mediaPlayer.prepare();*/
        mediaPlayer.prepare();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void playSound() {
        mediaPlayer.start();
    }

    public void setLooping(boolean b) {
        mediaPlayer.setLooping(b);
    }

    public void playSoundAsLoop() {
        setLooping(true);
        mediaPlayer.start();
    }

    public void pauseSound() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void stopSound() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }
}
