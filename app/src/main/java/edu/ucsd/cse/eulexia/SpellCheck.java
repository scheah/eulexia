package edu.ucsd.cse.eulexia;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
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
    private static String dictFile = "english.txt";
    private SpellChecker jazzySpellCheck = null;
    private String originalText;
    public ArrayList<String> misspelledWords = new ArrayList<String>();
    public HashMap<String, ArrayList<String> > suggestions = new HashMap<String, ArrayList<String>>();

    protected SpellCheck(Context context/*, String bodyText*/) {
        //originalText = bodyText;
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

    public static SpellCheck getInstance(Context context) {
        if (instance == null) {
            instance = new SpellCheck(context);
        }
        return instance;
    }

    public void checkWords(String bodyText) {
        misspelledWords.clear();
        suggestions.clear();
        originalText = bodyText;
        jazzySpellCheck.checkSpelling(new StringWordTokenizer(bodyText));
    }

    public void spellingError(SpellCheckEvent event) {
        String invalidWord = event.getInvalidWord();
        List suggestionsList = event.getSuggestions();
        //System.out.println("Invalid Word: " + event.getInvalidWord());
        if (suggestions.containsKey(invalidWord)) // if we already recorded this misspell previously, exit
            return;
        misspelledWords.add(event.getInvalidWord());
        ArrayList suggestionsForWord = new ArrayList<String>();
        for (Iterator suggestedWord = suggestionsList.iterator(); suggestedWord.hasNext();) {
            String currentSuggestion = (String) suggestedWord.next();
            suggestionsForWord.add(currentSuggestion);
            //System.out.println("\tSuggested Word: " + currentSuggestion);
        }
        suggestions.put(invalidWord, suggestionsForWord);

    }
}
