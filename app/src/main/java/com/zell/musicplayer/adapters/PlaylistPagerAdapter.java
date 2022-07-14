package com.zell.musicplayer.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.zell.musicplayer.fragments.AllLibraryFragment;
import com.zell.musicplayer.fragments.ArtistsFragment;
import com.zell.musicplayer.fragments.BaseFragment;

import java.util.List;

public class PlaylistPagerAdapter extends FragmentStateAdapter {

    private List<BaseFragment> fragments = List.of(new AllLibraryFragment(), new ArtistsFragment());

    public PlaylistPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

}
