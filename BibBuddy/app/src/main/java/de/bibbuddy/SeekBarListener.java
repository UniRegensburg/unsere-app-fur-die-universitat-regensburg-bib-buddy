package de.bibbuddy;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.SeekBar;
import android.widget.TextView;
import app.minimize.com.seek_bar_compat.SeekBarCompat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SeekBarListener implements SeekBar.OnSeekBarChangeListener {

  private final MediaPlayer mediaPlayer;
  private final SeekBarCompat progressBar;
  private final TextView playedTime;
  private boolean checkProgress = true;

  /**
   * Constructor to create a custom SeekBarListener for voice note progress displaying.
   *
   * @param context     activity as base context
   * @param mediaPlayer mediaPlayer of the target noteItem
   * @param progressBar seekBar of the target noteItem
   * @param playedTime  textView of the target noteItem to display currently played audio time
   */
  public SeekBarListener(Context context, MediaPlayer mediaPlayer, SeekBarCompat progressBar,
                         TextView playedTime) {
    this.mediaPlayer = mediaPlayer;
    this.progressBar = progressBar;
    this.playedTime = playedTime;
    this.progressBar.setThumbColor(context.getColor(R.color.tiffany));
    this.progressBar.setProgressColor(context.getColor(R.color.teal_700));
    this.progressBar.setProgressBackgroundColor(context.getColor(R.color.gray_background));
    this.progressBar.setThumbAlpha(128);
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress,
                                boolean fromUser) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
    // TODO Auto-generated method stub
  }

  /**
   * This method converts the given milliseconds value to a common time displaying format.
   *
   * @param millis time value given in milliseconds
   * @return returns a string value representing given millis in a "00:00" format
   */
  public String showTime(int millis) {
    return (new SimpleDateFormat("mm:ss", Locale.getDefault()).format(new Date(millis))
        .substring(0, 5));
  }

  /**
   * This method starts a runnable to permanently update the seekBar to match the position of the
   * media played.
   */
  public void barUpdate() {
    Handler handler = new Handler();
    checkProgress = true;
    Runnable r = new Runnable() {
      @Override
      public void run() {
        if (checkProgress) {
          progressBar.setMax(mediaPlayer.getDuration());
          int currentPosition = mediaPlayer.getCurrentPosition();
          playedTime.setText(showTime(currentPosition));
          progressBar.setProgress(currentPosition, true);
          handler.postDelayed(this, 100);
        } else {
          handler.removeCallbacks(this);
        }
      }
    };
    r.run();
  }

  /**
   * This method resets seekBar and playedTime view.
   */
  public void reset() {
    playedTime.setText(R.string.default_played_time);
    progressBar.setProgress(0, true);
    checkProgress = false;
  }

}

