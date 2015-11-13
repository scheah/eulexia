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
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by michelleawu on 11/12/15.
 * Creates a card scroll view containing all spelling suggestions.
 */
public class SuggestionActivity extends Activity {

    private static final String TAG = SpellCheckActivity.class.getSimpleName();

    // Index of misspelled words.
    // TODO: Create queue of misspelled words and cases for each. Currently, there is a hardcoded case example.
    // Visible for testing.
    static final int SUGG0 = 0;
    static final int SUGG1 = 1;

    private CardScrollAdapter mAdapter;
    private CardScrollView mCardScroller;

    // Visible for testing.
    CardScrollView getScroller() {
        return mCardScroller;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        mAdapter = new CardAdapter(createCards(this));
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(mAdapter);
        setContentView(mCardScroller);
        setCardScrollerListener();
    }

    /**
     * Create list of misspelled words.
     */
    private List<CardBuilder> createCards(Context context) {
        ArrayList<CardBuilder> cards = new ArrayList<CardBuilder>();
        cards.add(SUGG0, new CardBuilder(context, CardBuilder.Layout.MENU)
                .setText("tapioca")
                .setFootnote(R.string.suggestion_card_menu_description));
        cards.add(SUGG1, new CardBuilder(context, CardBuilder.Layout.MENU)
                .setText("typical")
                .setFootnote(R.string.suggestion_card_menu_description));
        return cards;

     /*   Bundle params = getIntent().getExtras();
        String word = params.getString("word"); // TODO generate suggestions and display*/

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
                        startActivity(new Intent(SuggestionActivity.this, SuggestionActivity.class));
                        break;
                    case SUGG1:
                        startActivity(new Intent(SuggestionActivity.this, SuggestionActivity.class));
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

    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        //Create a base listener for generic gestures
        gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                Log.e("tag", gesture.name());
                if (gesture == Gesture.TAP) {
                    // play spelling out loud
                    return true;
                } else if (gesture == Gesture.SWIPE_RIGHT) {
                    // go to next word
                    return true;
                } else if (gesture == Gesture.SWIPE_LEFT) {
                    // go to prev word, or prev activity
                    return true;
                }
                return false;
            }
        });

        return gestureDetector;
    }

}
