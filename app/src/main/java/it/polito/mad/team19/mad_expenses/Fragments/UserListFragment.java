package it.polito.mad.team19.mad_expenses.Fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tokenautocomplete.TokenCompleteTextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import it.polito.mad.team19.mad_expenses.Adapters.ContactListAdapter;
import it.polito.mad.team19.mad_expenses.Classes.ContactsCompletionView;
import it.polito.mad.team19.mad_expenses.Classes.UserContact;
import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by ikkoyeah on 14/04/17.
 */

public class UserListFragment extends Fragment {

    ArrayList<UserContact> contactList;
    ArrayList<UserContact> selected = new ArrayList<UserContact>();;
    ProgressDialog pDialog;
    private Handler updateBarHandler;
    ListView mListView;
    TextView debug_tv;
    ContactListAdapter adapter;

    ContactsCompletionView completionView;
    MenuItem btn_getContacts;

    RelativeLayout debug_ll;

    /**
     * Variables
     */

    public UserListFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static UserListFragment newInstance(int sectionNumber) {
        UserListFragment fragment = new UserListFragment();

        return fragment;
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.activity_create_group_frag1, container, false);

        completionView = (ContactsCompletionView) rootView.findViewById(R.id.addContacts);
        mListView = (ListView) rootView.findViewById(R.id.contacts_lv);
        debug_ll = (RelativeLayout) rootView.findViewById(R.id.debug_ll);
        Button btn_add = (Button) rootView.findViewById(R.id.btn_add_contact);
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Reading contacts...");
        pDialog.setCancelable(false);
        pDialog.show();

        setHasOptionsMenu(true);

        updateBarHandler =new Handler();


        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddContactDialog();
            }
        });


        new Thread(new Runnable() {
            @Override
            public void run() {
                getContacts();
            }
        }).start();

        return rootView;
    }

    public void getContacts() {
        contactList = new ArrayList<UserContact>();
        boolean changed = false; //mettere nell on textchanged quando si clicca
        ContentResolver resolver = null;
        String phoneNumber = null;
        String email = null;
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
        Uri EmailCONTENT_URI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
        String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
        String DATA = ContactsContract.CommonDataKinds.Email.DATA;
        ContentResolver contentResolver = getContext().getContentResolver();
        final Cursor cursor;
        final int[] counter = new int[1];
        cursor = contentResolver.query(CONTENT_URI, null, null, null, null);
        // Iterate every contact in the phone
        if (cursor.getCount() > 0) {
            counter[0] = 0;
            while (cursor.moveToNext()) {
                // Update the progress message
                updateBarHandler.post(new Runnable() {
                    public void run() {
                        pDialog.setMessage("Reading contacts : "+ counter[0]++ +"/"+cursor.getCount());
                    }
                });
                Bitmap bit_thumb = null;
                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
                String image_thumb = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));
                try {
                    if (image_thumb != null) {
                        bit_thumb = getCircleBitmap(MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), Uri.parse(image_thumb)));
                    } else {
                        //Log.e("No Image Thumb", "--------------");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //This is to read multiple phone numbers associated with the same contact
                        /*Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);
                        while (phoneCursor.moveToNext()) {
                            phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                            output.append("\n Phone number:" + phoneNumber);
                        }
                        phoneCursor.close();*/
                // Read every email id associated with the contact
                Cursor emailCursor = contentResolver.query(EmailCONTENT_URI,    null, EmailCONTACT_ID+ " = ?", new String[] { contact_id }, null);
                while (emailCursor.moveToNext()) {
                    email = emailCursor.getString(emailCursor.getColumnIndex(DATA));
                    if(!email.toString().trim().isEmpty() && email!=null)
                    {
                        contactList.add(new UserContact(name,email,bit_thumb));
                        //Log.e("Contacts",contactList.toString());
                    }
                }
                emailCursor.close();

                // Add the contact to the ArrayList
            }
            // ListView has to be updated using a ui thread
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Collections.sort(contactList, new Comparator<UserContact>() {
                        @Override
                        public int compare(UserContact s1, UserContact s2) {
                            return s1.getName().toLowerCase().compareTo(s2.getName().toLowerCase());
                        }
                    });

                    adapter = new ContactListAdapter(getContext(),contactList);
                    mListView.setAdapter(adapter);

                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Log.e("Selected",position+"");
                            Log.e("Selected2",mListView.getItemAtPosition(position).toString());
                            UserContact current = (UserContact)mListView.getItemAtPosition(position);
                            if(current.isChecked()) {
                                current.uncheck();
                                completionView.removeObject(current);
                            }
                            else{
                                current.check();
                                completionView.addObject(current);
                            }
                            //adapter.notifyDataSetChanged();
                        }
                    });

                    completionView.allowDuplicates(false);

                    completionView.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                            String[] tokens = s.toString().split(",");
                            if(tokens.length > 1)
                            {
                                debug_ll.setVisibility(View.GONE);
                                adapter.filter(tokens[tokens.length - 1].trim());
                                if(adapter.getCount()==0)
                                    debug_ll.setVisibility(View.VISIBLE);
                                else
                                    debug_ll.setVisibility(View.GONE);

                            }
                            else
                            {
                                adapter.filter(s.toString().trim());
                                if(adapter.getCount()==0)
                                    debug_ll.setVisibility(View.VISIBLE);
                                else
                                    debug_ll.setVisibility(View.GONE);
                            }

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                        }
                    });

                    completionView.setTokenListener(new TokenCompleteTextView.TokenListener<UserContact>() {
                        @Override
                        public void onTokenAdded(final UserContact token) {
                            btn_getContacts.setVisible(true);
                            btn_getContacts.setEnabled(true);
                            selected.add(token);
                            token.check();
                            adapter.notifyDataSetChanged();
                            Log.e("Selected",selected.size()+"");
                        }

                        @Override
                        public void onTokenRemoved(UserContact token) {
                            selected.remove(token);
                            if(selected.size()==0)
                            {
                                btn_getContacts.setVisible(false);
                                btn_getContacts.setEnabled(false);
                            }
                            token.uncheck();
                            //adapter.notifyDataSetChanged();
                            Log.e("Selected",selected.size()+"");
                        }

                    });

                }
            });
            // Dismiss the progressbar after 500 millisecondds
            updateBarHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pDialog.cancel();
                }
            }, 500);
        }
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }

    public void showAddContactDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.add_contact_dialog, null);
        final EditText emailEt = (EditText) dialogView.findViewById(R.id.add_email);


        final AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setTitle("Aggiungi contatto")
                .setPositiveButton("Aggiungi", null)
                .setNegativeButton("Annulla", null)
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {

                Button buttonPositive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                buttonPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(emailEt.getText().toString().trim().isEmpty())
                        {
                            Log.e("uao","no uao 1");
                            emailEt.setError(getString(R.string.mandatory_field));
                        }
                        else {
                            if (!emailEt.getText().toString().contains("@")) {
                                emailEt.setError("Inserisci una mail valida!");
                                Log.e("uao","no uao 1");


                            } else {
                                UserContact newContact = new UserContact(emailEt.getText().toString().trim().toLowerCase(), emailEt.getText().toString().trim().toLowerCase(), BitmapFactory.decodeResource(getContext().getResources(), R.drawable.circle));
                                contactList.add(newContact);
                                completionView.addObject(newContact);
                                dialog.cancel();
                            }
                        }
                    }
                });

                Button buttonNegative = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                buttonNegative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                    }
                });
            }
        });
        alertDialog.show();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater);
        btn_getContacts = menu.findItem(R.id.getContacts);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.getContacts:

                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("contacts",selected);
                GroupCreateFragment nextFrag= new GroupCreateFragment();
                nextFrag.setArguments(bundle);
                this.getFragmentManager().beginTransaction()
                        .replace(R.id.create_group_fragment_frame, nextFrag, null)
                        .addToBackStack(null)
                        .commit();
        }

        return false;
    }
}
