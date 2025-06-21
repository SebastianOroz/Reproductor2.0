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
        // Devuelve el fragmento correspondiente a la posición de la pestaña
        switch (position) {
            case 0:
                return new SongsFragment();
            case 1:
                return new FoldersFragment();
            case 2:
                return new SongsFragment();
            case 3:
                return new YouTubeFragment(); // Se añade el fragmento de YouTube
            default:
                // Como caso por defecto, se puede devolver el primer fragmento
                return new SongsFragment();
        }
    }

    @Override
    public int getItemCount() {
        // El número total de pestañas ahora es 4
        return 4;
    }
}