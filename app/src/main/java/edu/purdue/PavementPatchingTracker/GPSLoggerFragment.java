package edu.purdue.PavementPatchingTracker;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GPSLoggerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GPSLoggerFragment extends Fragment {

    public static final int REQUEST_TAKE_PHOTO = 1;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param vehicalName Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GPSLoggerFragment.
     */

    public static GPSLoggerFragment newInstance(String vehicalName, String param2) {
        GPSLoggerFragment fragment = new GPSLoggerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, vehicalName);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public GPSLoggerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);


//            Activity act = getActivity();
//            TextView tv = (TextView)act.findViewById(R.id.textViewVehicleTypeLabel);
//            tv.setText(mParam1);
           // ((TextView)(getActivity().findViewById(R.id.textViewVehicleTypeLabel))).setText(mParam1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        ((TextView)(getView().findViewById(R.id.textViewVehicleTypeLabel))).setText(mParam1);
        View view = inflater.inflate(R.layout.fragment_gpslogger, container, false);
        ((TextView)(view.findViewById(R.id.textViewVehicleTypeLabel))).setText(mParam1);
        return view;
    }

    String imageFilename="Yaguang";



}
