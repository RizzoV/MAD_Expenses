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
import java.util.Currency;

import it.polito.mad.team19.mad_expenses.Classes.ExpenseDetail;
import it.polito.mad.team19.mad_expenses.R;

/**
 * Created by Valentino on 29/04/2017.
 */

public class ExpenseDetailsAdapter extends BaseAdapter {

    private ArrayList<ExpenseDetail> detailsList = new ArrayList<>();
    private Activity context;

    public ExpenseDetailsAdapter(Context context, ArrayList<ExpenseDetail> detailsList) {
        this.detailsList = detailsList;
        this.context = (Activity) context;
    }

    public void setListData(ArrayList<ExpenseDetail> detailsList) {
        this.detailsList = detailsList;
    }

    @Override
    public int getCount() {
        return detailsList.size();
    }

    @Override
    public Object getItem(int position) {
        return detailsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = context.getLayoutInflater().inflate(R.layout.expense_details_list_row, parent, false);
        }

        TextView creditorName = (TextView) convertView.findViewById(R.id.creditor_name);
        TextView debtorName = (TextView) convertView.findViewById(R.id.debtor_name);
        TextView amount = (TextView) convertView.findViewById(R.id.debt_amount);
        final ImageView creditorImage = (ImageView) convertView.findViewById(R.id.creditor_icon);
        final ImageView debtorImage = (ImageView) convertView.findViewById(R.id.debtor_icon);

        ExpenseDetail ed = detailsList.get(position);

        creditorName.setText(ed.getCreditor());
        debtorName.setText(ed.getDebtor());
        amount.setText(ed.getAmount() + " " + Currency.getInstance("EUR").getSymbol());


        // Manage creditor icon
        if (ed.getCreditorImage() != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReferenceFromUrl(ed.getCreditorImage());
            final long ONE_MEGABYTE = 1024 * 1024;
            storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    creditorImage.setImageBitmap(getCircleBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length)));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("ExpenseDetailsAdapter", "Error in the getBytes() function");
                }
            });
        } else {
            creditorImage.setImageResource(R.drawable.man1);
        }

        // Manage debtor icon
        if (ed.getDebtorImage() != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReferenceFromUrl(ed.getDebtorImage());
            final long ONE_MEGABYTE = 1024 * 1024;
            storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    debtorImage.setImageBitmap(getCircleBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length)));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    //TODO: Handle any errors
                }
            });
        } else {
            debtorImage.setImageResource(R.drawable.man1);
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
