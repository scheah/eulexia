package edu.ucsd.cse.eulexia;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener; // asynchronous
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;


/**
 * Created by Sebastian on 10/26/2015.
 */
public class SpellCheck implements SpellCheckListener {
    private static String dictFile = "english.0";
    private SpellChecker jazzySpellCheck = null;
    private String originalText;
    public ArrayList<String> misspelledWords = new ArrayList<String>();
    public HashMap<String, ArrayList<String> > suggestions = new HashMap<String, ArrayList<String>>();

    public SpellCheck(Context context, String bodyText) {
        originalText = bodyText;
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = null;
        SpellDictionary dictionary = null;
        try {
            String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/eulexia/english.txt";
            inputStream = assetManager.open(dictFile);
            File file = createFileFromInputStream(inputStream, filename);
            dictionary = new SpellDictionaryHashMap(file);
        } catch (IOException e) {
            e.getMessage();
            e.printStackTrace();
        }
        jazzySpellCheck = new SpellChecker(dictionary);
        jazzySpellCheck.addSpellCheckListener(this); // callbacks implemented in this class
        jazzySpellCheck.checkSpelling(new StringWordTokenizer(bodyText)); // asynchronous
    }

    public void spellingError(SpellCheckEvent event) {
        List suggestionsList = event.getSuggestions();
        String invalidWord = event.getInvalidWord();
        System.out.println("Invalid Word: " + event.getInvalidWord());
        misspelledWords.add(event.getInvalidWord());
        if (suggestionsList.size() > 0) {
            for (Iterator suggestedWord = suggestionsList.iterator(); suggestedWord.hasNext();) {
                String currentSuggestion = (String)suggestedWord.next();
                ArrayList suggestionsForWord = suggestions.get(invalidWord);
                if (suggestionsForWord != null)
                    suggestionsForWord.add(currentSuggestion);
                else
                    suggestions.put(invalidWord, new ArrayList<String>());
                System.out.println("\tSuggested Word: " + currentSuggestion);
            }
        }
        else {
            System.out.println("\tNo suggestions");
        }
    }

    // file helper function
    private File createFileFromInputStream(InputStream inputStream, String path) {
        File directory = new File(Environment.getExternalStorageDirectory(), "eulexia");
        directory.mkdirs();
        File file = new File(Environment.getExternalStorageDirectory(), "eulexia/english.txt");
        if (!file.exists()) {
            try {

                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
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
