package edu.ucsd.cse.eulexia.slider;

/**
 * Created by michelleawu on 11/12/15.
 */
import edu.ucsd.cse.eulexia.card.CardAdapter;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollView;
import com.google.android.glass.widget.Slider;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity that demonstrates the slider API.
 */
public final class SliderActivity extends Activity {

    // Determinate slider
    private static final int DETERMINATE = 1;
    private static final int MAX_SLIDER_VALUE = 5;

    private static final long ANIMATION_DURATION_MILLIS = 5000;

    private CardScrollView mCardScroller;
    private Slider mSlider;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // Ensure screen stays on during demo.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(new CardAdapter(createCards(this)));
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Plays sound.
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(Sounds.TAP);
                processSliderRequest(position);
            }
        });
        setContentView(mCardScroller);
        mSlider = Slider.from(mCardScroller);
    }

    @Override
    public void onBackPressed() {
        // If the Grace Period is running, cancel it instead of finishing the Activity.
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
    }

    @Override
    protected void onPause() {
        mCardScroller.deactivate();
        super.onPause();
    }

    /**
     * Processes a request to show a slider.
     *
     * Starting a new Slider, regardless of its type, automatically hides any shown Slider.
     */
    private void processSliderRequest(int position) {
        final Slider.Determinate determinate =
                mSlider.startDeterminate(MAX_SLIDER_VALUE, 0);
        ObjectAnimator animator = ObjectAnimator.ofFloat(determinate, "position", 0,
                MAX_SLIDER_VALUE);

        // Hide the slider when the animation stops.
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                determinate.hide();
            }
        });
        // Start an animation showing the different positions of the slider.
        animator.setDuration(ANIMATION_DURATION_MILLIS)
                .start();
    }

    /**
     * Create a list of cards to display as activity content.
     */
    private List<CardBuilder> createCards(Context context) {
        ArrayList<CardBuilder> cards = new ArrayList<CardBuilder>();
        cards.add(DETERMINATE, new CardBuilder(context, CardBuilder.Layout.TEXT)
                .setText("Start a determinate slider1"));
        cards.add(DETERMINATE, new CardBuilder(context, CardBuilder.Layout.TEXT)
                .setText("Start a determinate slider2"));
        cards.add(DETERMINATE, new CardBuilder(context, CardBuilder.Layout.TEXT)
                .setText("Start a determinate slider3"));
        cards.add(DETERMINATE, new CardBuilder(context, CardBuilder.Layout.TEXT)
                .setText("Start a determinate slider4"));
        return cards;
    }
}
