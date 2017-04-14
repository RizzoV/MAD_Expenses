package it.polito.mad.team19.mad_expenses.Classes;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tokenautocomplete.TokenCompleteTextView;

import it.polito.mad.team19.mad_expenses.AccountActivity;
import it.polito.mad.team19.mad_expenses.R;


/**
 * Created by ikkoyeah on 11/04/17.
 */

public class ContactsCompletionView extends TokenCompleteTextView<UserContact> {
    public ContactsCompletionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected View getViewForObject(final UserContact person) {

        LayoutInflater l = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        LinearLayout view = (LinearLayout) l.inflate(R.layout.contact_token, (ViewGroup) getParent(), false);
        TextView name = (TextView) view.findViewById(R.id.name);
        ImageView thumb = (ImageView) view.findViewById(R.id.thumb);

        setTokenClickStyle(TokenClickStyle.Delete);

        name.setText(person.getEmail());
        try {

            if (person.getThumb() != null) {
                thumb.setImageBitmap(person.getThumb());
            } else {
                thumb.setImageResource(R.drawable.circle);
            }
            // Seting round image
        } catch (OutOfMemoryError e) {
            // Add default picture
            e.printStackTrace();
        }

        return view;
    }

    @Override
    protected UserContact defaultObject(String completionText) {
        //Stupid simple example of guessing if we have an email or not
        int index = completionText.indexOf('@');
        Bitmap defThumb = BitmapFactory.decodeResource(getContext().getResources(),
                R.drawable.circle);
        if (index == -1) {
            return null;
        } else {
            return new UserContact(completionText.substring(0, index), completionText,defThumb);
        }
    }

}
