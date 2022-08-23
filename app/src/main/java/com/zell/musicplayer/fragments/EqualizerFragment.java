package com.zell.musicplayer.fragments;

import android.content.Context;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.NoiseSuppressor;
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
import android.widget.Switch;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.zell.musicplayer.R;
import com.zell.musicplayer.models.Player;

import java.util.ArrayList;
import java.util.List;

public class EqualizerFragment extends Fragment {

    private Equalizer equalizer;
    private BassBoost bassBoost;
    private LinearLayout slidersContainer;
    private AcousticEchoCanceler echoCanceler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_equalizer, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        ImageButton backButton = getActivity().findViewById(R.id.back_button);
        backButton.setOnClickListener(view -> getActivity().onBackPressed());

        Context context = getActivity();
        equalizer = Player.getInstance().getEqualizer();
        bassBoost = Player.getInstance().getBassBoost();
        if(equalizer!=null) {
            fillSpinner(context);
            addSliders(context);
        }
    }

    private void fillSpinner(Context context){
        Spinner spinner = getActivity().findViewById(R.id.preset_list);
        List<String> list = new ArrayList<>();
        list.add(getResources().getString(R.string.custom_eualizer));
        for (int i = 0; i < equalizer.getNumberOfPresets(); i++) {
            list.add(equalizer.getPresetName((short) i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, list);
        spinner.setAdapter(adapter);
        spinner.setSelection(Player.getInstance().getCurrentPreset() + 1);
        spinner.setOnItemSelectedListener(spinnerItemSelectedListener);
    }

    private void addSliders(Context context){
        int slidersCount = equalizer.getNumberOfBands();
        short minEQLevel = equalizer.getBandLevelRange()[0];
        short maxEQLevel = equalizer.getBandLevelRange()[1];
        slidersContainer = getActivity().findViewById(R.id.sliders_container);

        for (int i = 0; i < slidersCount; i++) {
            TextView freqTextView = new TextView(context);
            freqTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            freqTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            freqTextView.setText((equalizer.getCenterFreq((short) i) / 1000) + " Hz");
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
            bar.setProgress(equalizer.getBandLevel((short) i));
            bar.setOnSeekBarChangeListener(seekBarChangeListener);

            row.addView(minEqLevelTextView);
            row.addView(bar);
            row.addView(maxEqLevelTextView);
            slidersContainer.addView(row);
        }

        if(bassBoost.getStrengthSupported()) {
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
            SeekBar bar = new SeekBar(context);
            bar.setLayoutParams(layoutParams);
            bar.setMax(1000);
            bar.setProgress(Player.getInstance().getCurrentBassBoostStrength());
            bar.setOnSeekBarChangeListener(bassBoostChangeListener);

            row.addView(minEqLevelTextView);
            row.addView(bar);
            row.addView(maxEqLevelTextView);
            slidersContainer.addView(row);
        }

        echoCanceler = Player.getInstance().getEchoCanceler();
        if(AcousticEchoCanceler.isAvailable() && echoCanceler != null) {
            LinearLayout echoCancelerLayout = new LinearLayout(context);
            echoCancelerLayout.setOrientation(LinearLayout.HORIZONTAL);
            echoCancelerLayout.setPadding(0,0,10,0);

            Switch echoCancelerSwitch = new Switch(context);
            echoCancelerSwitch.setOnClickListener(view -> echoCanceler.setEnabled(echoCancelerSwitch.isEnabled()));
            TextView echoCancelerTextView = new TextView(context);
            echoCancelerTextView.setText(getResources().getString(R.string.echo_canceler));
            echoCancelerLayout.addView(echoCancelerTextView);
            echoCancelerLayout.addView(echoCancelerSwitch);
            slidersContainer.addView(echoCancelerLayout);
        }

        NoiseSuppressor noiseSuppressor = Player.getInstance().getNoiseSuppressor();
        if(NoiseSuppressor.isAvailable() && noiseSuppressor != null) {
            LinearLayout noiseSuppressorLayout = new LinearLayout(context);
            noiseSuppressorLayout.setOrientation(LinearLayout.HORIZONTAL);
            noiseSuppressorLayout.setPadding(0,0,10,0);

            Switch noiseSuppressorSwitch = new Switch(context);
            noiseSuppressorSwitch.setOnClickListener(view -> echoCanceler.setEnabled(noiseSuppressorSwitch.isEnabled()));
            TextView noiseSuppressorTextView = new TextView(context);
            noiseSuppressorTextView.setText(getResources().getString(R.string.noise_suppressor));
            noiseSuppressorLayout.addView(noiseSuppressorTextView);
            noiseSuppressorLayout.addView(noiseSuppressorSwitch);
            slidersContainer.addView(noiseSuppressorLayout);
        }
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            short band = Short.parseShort(seekBar.getTag().toString());
            equalizer.setBandLevel(band, (short) (i + equalizer.getBandLevelRange()[0]));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    SeekBar.OnSeekBarChangeListener bassBoostChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            bassBoost.setStrength((short) i);
            Player.getInstance().setCurrentBassBoostStrength((short)i);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    AdapterView.OnItemSelectedListener spinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if(i > 0){
                short preset = (short)(i - 1);
                equalizer.usePreset(preset);
                Player.getInstance().setCurrentPreset(preset);
                for(int j = 0; j < equalizer.getNumberOfBands(); j ++){
                    SeekBar bar = slidersContainer.findViewWithTag(j);
                    bar.setProgress(equalizer.getBandLevel((short) (j)) - equalizer.getBandLevelRange()[0]);
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

}