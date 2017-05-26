package it.polito.mad.team19.mad_expenses;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FullscreenImageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FullscreenImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FullscreenImageFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String IMAGE_URL = "param1";

    private OnFragmentInteractionListener mListener;
    private String imageUrl;

    public FullscreenImageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param imageUrl Parameter 1.
     * @return A new instance of fragment FullscreenImageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FullscreenImageFragment newInstance(String imageUrl) {
        FullscreenImageFragment fragment = new FullscreenImageFragment();
        Bundle args = new Bundle();
        args.putString(IMAGE_URL, imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageUrl = getArguments().getString(IMAGE_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_fullscreen_image, container, false);


        ImageView fullScreenIV = (ImageView) rootView.findViewById(R.id.fullscreen_imageview);

        final Bitmap[] fileBitmap = new Bitmap[1];
        final byte[][] datas = new byte[1][1];
        Glide.with(this).load(imageUrl).asBitmap().error(R.drawable.circle).into(new BitmapImageViewTarget(fullScreenIV) {
            @Override
            protected void setResource(Bitmap resource) {
                fileBitmap[0] = resource;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                datas[0] = baos.toByteArray();
            }
        });

        fullScreenIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
