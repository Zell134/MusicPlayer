package com.zell.musicplayer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.zell.musicplayer.R;
import com.zell.musicplayer.adapters.PlaylistPagerAdapter;

public class MainFragment extends Fragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        PlaylistPagerAdapter pagerAdapter = new PlaylistPagerAdapter(this);
        ViewPager2 viewPager = view.findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        TabLayout tabs = getActivity().findViewById(R.id.tabs);
        setlibraryTab(viewPager, tabs);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    private void setlibraryTab(ViewPager2 viewPager, TabLayout tabs){

        new TabLayoutMediator(tabs, viewPager, (tab, position)->{
            switch(position){
                case 0:
                    tab.setText(getActivity().getResources().getString(R.string.all_media_library));
                    break;
                case 1:
                    tab.setText(getActivity().getResources().getString(R.string.artists));
                    break;
            }
        }).attach();
    }
}
