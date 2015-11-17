package edu.ucsd.cse.eulexia;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener; // asynchronous
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
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
    private Context mContext;
    private static SpellCheck instance = null; // singleton, we do not want to keep re-reading a dictionary
    private static String dictFile = "english.0";
    private static String userSuggestionFile = "usersuggestions.0";
    private static SpellChecker jazzySpellCheck = null;
    private String mOriginalText = null;
    private ArrayList<String> mMisspelledWords = new ArrayList<String>();
    private HashMap<String, ArrayList<String> > mSuggestions = new HashMap<String, ArrayList<String>>();
    private HashMap<String, String> mUserSuggestions = null;

    protected SpellCheck(Context context/*, String bodyText*/) {
        //mOriginalText = bodyText;
        mContext = context;
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
        File directory = context.getFilesDir();
        if (!directory.exists()) directory.mkdirs();
        File file = new File(context.getFilesDir(), userSuggestionFile);
        try {
            if(file.exists()) {
                FileInputStream fileInputStream = new FileInputStream(file);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                mUserSuggestions = (HashMap) objectInputStream.readObject();
                objectInputStream.close();
            }
            else {
                file.createNewFile();
                mUserSuggestions = new HashMap<String, String>();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

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

    public String getUserSuggestion(String misspelledWord) {
        return mUserSuggestions.get(misspelledWord);
    }

    public void addUserSuggestion(String misspelledWord, String suggestedWord) {
        mUserSuggestions.put(misspelledWord, suggestedWord);
        File file = new File(mContext.getFilesDir(), userSuggestionFile);
        try{
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(mUserSuggestions);
            objectOutputStream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
