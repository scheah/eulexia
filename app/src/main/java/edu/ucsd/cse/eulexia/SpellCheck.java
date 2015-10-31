package edu.ucsd.cse.eulexia;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener; // asynchronous
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.Iterator;

import android.content.Context;
import android.content.res.AssetManager;


/**
 * Created by Sebastian on 10/26/2015.
 */
public class SpellCheck implements SpellCheckListener {
    private static String dictFile = "english.txt";
    private SpellChecker jazzySpellCheck = null;
    private String originalText;

    public SpellCheck(Context context, String bodyText) {
        originalText = bodyText;
        AssetManager assetManager = context.getApplicationContext().getResources().getAssets();
        InputStream inputStream = null;
        SpellDictionary dictionary = null;
        try {
            inputStream = assetManager.open(dictFile);
            File file = createFileFromInputStream(inputStream);
            dictionary = new SpellDictionaryHashMap(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        jazzySpellCheck = new SpellChecker(dictionary);
        jazzySpellCheck.addSpellCheckListener(this); // callbacks implemented in this class
        jazzySpellCheck.checkSpelling(new StringWordTokenizer(bodyText)); // asynchronous
    }

    public void spellingError(SpellCheckEvent event) {
        List suggestions = event.getSuggestions();
        System.out.println("Invalid Word: " + event.getInvalidWord());
        if (suggestions.size() > 0) {
            for (Iterator suggestedWord = suggestions.iterator(); suggestedWord.hasNext();) {
                System.out.println("\tSuggested Word: " + suggestedWord.next());
            }
        }
        else {
            System.out.println("\tNo suggestions");
        }
    }

    // file helper function
    private File createFileFromInputStream(InputStream inputStream) {
        try{
            File f = new File("copy.txt");
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;
            while((length=inputStream.read(buffer)) > 0) {
                outputStream.write(buffer,0,length);
            }
            outputStream.close();
            inputStream.close();
            return f;
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
