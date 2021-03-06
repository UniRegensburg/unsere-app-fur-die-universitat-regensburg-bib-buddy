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

/**
 * The SeekBarListener is responsible for handling the scrolling through a seekBar in voice notes.
 *
 * @author Sabrina Freisleben
 */
public class SeekBarListener implements SeekBar.OnSeekBarChangeListener {

  private final MediaPlayer mediaPlayer;
  private final SeekBarCompat progressBar;
  private final TextView playedTime;

  private boolean checkProgress = true;

  /**
   * Constructor to create a custom SeekBarListener to handle voice note playing progress.
   *
   * @param context     activity as base context
   * @param mediaPlayer of the target noteItem
   * @param seekBar     of the target noteItem
   * @param playedTime  textView of the target noteItem to display currently played audio time
   */
  public SeekBarListener(Context context, MediaPlayer mediaPlayer, SeekBarCompat seekBar,
                         TextView playedTime) {
    this.mediaPlayer = mediaPlayer;
    this.progressBar = seekBar;
    this.playedTime = playedTime;
    this.progressBar.setThumbColor(context.getColor(R.color.tiffany));
    this.progressBar.setProgressColor(context.getColor(R.color.design_default_color_secondary));
    this.progressBar.setProgressBackgroundColor(context.getColor(R.color.black));
    this.progressBar.setThumbAlpha(128);
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress,
                                boolean fromUser) {
    if (fromUser) {
      mediaPlayer.seekTo(progress);
    }
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
    // Auto-generated method stub
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
    // Auto-generated method stub
  }

  /**
   * Converts a given value in milliseconds to a common time displaying format.
   *
   * @param millis time value given in milliseconds
   * @return the string value representing the given millis in a "00:00" format
   */
  public String showTime(int millis) {
    return (new SimpleDateFormat("mm:ss", Locale.getDefault()).format(new Date(millis)));
  }

  /**
   * Starts a runnable to permanently update the seekBar to match the position of the played audio.
   */
  @SuppressWarnings("deprecation")
  public void updateProgress() {
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
   * Resets the seekBar and playedTime views.
   */
  public void reset() {
    playedTime.setText(R.string.default_played_timer);
    progressBar.setProgress(0, true);
    checkProgress = false;
  }

}

