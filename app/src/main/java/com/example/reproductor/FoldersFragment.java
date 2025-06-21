package com.example.reproductor;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File; // Ya no es tan crítico aquí, pero déjalo si lo usas en otro sitio
import java.util.ArrayList;
import java.util.Collections; // Ya no es tan crítico aquí
import java.util.HashMap; // Ya no es tan crítico aquí

public class FoldersFragment extends Fragment {

    // private ArrayList<Folder> folderList = new ArrayList<>(); // Esta lista ya no se carga aquí
    private ListView folderListView; // Hacerla variable de clase
    private FolderAdapter adapter;
    private OnFolderSelectedListener mListener;

    public interface OnFolderSelectedListener {
        void onFolderSelected(String folderPath);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFolderSelectedListener) {
            mListener = (OnFolderSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFolderSelectedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folders, container, false);

        folderListView = view.findViewById(R.id.folderListView); // Inicializar aquí

        // Obtener la lista de carpetas ya procesada desde MainActivity
        // ¡IMPORTANTE! MainActivity debe tener un método getDisplayableFoldersList() que devuelve la lista
        ArrayList<Folder> folders = ((MainActivity) requireActivity()).getDisplayableFoldersList();

        adapter = new FolderAdapter(getContext(), folders); // Usar la lista obtenida de MainActivity
        folderListView.setAdapter(adapter);

        folderListView.setOnItemClickListener((parent, view1, position, id) -> {
            String path = folders.get(position).getPath(); // Acceder a la lista que alimenta el adapter
            mListener.onFolderSelected(path); // Notificamos a la Activity
        });

        // La lógica de loadFolders() se mueve a MainActivity
        // loadFolders(); // REMOVER esta llamada de aquí

        return view;
    }

    // Método para que MainActivity pueda notificar al adaptador de cambios
    public void notifyAdapterChange() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            // ¡NUEVO! Forzar el redibujado del ListView
            folderListView.invalidateViews();
        }
    }

    // Getter para acceder al ListView desde la actividad
    public ListView getFolderListView() {
        return folderListView;
    }

    // REMOVER ESTE MÉTODO COMPLETO de FoldersFragment
    // private void loadFolders() { ... }
}