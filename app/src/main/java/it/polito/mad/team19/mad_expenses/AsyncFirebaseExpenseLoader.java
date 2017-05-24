package it.polito.mad.team19.mad_expenses;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import it.polito.mad.team19.mad_expenses.Classes.FirebaseExpense;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;

/**
 * Created by Jured on 22/05/17.
 */

public class AsyncFirebaseExpenseLoader extends AsyncTask<Void,Void,Void> {

    private String idExpense;
    private String groupId;
    private String usrId;
    private String mCurrentPhotoPath;
    private StorageReference storageRef;
    private StorageReference groupImagesRef;
    private String mCurrentPhotoName;
    private Uri mCurrentPhotoFirebaseUri;

    private String nameEditText;
    private String descriptionEditText;
    private String costEditText;

    private ArrayList<FirebaseGroupMember> excludedList;
    private ArrayList<FirebaseGroupMember> contributorsList;

    private Boolean isModifyActivity;
    private String oldExpenseId;
    private Context mContext;

    public AsyncFirebaseExpenseLoader(String idExpense, String groupId, String usrId, String mCurrentPhotoPath, String mCurrentPhotoName, String nameEditText, String descriptionEditText, String costEditText
    , Boolean isModifyActivity, String oldExpenseId, ArrayList<FirebaseGroupMember> excludedList, ArrayList<FirebaseGroupMember> contributorsList, Context mContext) {
        this.idExpense = idExpense;
        this.groupId = groupId;
        this.usrId = usrId;
        this.mCurrentPhotoPath = mCurrentPhotoPath;
        this.mCurrentPhotoName = mCurrentPhotoName;
        this.nameEditText = nameEditText;
        this.descriptionEditText = descriptionEditText;
        this.costEditText = costEditText;
        this.isModifyActivity = isModifyActivity;
        this.oldExpenseId = oldExpenseId;
        this.excludedList = excludedList;
        this.contributorsList = contributorsList;
        this.mContext = mContext;

    }

    @Override
    protected Void doInBackground(Void... params) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("gruppi").child(groupId).child("expenses");

        final DatabaseReference newExpenseRef = myRef.child(idExpense);

        if (mCurrentPhotoPath != null) {
            storageRef = FirebaseStorage.getInstance().getReference();
            groupImagesRef = storageRef.child("images").child(groupId);
            final File imageToUpload = new File(mCurrentPhotoPath);
            Bitmap fileBitmap = shrinkBitmap(mCurrentPhotoPath, 1000, 1000);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            fileBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
            byte[] datas = baos.toByteArray();
            mCurrentPhotoName = imageToUpload.getName();
            UploadTask uploadTask = groupImagesRef.child(mCurrentPhotoName).putBytes(datas);


            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    // mCurrentPhotoFirebaseUri = taskSnapshot.getDownloadUrl();
                    groupImagesRef.child(mCurrentPhotoName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            newExpenseRef.setValue(new FirebaseExpense(usrId, nameEditText, descriptionEditText, Float.valueOf(costEditText.replace(",", ".")), uri.toString()));

                            for (FirebaseGroupMember member : excludedList) {
                                newExpenseRef.child("excluded").child(member.getUid()).child("nome").setValue(member.getName());
                                newExpenseRef.child("excluded").child(member.getUid()).child("immagine").setValue(member.getImgUrl());
                            }
                            for (FirebaseGroupMember member : contributorsList) {
                                newExpenseRef.child("contributors").child(member.getUid()).child("nome").setValue(member.getName());
                                newExpenseRef.child("contributors").child(member.getUid()).child("immagine").setValue(member.getImgUrl());
                            }

                            Log.d("DebugIsModifyFlag", isModifyActivity.toString());
                            if (isModifyActivity) {
                                newExpenseRef.child("oldVersionId").setValue(oldExpenseId);
                            }

                            ((AddExpenseActivity)mContext).finishTasks(nameEditText,descriptionEditText,usrId,uri.toString(),costEditText);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                            mCurrentPhotoFirebaseUri = Uri.EMPTY;
                        }
                    });
                }
            });
        } else {
            Log.d("DebugCaricamentoSpesa", "NoImage");
            newExpenseRef.setValue(new FirebaseExpense(usrId, nameEditText, descriptionEditText, Float.valueOf(costEditText.replace(",", "."))));
            for (FirebaseGroupMember member : excludedList) {
                newExpenseRef.child("excluded").child(member.getUid()).child("nome").setValue(member.getName());
                newExpenseRef.child("excluded").child(member.getUid()).child("immagine").setValue(member.getImgUrl());
            }
            for (FirebaseGroupMember member : contributorsList) {
                newExpenseRef.child("contributors").child(member.getUid()).child("nome").setValue(member.getName());
                newExpenseRef.child("contributors").child(member.getUid()).child("immagine").setValue(member.getImgUrl());
            }

            Log.d("DebugIsModifyFlag", isModifyActivity.toString());
            if (isModifyActivity) {
                newExpenseRef.child("oldVersionId").setValue(oldExpenseId);
            }

            ((AddExpenseActivity)mContext).finishTasks(nameEditText,descriptionEditText,usrId,"none",costEditText);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    private Bitmap shrinkBitmap(String file, int width, int height) {

        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds = true;
        Bitmap bitmap;
        BitmapFactory.decodeFile(file, bmpFactoryOptions); // Vale: No need to store the bitmap in the dedicated variable, I'm just loading its infos

        int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight / (float) height);
        int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth / (float) width);

        if (heightRatio > 1 || widthRatio > 1) {
            if (heightRatio > widthRatio)
                bmpFactoryOptions.inSampleSize = heightRatio;
            else
                bmpFactoryOptions.inSampleSize = widthRatio;
        }

        bmpFactoryOptions.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);
        return bitmap;
    }


}
