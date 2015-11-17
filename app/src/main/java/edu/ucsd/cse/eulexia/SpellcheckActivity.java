package edu.ucsd.cse.eulexia;

import com.google.android.glass.media.Sounds;
import edu.ucsd.cse.eulexia.card.CardAdapter;
import edu.ucsd.cse.eulexia.SuggestionActivity;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michelleawu on 11/12/15.
 * Creates a card scroll view containing all misspelled words.
 */
public class SpellcheckActivity extends Activity {

    private static final String TAG = SpellcheckActivity.class.getSimpleName();
    private static final String TAG2 = "Spellz";


    // List of misspelled words.
    private static ArrayList<String> msWords = new ArrayList<String>();

    private CardScrollAdapter mAdapter;
    private CardScrollView mCardScroller;
    private SpellCheck mSpellChecker;

    // Visible for testing.
    CardScrollView getScroller() {
        return mCardScroller;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // TODO: Fill msWords with words recognized via OCR
        //msWords.add("galery");
        //msWords.add("finaly");

        // get data from OCRActivity
        Bundle b = getIntent().getExtras();
        msWords = b.getStringArrayList("ocrResults");

        mAdapter = new CardAdapter(createCards(this));
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(mAdapter);
        setContentView(mCardScroller);
        setCardScrollerListener();
        mSpellChecker = SpellCheck.getInstance(getApplicationContext());
        Log.d(TAG2, "Done loading Jazzy");
    }

    /**
     * Create list of misspelled words.
     */
    private List<CardBuilder> createCards(Context context) {
        ArrayList<CardBuilder> cards = new ArrayList<CardBuilder>();
        int i = 0;
        for(String word : msWords) {
            cards.add(i, new CardBuilder(context, CardBuilder.Layout.MENU)
                    .setText(word)
                    .setFootnote(R.string.misspelled_card_menu_description));
            i++;
        }
        return cards;
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

    // Check misspelled word against dictionary and pass suggestions to next intent
    private void checkSpelling(int index, Intent _intent, Bundle _params){
        String currWord = msWords.get(index);

        // Check misspelled word
        mSpellChecker.checkWords(currWord);

        // Pass suggested words to SuggestionActivity
        _params.putStringArrayList("suggestions", mSpellChecker.getSuggestionsForWord(currWord));

        _intent.putExtras(_params);
        startActivity(_intent);

        finish();
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

                final Intent intent = new Intent(SpellcheckActivity.this, SuggestionActivity.class);
                Bundle params = new Bundle();

                checkSpelling(position, intent, params);

                /*soundEffect = Sounds.ERROR;
                Log.d(TAG, "Don't show anything");*/

                // Play sound.
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(soundEffect);
            }
        });
    }

    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        final Intent intent = new Intent(context, SuggestionActivity.class);

        //Create a base listener for generic gestures
        gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                Log.e("tag", gesture.name());
                if (gesture == Gesture.TAP) {
                    return true;
                } else if (gesture == Gesture.SWIPE_RIGHT) {
                    // go to next word
                    return true;
                } else if (gesture == Gesture.SWIPE_LEFT) {
                    // go to prev word
                    return true;
                }
                return false;
            }
        });

        return gestureDetector;
    }
}
