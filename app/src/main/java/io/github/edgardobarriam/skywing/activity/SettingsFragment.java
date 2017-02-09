package io.github.edgardobarriam.skywing.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import io.github.edgardobarriam.skywing.R;

import static android.content.Context.MODE_PRIVATE;
import static com.google.android.gms.internal.zzs.TAG;


public class SettingsFragment extends Fragment {

    private EditText edtUpdateInterval;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int updateInterval;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get sharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SkyWingPref",MODE_PRIVATE);
        updateInterval = (sharedPreferences.getInt("UpdateInterval",10));
        Log.d(TAG, "getting UpdateInterval: " + updateInterval);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        sharedPreferences = getActivity().getSharedPreferences("SkyWingPref",Context.MODE_PRIVATE);

        // Inflate the layout for this fragment
        return rootView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        edtUpdateInterval = (EditText) getView().findViewById(R.id.edtUpdateInterval);

        //Change EditText text and move text cursor to end of text
        edtUpdateInterval.setText(String.valueOf(updateInterval));
        edtUpdateInterval.setSelection(edtUpdateInterval.getText().length());

        edtUpdateInterval.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if(!hasFocus){

                    String updateIntervalString = edtUpdateInterval.getText().toString();
                    int updateIntervalValue = Integer.valueOf(updateIntervalString);


                    Log.d(TAG, "onFocusChange: Focus has been lost");

                    if(updateIntervalValue <5) updateIntervalValue=5; // Default value if interval is too low

                    Log.d(TAG, "onFocusChange: Saving update interval: " + updateIntervalValue);

                    //commit changes to sharedPreferences
                    editor = sharedPreferences.edit();
                    editor.putInt("UpdateInterval",updateIntervalValue);
                    editor.commit();
                }
            }
        });

        edtUpdateInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("onViewCreated: ", "onTextChanged");
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("onViewCreated: ", "afterTextChanged");
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onDetach() {
        super.onDetach();

    }
}
