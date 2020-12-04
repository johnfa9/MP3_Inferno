package com.ga.mp3inferno;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.HashMap;


public class PlayerActivity extends AppCompatActivity {
    //set up view references
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private TextView duration;
    private TextView time;
    private TextView title;
    private SeekBar seekBar;

    private ImageView imagePlayer;
    private ImageView imageRepeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        //set view references
        time = findViewById(R.id.StartTime);
        imagePlayer = findViewById(R.id.Play);
        imageRepeat = findViewById((R.id.Repeat));
        duration = findViewById((R.id.Duration));
        title = findViewById((R.id.SongTitle));
        seekBar = findViewById(R.id.Seek);
        seekBar.setMax(100);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void play(View v) {
        if (mediaPlayer == null) {  //check if media player exists
            mediaPlayer = new MediaPlayer();
            setMediaPlayer();  //create media player
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //check if song is finished playing
                    handler.removeCallbacks(seekUpdater);
                    title.setText("");
                    stopPlayer();
                    imagePlayer.setImageResource(R.drawable.ic_play);
                }
            });
        }

        if (mediaPlayer.isPlaying()) {
            //pause the media player
            handler.removeCallbacks(seekUpdater);
            mediaPlayer.pause();
            imagePlayer.setImageResource(R.drawable.ic_play);

        } else {
            mediaPlayer.start();  //start player
            imagePlayer.setImageResource((R.drawable.ic_pause));

            changeSeekBar();
            seekBar.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    SeekBar seekBar = (SeekBar) view;
                    int position = (mediaPlayer.getDuration() / 100) * seekBar.getProgress();
                    mediaPlayer.seekTo((position));
                    time.setText((convertTime(mediaPlayer.getCurrentPosition())));
                    return false;
                }
            });
            //show seek bar buffering on media player
            mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                //Show buffering progress on seekbar
                @Override
                public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                    seekBar.setSecondaryProgress(i);
                }
            });
        }
    }

    @Override
    protected void onStop() {
        //player stops
        super.onStop();
        stopPlayer();
    }

    public void stop(View v) {
        //stop player
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            handler.removeCallbacks(seekUpdater);
            imagePlayer.setImageResource(R.drawable.ic_play);
            title.setText("");
            stopPlayer();
        }
    }

    private void stopPlayer() {
        //remove media player
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void setMediaPlayer() {
        //set up the stream for the media player
        String songTitle = "";
        String Url = "http://www.ashaquinn.com/samples/song15-1.mp3";

        try {
            mediaPlayer.setDataSource(Url);
            mediaPlayer.prepare();
            duration.setText(convertTime(mediaPlayer.getDuration()));
            songTitle = retrieveTitle(Url);
            title.setText(("Now Playing..." + songTitle));  //show the song's title
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String convertTime(long ms) {
        //set up time for seekbar
        String secondsValue = "";
        String timer = "";
        int hours = (int) (ms / (1000 * 60 * 60));
        int minutes = (int) (ms % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((ms % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (hours > 0) {
            timer = hours + ":";
        }
        if (seconds < 10) {
            secondsValue = "0" + seconds;
        } else {
            secondsValue = "" + seconds;
        }
        timer = timer + minutes + ":" + secondsValue;
        return timer;
    }

    private void changeSeekBar() {
        //update the seek bar position
        if (mediaPlayer.isPlaying()) {
            seekBar.setProgress((int) (((float) mediaPlayer.getCurrentPosition() /
                    mediaPlayer.getDuration()) * 100));
            handler.postDelayed(seekUpdater, 1000);
        }
    }

    private Runnable seekUpdater = new Runnable() {
        @Override
        public void run() {
            //runnable seekbar
            changeSeekBar();
            long current = mediaPlayer.getCurrentPosition();
            time.setText(convertTime(current));
        }
    };

    private String retrieveTitle(String getTitle) {
        //Retrieve the title of the song
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();

        if (metaRetriever != null) {
            try {
                if (Build.VERSION.SDK_INT >= 14) //new version android
                    metaRetriever.setDataSource(getTitle, new HashMap<String, String>());
                else
                    metaRetriever.setDataSource(getTitle); //old version of android
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }
        //Retrieve the title of the song
        String title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        return title;
    }

    public void repeat(View view) {
        //set up option to repeat the song
        if (mediaPlayer != null && mediaPlayer.isPlaying() && mediaPlayer.isLooping()) {
            mediaPlayer.setLooping(false);
            imageRepeat.setColorFilter(Color.RED);
            handler.removeCallbacks(seekUpdater);
        } else if (mediaPlayer != null && mediaPlayer.isPlaying() &&
                mediaPlayer.isLooping() == false) {
            imageRepeat.setColorFilter(Color.GREEN);
            mediaPlayer.setLooping(true);
        }
    }
}