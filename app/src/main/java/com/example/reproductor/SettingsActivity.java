package com.example.reproductor;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings); // Usaremos un nuevo layout para esta Activity

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Habilita el botón de "volver atrás"
            getSupportActionBar().setTitle("Configuración"); // Establece el título de la toolbar
        }

        // Carga el fragmento de preferencias dentro de esta Activity
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Maneja el clic en el botón de "volver atrás" de la toolbar
        finish(); // Cierra esta Activity y regresa a la anterior (MainActivity)
        return true;
    }

    /**
     * Fragmento que contendrá las preferencias de la aplicación.
     */
    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            // Encontrar la preferencia del modo oscuro
            SwitchPreferenceCompat darkModePreference = findPreference("pref_dark_mode");

            if (darkModePreference != null) {
                darkModePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean isDarkMode = (Boolean) newValue;
                        if (isDarkMode) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        } else {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        }
                        // Recrear la actividad para que el cambio de tema se aplique de inmediato
                        requireActivity().recreate();
                        return true; // Indica que el cambio ha sido aceptado
                    }
                });
            }
        }
    }
}