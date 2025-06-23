package com.example.reproductor;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TabsPagerAdapter extends FragmentStateAdapter {

    public TabsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 0:
                return new SongsFragment();
            case 1:
                return new FoldersFragment();
            case 2:
                return new PlaylistsFragment();
            case 3:
                return new YouTubeFragment();
            default:

                return new SongsFragment();
        }
    }

    @Override
    public int getItemCount() {

        return 4;
    }
}