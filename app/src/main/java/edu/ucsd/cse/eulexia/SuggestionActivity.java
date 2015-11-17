package edu.ucsd.cse.eulexia;

import com.google.android.glass.app.Card;
import com.google.android.glass.media.Sounds;
import edu.ucsd.cse.eulexia.card.CardAdapter;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by michelleawu on 11/12/15.
 * Creates a card scroll view containing all spelling suggestions.
 */
public class SuggestionActivity extends Activity implements TextToSpeech.OnInitListener, GestureDetector.BaseListener {

    private static final String TAG = SpellcheckActivity.class.getSimpleName();
    private static final String TAG2 = "Spellz";

    // Gesture detection
    private GestureDetector mGestureDetector;

    // Text to speech
    private TextToSpeech tts;
    private boolean initialized = false;
    private String queuedText;

    // ArrayList of suggested spellings
    ArrayList<String> suggList = new ArrayList<String>();

    private CardScrollAdapter mAdapter;
    private CardScrollView mCardScroller;

    // Visible for testing.
    CardScrollView getScroller() {
        return mCardScroller;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // Retrieve saved suggestions from SpellCheckActivity
        Bundle b = getIntent().getExtras();
        suggList = b.getStringArrayList("suggestions");

        // Create cards
        mAdapter = new CardAdapter(createCards(this), getBaseContext());
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(mAdapter);
        setContentView(mCardScroller);
        setCardScrollerListener();

        tts = new TextToSpeech(this /* context */, this /* listener */);

        // Initialize the gesture detector and set the activity to listen to discrete gestures.
        mGestureDetector = new GestureDetector(this).setBaseListener(this);
    }

    /**
     * Create list of spelling suggestions.
     */
    private List<CardBuilder> createCards(Context context) {
        ArrayList<CardBuilder> cards = new ArrayList<CardBuilder>();
        int i = 0;
        for(String word : suggList){
            cards.add(i, new CardBuilder(context, CardBuilder.Layout.MENU)
            .setText(word)
            .setFootnote(R.string.suggestion_card_menu_description));
            i++;
        }
        return cards;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            initialized = true;
            tts.setLanguage(Locale.ENGLISH);

            if (queuedText != null) {
                speak(queuedText);
            }
        }
    }

    public void speak(String text) {
        // If not yet initialized, queue up the text.
        if (!initialized) {
            queuedText = text;
            return;
        }
        queuedText = null;
        // Before speaking the current text, stop any ongoing speech.
        tts.stop();

        // Set speech rate to a slower speed
        tts.setSpeechRate(0.1f);

        // Speak the text.
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
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

    // Insert spaces after every letter in the string
    private void spellWordAt(int index) {
        speak(suggList.get(index).replace("", " ").trim());
    }

    /**
     * Different type of activities can be shown, when tapped on a card.
     */
    private void setCardScrollerListener() {
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG2, "Clicked view at position " + position + ", row-id " + id);
                int soundEffect = Sounds.TAP;

                // On tap, play audio spelling
                spellWordAt(position);

                /*soundEffect = Sounds.ERROR;
                Log.d(TAG, "Don't show anything");*/

                // Play sound.
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(soundEffect);
            }
        });
    }

    @Override
    public boolean onGesture(Gesture gesture) {
        Log.e("tag", gesture.name());
        if (gesture == Gesture.TAP) {
            return true;
        } else if (gesture == Gesture.SWIPE_RIGHT) {
            // go to next word
            return true;
        } else if (gesture == Gesture.SWIPE_LEFT) {
            // go to prev word, or prev activity
            return true;
        }
        else if (gesture == Gesture.SWIPE_DOWN) {
            return false;
        }
        return false;
    }

    /**
     * Overridden to allow the gesture detector to process motion events that occur anywhere within
     * the activity.
     */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector.onMotionEvent(event);
    }
}
