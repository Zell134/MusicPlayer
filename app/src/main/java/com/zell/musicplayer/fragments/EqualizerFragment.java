package com.zell.musicplayer.fragments;

import android.content.Context;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.zell.musicplayer.R;
import com.zell.musicplayer.services.PropertiesService;
import com.zell.musicplayer.models.Player;

import java.util.ArrayList;
import java.util.List;

public class EqualizerFragment extends Fragment {

    private Equalizer equalizer;
    private BassBoost bassBoost;
    private LinearLayout slidersContainer;
    private Context context;
    private Spinner presetsSpinner;
    private SeekBar bassBoostBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getActivity();
        return inflater.inflate(R.layout.fragment_equalizer, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        ImageButton backButton = ((AppCompatActivity)context).findViewById(R.id.back_button);
        backButton.setOnClickListener(view -> ((AppCompatActivity)context).onBackPressed());

        equalizer = Player.getInstance().getEqualizer();
        bassBoost = Player.getInstance().getBassBoost();
        if(equalizer!=null) {
            addSliders();
            fillSpinner();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        for (short i = 0; i < equalizer.getNumberOfBands(); i++) {
            PropertiesService.setEqualizerBand(context, i, String.valueOf(equalizer.getBandLevel(i)));
        }
        PropertiesService.setCurrentPreset(context, String.valueOf(presetsSpinner.getSelectedItemPosition()));
        if(bassBoostBar !=null) {
            PropertiesService.setBassBoostValue(context, String.valueOf(bassBoostBar.getProgress()));
        }
    }

    private void fillSpinner(){
        presetsSpinner = ((AppCompatActivity)context).findViewById(R.id.preset_list);
        List<String> list = new ArrayList<>();
        list.add(getResources().getString(R.string.custom_eualizer));
        for (int i = 0; i < equalizer.getNumberOfPresets(); i++) {
            list.add(equalizer.getPresetName((short) i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, list);
        presetsSpinner.setAdapter(adapter);
        String preset = PropertiesService.getCurrentPreset(context);
        if(preset != null) {
            presetsSpinner.setSelection(Integer.parseInt(preset));
        }
        presetsSpinner.setOnItemSelectedListener(spinnerItemSelectedListener);
    }

    private void addSliders(){
        int slidersCount = equalizer.getNumberOfBands();
        short minEQLevel = equalizer.getBandLevelRange()[0];
        short maxEQLevel = equalizer.getBandLevelRange()[1];
        slidersContainer = ((AppCompatActivity)context).findViewById(R.id.sliders_container);

        for (short i = 0; i < slidersCount; i++) {
            TextView freqTextView = new TextView(context);
            freqTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            freqTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            freqTextView.setText((equalizer.getCenterFreq( i) / 1000) + " Hz");
            slidersContainer.addView(freqTextView);

            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(10,0,10,0);

            TextView minEqLevelTextView = new TextView(context);
            minEqLevelTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            minEqLevelTextView.setText((minEQLevel / 100) + " dB");

            TextView maxEqLevelTextView = new TextView(context);
            maxEqLevelTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            maxEqLevelTextView.setText((maxEQLevel / 100) + " dB");

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;
            layoutParams.setMargins(0, 0, 0, 10);
            SeekBar bar = new SeekBar(context);
            bar.setLayoutParams(layoutParams);
            bar.setMax(maxEQLevel - minEQLevel);
            bar.setTag(i);
            bar.setProgress(equalizer.getBandLevel(i) - minEQLevel);
            bar.setOnSeekBarChangeListener(seekBarChangeListener);

            row.addView(minEqLevelTextView);
            row.addView(bar);
            row.addView(maxEqLevelTextView);
            slidersContainer.addView(row);
        }


            TextView textView = new TextView(context);
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setText(getResources().getString(R.string.bass_bost));
            slidersContainer.addView(textView);

            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(10, 0, 10, 0);

            TextView minEqLevelTextView = new TextView(context);
            minEqLevelTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            minEqLevelTextView.setText("0");

            TextView maxEqLevelTextView = new TextView(context);
            maxEqLevelTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            maxEqLevelTextView.setText("1000");

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;
            layoutParams.setMargins(0, 0, 0, 10);
            bassBoostBar = new SeekBar(context);
            bassBoostBar.setLayoutParams(layoutParams);
            bassBoostBar.setMax(1000);
            if(bassBoostBar != null) {
                String value = PropertiesService.getBassBoostValue(context);
                if (value != null) {
                    bassBoostBar.setProgress(Integer.parseInt(value));
                }
            }
            bassBoostBar.setOnSeekBarChangeListener(bassBoostChangeListener);

            row.addView(minEqLevelTextView);
            row.addView(bassBoostBar);
            row.addView(maxEqLevelTextView);
            slidersContainer.addView(row);
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            short band = Short.parseShort(seekBar.getTag().toString());
            short value = (short) (seekBar.getProgress() + equalizer.getBandLevelRange()[0]);
            equalizer.setBandLevel(band, value);
            presetsSpinner.setSelection(0);
        }
    };

    SeekBar.OnSeekBarChangeListener bassBoostChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if(bassBoost.getStrengthSupported()) {
                bassBoost.setStrength((short) seekBar.getProgress());
            }else{
                Toast.makeText(context, getResources().getString(R.string.bass_boost_not_supported_message), Toast.LENGTH_SHORT).show();
            }
        }
    };

    AdapterView.OnItemSelectedListener spinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if(i > 0){
                short preset = (short)(i - 1);
                equalizer.usePreset(preset);
                for(short j = 0; j < equalizer.getNumberOfBands(); j ++) {
                    SeekBar bar = slidersContainer.findViewWithTag(j);
                    bar.setProgress(equalizer.getBandLevel(j) - equalizer.getBandLevelRange()[0]);
                }
                presetsSpinner.setSelection(i);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

}