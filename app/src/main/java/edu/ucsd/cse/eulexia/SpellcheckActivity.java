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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by michelleawu on 11/12/15.
 * Creates a card scroll view containing all misspelled words.
 */
public class SpellcheckActivity extends Activity {

    private static final String TAG = SpellcheckActivity.class.getSimpleName();

    // Index of misspelled words.
    // TODO: Create queue of misspelled words and cases for each. Currently, there is a hardcoded case example.
    // Visible for testing.
    static final int WORD0 = 0;
    static final int WORD1 = 1;

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
        cards.add(WORD0, new CardBuilder(context, CardBuilder.Layout.MENU)
                .setText("tapoica")
                .setFootnote(R.string.misspelled_card_menu_description));
        cards.add(WORD1, new CardBuilder(context, CardBuilder.Layout.MENU)
                .setText("galery")
                .setFootnote(R.string.misspelled_card_menu_description));
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

    /**
     * Different type of activities can be shown, when tapped on a card.
     */
    private void setCardScrollerListener() {
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Clicked view at position " + position + ", row-id " + id);
                int soundEffect = Sounds.TAP;

                final Intent intent = new Intent(SpellcheckActivity.this, SuggestionActivity.class);
                Bundle params = new Bundle();

                switch (position) {
                    case WORD0:
                        // Pass word to SuggestionActivity
                        params.putString("word", "tapoica");
                        intent.putExtras(params);
                        startActivity(intent);
                        finish();
                        break;
                    case WORD1:
                        // Pass word to SuggestionActivity
                        params.putString("word", "galery");
                        intent.putExtras(params);
                        startActivity(intent);
                        finish();
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
        final Intent intent = new Intent(context, SuggestionActivity.class);

        //Create a base listener for generic gestures
        gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                Log.e("tag", gesture.name());
                if (gesture == Gesture.TAP) {
                    // go to suggestions
                    Bundle params = new Bundle();
                    params.putString("word", "misspled"); // TODO replace with real params
                    intent.putExtras(params);
                    startActivity(intent);
                    finish();
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
