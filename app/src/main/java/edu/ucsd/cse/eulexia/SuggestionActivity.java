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
import android.app.IntentService;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.lang.annotation.Documented;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;

import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by michelleawu on 11/12/15.
 * Creates a card scroll view containing all spelling suggestions.
 */
public class SuggestionActivity extends Activity implements TextToSpeech.OnInitListener {

    private static final String WORDLOG_FILENAME = "wordlog";
    private static final String TAG = SpellcheckActivity.class.getSimpleName();
    private static final String TAG2 = "Spellz";

    // Gesture detection
    private GestureDetector mGestureDetector;

    // Text to speech
    private TextToSpeech tts;
    private boolean initialized = false;
    private String queuedText;

   // private Map<String, Integer> wordCountMap;
  //  private HashMap<String, Integer> wordCountMap = new HashMap<String, Integer>();

    // ArrayList of suggested spellings
    ArrayList<String> suggList = new ArrayList<String>();
    ArrayList<String> orderedSuggList = new ArrayList<String>();
    //PriorityQueue<Suggestion> orderedSQ = new PriorityQueue<Suggestion>();

    private CardScrollAdapter mAdapter;
    private CardScrollView mCardScroller;

    // Visible for testing.
    CardScrollView getScroller() {
        return mCardScroller;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Retrieve saved suggestions from SpellCheckActivity
        Bundle b = getIntent().getExtras();
        suggList = b.getStringArrayList("suggestions");

       /* try {
            // read hashmap from file
            FileInputStream fileInputStream = openFileInput(WORDLOG_FILENAME);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            wordCountMap = (HashMap)objectInputStream.readObject();
            Log.d(TAG2, " caught");
            objectInputStream.close();

        } catch (Exception e) {
            // file not found - do something
            Log.d(TAG2, "Exception caught");
          //  wordCountMap.put()
        }*/

        // Create cards
        mAdapter = new CardAdapter(createCards(this), getBaseContext());
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(mAdapter);
        setContentView(mCardScroller);
        setCardScrollerListener();

        tts = new TextToSpeech(this /* context */, this /* listener */);
    }

    @Override
    protected void onDestroy() {
    /*    try {
            // write new data back to file
            FileOutputStream fileOutputStream = openFileOutput(WORDLOG_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(wordCountMap);
            objectOutputStream.close();
        } catch(Exception e) {
            // error
        }*/
    }

    /**
     * Create list of spelling suggestions.
     */
    private List<CardBuilder> createCards(Context context) {
        Log.d(TAG2, "Size of sugg list is " + suggList.size());
        // If the suggested word is not already in the word count map, add it in
        for(int j = 0; j < suggList.size(); j++) {
            String currSugg = suggList.get(j);
            Log.d(TAG2, "suggestions[" + j + "] is " + currSugg);
     /*       if(!wordCountMap.containsKey(currSugg)) {
                wordCountMap.put(currSugg, 1);
            }*/
        }

        // prioritize suggestions
      /*  for (String s : suggList) {
            if(wordCountMap.containsKey(s)) {
                Suggestion sugg = new Suggestion(s, wordCountMap.get(s));
                orderedSQ.add(sugg);
            } else {
                Suggestion sugg = new Suggestion(s, 0);
                orderedSQ.add(sugg);
            }
        }

        // reorder list - need this for updating suggestion priorities
        for(Suggestion s : orderedSQ) {
            orderedSuggList.add(s.word);
        }

        Log.d(TAG2, "Size of ordered sugg list 1 is " + orderedSuggList.size());
        int i = 0;
        for(String word : orderedSuggList){
            Log.d(TAG2, "suggestions[" + i + "] is " + word);
            i++;
        }*/

        int i = 0;
        ArrayList<CardBuilder> cards = new ArrayList<CardBuilder>();
        for(String word : suggList){
            cards.add(i, new CardBuilder(context, CardBuilder.Layout.MENU)
            .setText(word)
            .setFootnote(R.string.suggestion_card_menu_description));
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

                // Play success sound effect when user selects correct spelling
                int soundEffect = Sounds.SUCCESS;
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(soundEffect);

                // update record
                String word = suggList.get(position);
              //  Log.d(TAG2, "wordCountMap.get(word) is " + wordCountMap.get(word));
               // wordCountMap.put(word, wordCountMap.get(word) + 1);
            }
        });

        mCardScroller.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG2, "Long clicked view at position " + position + ", row-id " + id);
                // On long tap, play audio spelling
                int soundEffect = Sounds.TAP;
                /*soundEffect = Sounds.ERROR;
                Log.d(TAG, "Don't show anything");*/

                // Play sound.
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(soundEffect);

                spellWordAt(position);
                return true;
            }
        });
    }
}
