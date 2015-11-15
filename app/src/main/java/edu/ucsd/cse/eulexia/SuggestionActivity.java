package edu.ucsd.cse.eulexia;

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

    // Gesture detection
    private GestureDetector mGestureDetector;

    // Text to speech
    private TextToSpeech tts;
    private boolean initialized = false;
    private String queuedText;

    // Index of spelling suggestion
    // TODO: Create queue of misspelled words and cases for each. Currently, there is a hardcoded case example.
    // Visible for testing.

    // Suggestion array indices
    static final int SUGG0 = 0;
    static final int SUGG1 = 1;
    static final int SUGG2 = 2;

    // Array of suggested spellings
    private String[] suggArray = new String[3];

    private CardScrollAdapter mAdapter;
    private CardScrollView mCardScroller;

    // Visible for testing.
    CardScrollView getScroller() {
        return mCardScroller;
    }

    private void createSuggArray(String word) {
        if (word.equals("tapoica")){
            suggArray[0] = "tapioca";
            suggArray[1] = "typical";
            suggArray[2] = "topical";
        } else if(word.equals("galery")){
            suggArray[0] = "gallery";
            suggArray[1] = "glory";
            suggArray[2] = "gale";
        }
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // Retrieve saved word from SpellCheckActivity to create array of suggestions
        Bundle b = getIntent().getExtras();
        String w = b.getString("word");
        createSuggArray(w);

        // Create cards
        mAdapter = new CardAdapter(createCards(this));
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
        cards.add(SUGG0, new CardBuilder(context, CardBuilder.Layout.MENU)
                .setText(suggArray[SUGG0])
                .setFootnote(R.string.suggestion_card_menu_description));
        cards.add(SUGG1, new CardBuilder(context, CardBuilder.Layout.MENU)
                .setText(suggArray[SUGG1])
                .setFootnote(R.string.suggestion_card_menu_description));
        cards.add(SUGG2, new CardBuilder(context, CardBuilder.Layout.MENU)
                .setText(suggArray[SUGG2])
                .setFootnote(R.string.suggestion_card_menu_description));
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
        speak(suggArray[index].replace("", " ").trim());
    }

    /**
     * Different type of activities can be shown, when tapped on a card.
     */
    private void setCardScrollerListener() {
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Clicked view at position " + position + ", row-id " + id);
                int soundEffect = Sounds.TAP;

                // On tap, play audio spelling
                switch (position) {
                    case SUGG0:
                        spellWordAt(SUGG0);
                        break;
                    case SUGG1:
                        spellWordAt(SUGG1);
                        //startActivity(new Intent(SuggestionActivity.this, SuggestionActivity.class));
                        break;
                    case SUGG2:
                        spellWordAt(SUGG2);
                        break;

                    default:
                        soundEffect = Sounds.ERROR;
                        Log.d(TAG, "Don't show anything");
                }

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
