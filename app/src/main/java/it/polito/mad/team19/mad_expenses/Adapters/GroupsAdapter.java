package it.polito.mad.team19.mad_expenses.Adapters;

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
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.Classes.Group;
import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by Jured on 24/03/17.
 */

public class GroupsAdapter extends BaseAdapter {

    ArrayList<Group> groupList;
    Activity context;

    public GroupsAdapter(Context context, ArrayList<Group> groupList) {
        this.groupList = groupList;
        this.context = (Activity) context;
    }

    @Override
    public int getCount() {
        return groupList.size();
    }

    @Override
    public Object getItem(int position) {
        return groupList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView==null)
        {
            convertView=context.getLayoutInflater().inflate(R.layout.groups_list_row,parent,false);
        }

        TextView name=(TextView)convertView.findViewById(R.id.group_name_tv);
        TextView balance=(TextView)convertView.findViewById(R.id.balance_tv);
        TextView notifications=(TextView)convertView.findViewById(R.id.notification_cnt_tv);
        final ImageView image = (ImageView) convertView.findViewById(R.id.group_image);
        ImageView notification_back = (ImageView) convertView.findViewById(R.id.notification_back);


        Group group=groupList.get(position);

        /* Manage group name */
        name.setText(group.getName());

        /* Manage personal balance in group */
        Float balanceAmount = group.getBalance();
        if(balanceAmount>0)
            balance.setText("Devi dare: " + String.format("%.2f", group.getBalance()));
        if(balanceAmount<0)
            balance.setText("Devi ricevere: " + String.format("%.2f", group.getBalance()));
        if(balanceAmount==0)
            balance.setText("Non hai nessun debito/credito");

        /* Manage notifications count */
        if(group.getNotifyCnt()>0)
            notifications.setText(group.getNotifyCnt().toString());
        else {
            notifications.setVisibility(View.INVISIBLE);
            notification_back.setVisibility(View.INVISIBLE);
        }

        /* Manage group image */
        //TODO: prendere l'immagine dalla memoria e non direttamente da firebase (LUDO)
        if(group.getImage() != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReferenceFromUrl(group.getImage());
            final long ONE_MEGABYTE = 1024 * 1024;
            storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    image.setImageBitmap(getCircleBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length)));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    //TODO: Handle any errors
                }
            });
        }
        else {
            image.setImageResource(R.mipmap.ic_group);
        }

        return convertView;
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
}
