package edu.ucsd.cse.eulexia.card;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.List;

import edu.ucsd.cse.eulexia.R;

/**
 * Created by michelleawu on 11/12/15.
 * Adapter class that handles list of cards.
 */
public class CardAdapter extends CardScrollAdapter {

    final List<CardBuilder> mCards;

    List<String> msWords;

    private Context mContext;

    public CardAdapter(List<CardBuilder> cards, Context context, List<String> words) {
        mContext = context;
        mCards = cards;
        msWords = words;
    }

    public CardAdapter(List<CardBuilder> cards, Context context) {
        mContext = context;
        mCards = cards;
    }

    @Override
    public int getCount() {
        return mCards.size();
    }

    @Override
    public Object getItem(int position) {
        return mCards.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mCards.get(position).getView(convertView, parent);
        TextView textView1 = (TextView) view.findViewById(R.id.textView);
        Typeface tf = Typeface.createFromAsset(mContext.getAssets(),
                "fonts/bookmanoldstyle.ttf");
//        textView1.setTypeface(tf);
        if(msWords != null){
            textView1.setText(msWords.get(position));
        }

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return CardBuilder.getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position){
        return mCards.get(position).getItemViewType();
    }

    @Override
    public int getPosition(Object item) {
        for (int i = 0; i < mCards.size(); i++) {
            if (getItem(i).equals(item)) {
                return i;
            }
        }
        return AdapterView.INVALID_POSITION;
    }
}
