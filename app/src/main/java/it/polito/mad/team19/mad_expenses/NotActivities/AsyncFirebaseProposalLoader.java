package it.polito.mad.team19.mad_expenses.NotActivities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import it.polito.mad.team19.mad_expenses.AddProposalActivity;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseGroupMember;
import it.polito.mad.team19.mad_expenses.Classes.FirebaseProposal;

/**
 * Created by Valentino on 24/05/17.
 */

public class AsyncFirebaseProposalLoader extends AsyncTask<Void,Void,Void> {

    private String proposalId;
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
    private String currencyCode;

    private Context mContext;

    public AsyncFirebaseProposalLoader(String proposalId, String groupId, String usrId, String mCurrentPhotoPath, String mCurrentPhotoName,
                                       String nameEditText, String descriptionEditText, String costEditText, String currencyCode, Context mContext) {
        this.proposalId = proposalId;
        this.groupId = groupId;
        this.usrId = usrId;
        this.mCurrentPhotoPath = mCurrentPhotoPath;
        this.mCurrentPhotoName = mCurrentPhotoName;
        this.nameEditText = nameEditText;
        this.descriptionEditText = descriptionEditText;
        this.costEditText = costEditText;
        this.mContext = mContext;
        this.currencyCode = currencyCode;
    }

    @Override
    protected Void doInBackground(Void... params) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("gruppi").child(groupId).child("proposals");

        final DatabaseReference newProposalRef = myRef.child(proposalId);

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
                            newProposalRef.setValue(new FirebaseProposal(nameEditText, descriptionEditText, usrId, Double.parseDouble(costEditText.replace(",", ".")), currencyCode, uri.toString()));

                            // Vale: Aggiungi waitingFor
                            final ArrayList<FirebaseGroupMember> groupMembers = new ArrayList<>();

                            DatabaseReference groupMembersReference  = database.getReference().child("gruppi").child(groupId).child("membri");
                            groupMembersReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChildren()) {
                                        for(DataSnapshot member : dataSnapshot.getChildren()) {
                                            if(member.child("deleted").getValue() == null)
                                                groupMembers.add(new FirebaseGroupMember(member.child("nome").getValue(String.class), member.child("immagine").getValue(String.class), member.getKey()));
                                        }

                                        for(FirebaseGroupMember fbgm : groupMembers) {
                                            database.getReference().child("gruppi").child(groupId).child("proposals").child(proposalId).child("waitingFor").child(fbgm.getUid()).setValue(fbgm.getName());
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e("AsyncProposalWaitingFor", "Could not retrieve the list of group members");
                                }
                            });

                            ((AddProposalActivity)mContext).finishTasks();
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
            Log.d("DebugCaricamentoPropost", "NoImage");
            newProposalRef.setValue(new FirebaseProposal(nameEditText, descriptionEditText, usrId, Double.parseDouble(costEditText.replace(",",".")), currencyCode));

            // Vale: Aggiungi waitingFor
            final ArrayList<FirebaseGroupMember> groupMembers = new ArrayList<>();

            DatabaseReference groupMembersReference  = database.getReference().child("gruppi").child(groupId).child("membri");
            groupMembersReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChildren()) {
                        for(DataSnapshot member : dataSnapshot.getChildren()) {
                            if(member.child("deleted").getValue() == null)
                                groupMembers.add(new FirebaseGroupMember(member.child("nome").getValue(String.class), member.child("immagine").getValue(String.class), member.getKey()));
                        }

                        for(FirebaseGroupMember fbgm : groupMembers) {
                            database.getReference().child("gruppi").child(groupId).child("proposals").child(proposalId).child("waitingFor").child(fbgm.getUid()).setValue(fbgm.getName());
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("AsyncProposalWaitingFor", "Could not retrieve the list of group members");
                }
            });

            ((AddProposalActivity)mContext).finishTasks();
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
