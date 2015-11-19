package edu.ucsd.cse.eulexia;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryCachedDichoDisk;
import com.swabunga.spell.engine.SpellDictionaryDichoDisk;
import com.swabunga.spell.engine.SpellDictionaryDisk;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener; // asynchronous
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;


/**
 * Created by Sebastian on 10/26/2015.
 */
public class SpellCheck implements SpellCheckListener {
    private Context mContext;
    private static SpellCheck instance = null; // singleton, we do not want to keep re-reading a dictionary
    private static String dictFile = "english.0";
//    private static String phoneticFile = "phonet.en";
    private SpellDictionary mDictionary = null;

    public static SpellChecker jazzySpellCheck = null;
    private String mOriginalText = null;
    private ArrayList<String> mMisspelledWords = new ArrayList<String>();
    private HashMap<String, ArrayList<String> > mSuggestions = new HashMap<String, ArrayList<String>>();


    protected SpellCheck(Context context/*, String bodyText*/) {
        mContext = context;
        //mOriginalText = bodyText;
        AssetManager assetManager = context.getAssets();
        try {
            File directoryOnDisk = mContext.getFilesDir();
            if (!directoryOnDisk.exists()) directoryOnDisk.mkdirs();
            InputStream dictInputStream = assetManager.open(dictFile);
//            Following code used for disk-based dictionary
//            File dict = createFileFromInputStream(dictInputStream, dictFile);
//            InputStream phoneticInputStream = assetManager.open(phoneticFile);
//            File phonetic = createFileFromInputStream(phoneticInputStream, phoneticFile);
//            mDictionary = new SpellDictionaryCachedDichoDisk(dict);
            Reader reader = new InputStreamReader(dictInputStream, "UTF-8");
            mDictionary = new SpellDictionaryHashMap(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        jazzySpellCheck = new SpellChecker(mDictionary);
        jazzySpellCheck.addSpellCheckListener(this); // callbacks implemented in this class, can possibly add listener to a different class
        //jazzySpellCheck.checkSpelling(new StringWordTokenizer(bodyText)); // asynchronous
    }

    public static SpellCheck getInstance(Context context) { // *Spellcheck.getInstance
        if (instance == null) {
            instance = new SpellCheck(context);
        }
        return instance;
    }

    public void setListener(SpellCheckListener activity) {
        jazzySpellCheck.addSpellCheckListener(activity);
    }

    public void checkWords(String bodyText) {
        mMisspelledWords.clear();
        mSuggestions.clear();
        mOriginalText = bodyText;
        jazzySpellCheck.checkSpelling(new StringWordTokenizer(bodyText)); // *asynchronous
    }

    public boolean checkWordSynchronous(String word) {
        mMisspelledWords.clear();
        mSuggestions.clear();
        boolean misspelled = !(mDictionary.isCorrect(word));
        if (misspelled) {
            List suggestionsList = mDictionary.getSuggestions(word, 5); //max 3 suggestions
            ArrayList suggestionsForWord = new ArrayList<String>();
            for (Iterator suggestedWord = suggestionsList.iterator(); suggestedWord.hasNext(); ) {
                Word currentSuggestion = (Word) suggestedWord.next();
                suggestionsForWord.add(currentSuggestion.getWord());
            }
            mSuggestions.put(word, suggestionsForWord);
        }
        return misspelled;
        // returns true if word was misspelled
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

    // file helper function
    private File createFileFromInputStream(InputStream inputStream, String filename) {
        File file = new File(mContext.getFilesDir(), filename);
        boolean bool = file.delete();
        if (!file.exists()) { // create it
            try {
                file.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                byte buf[] = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    fileOutputStream.write(buf, 0, len);
                }
                fileOutputStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
