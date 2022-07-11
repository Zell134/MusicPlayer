package com.zell.musicplayer.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.zell.musicplayer.fragments.AllLibraryFragment;
import com.zell.musicplayer.fragments.ArtistsFragment;

public class PlaylistPagerAdapter extends FragmentStateAdapter {

    public PlaylistPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new AllLibraryFragment();
            case 1:
                return new ArtistsFragment();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
