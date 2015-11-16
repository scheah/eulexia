package edu.ucsd.cse.eulexia;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener; // asynchronous
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;

import android.content.Context;
import android.content.res.AssetManager;


/**
 * Created by Sebastian on 10/26/2015.
 */
public class SpellCheck implements SpellCheckListener {
    private static SpellCheck instance = null; // singleton, we do not want to keep re-reading a dictionary
    private static String dictFile = "english.0";
    private static SpellChecker jazzySpellCheck = null;
    private String mOriginalText = null;
    private ArrayList<String> mMisspelledWords = new ArrayList<String>();
    private HashMap<String, ArrayList<String> > mSuggestions = new HashMap<String, ArrayList<String>>();

    protected SpellCheck(Context context/*, String bodyText*/) {
        //mOriginalText = bodyText;
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = null;
        SpellDictionary dictionary = null;
        try {
            inputStream = assetManager.open(dictFile);
            Reader reader = new InputStreamReader(inputStream, "UTF-8");
            dictionary = new SpellDictionaryHashMap(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        jazzySpellCheck = new SpellChecker(dictionary);
        jazzySpellCheck.addSpellCheckListener(this); // callbacks implemented in this class, can possibly add listener to a different class
        //jazzySpellCheck.checkSpelling(new StringWordTokenizer(bodyText)); // asynchronous
    }

    public static SpellCheck getInstance(Context context) { // *Spellcheck.getInstance
        if (instance == null) {
            instance = new SpellCheck(context);
        }
        return instance;
    }

    public void checkWords(String bodyText) {
        mMisspelledWords.clear();
        mSuggestions.clear();
        mOriginalText = bodyText;
        jazzySpellCheck.checkSpelling(new StringWordTokenizer(bodyText)); // *asynchronous
    }

    public void spellingError(SpellCheckEvent event) {
        String invalidWord = event.getInvalidWord();
        List suggestionsList = event.getSuggestions();
        //System.out.println("Invalid Word: " + event.getInvalidWord());
        if (mSuggestions.containsKey(invalidWord)) // if we already recorded this misspell previously, exit
            return;
        mMisspelledWords.add(event.getInvalidWord());
        ArrayList suggestionsForWord = new ArrayList<String>();
        for (Iterator suggestedWord = suggestionsList.iterator(); suggestedWord.hasNext();) {
            Word currentSuggestion = (Word) suggestedWord.next();
            suggestionsForWord.add(currentSuggestion.getWord());
            //System.out.println("\tSuggested Word: " + currentSuggestion);
        }
        mSuggestions.put(invalidWord, suggestionsForWord);

    }

    public int getNumMisspelledWords() {
        return mMisspelledWords.size();
    }

    public int getNumSuggestions(String invalidWord) {
        ArrayList suggestions = mSuggestions.get(invalidWord);
        if (suggestions != null) {
            return suggestions.size();
        }
        return 0;
    }

    public ArrayList<String> getMisspelledWords(){
        return mMisspelledWords;
    }

    public ArrayList<String> getSuggestionsForWord(String key) {
        return mSuggestions.get(key);
    }
}
