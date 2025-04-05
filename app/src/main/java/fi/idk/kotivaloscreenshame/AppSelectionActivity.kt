package fi.idk.kotivaloscreenshame

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.edit

class AppSelectionActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppAdapter
    private lateinit var saveButton: Button
    private val selectedApps = mutableSetOf<String>() // Package names

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_selection)

        recyclerView = findViewById(R.id.recyclerView)
        saveButton = findViewById(R.id.saveButton)

        val prefs = getSharedPreferences("tracked_apps", MODE_PRIVATE)
        val savedPackages = prefs.getStringSet("selected_packages", emptySet()) ?: emptySet()

        val appHelper = AppHelper(this)
        val apps = appHelper.getApps()
            .map {
                AppItem(
                    packageName = it.packageName,
                    appName = appHelper.getAppName(it),
                    icon = appHelper.getAppIcon(it),
                    isChecked = savedPackages.contains(it.packageName)
                )
            }
            .sortedBy { it.appName.lowercase() }

        selectedApps.addAll(savedPackages)

        adapter = AppAdapter(apps) { app ->
            if (app.isChecked) selectedApps.add(app.packageName)
            else selectedApps.remove(app.packageName)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.setItemViewCacheSize(500);

        saveButton.setOnClickListener {
            saveSelectedApps()
            Toast.makeText(this, "There is now ${selectedApps.size} apps to be tracked by Mr. Kotivalo", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun saveSelectedApps() {
        val prefs = getSharedPreferences("tracked_apps", MODE_PRIVATE)
        prefs.edit() {
            // Save the selected apps (package names) to SharedPreferences
            val selectedPackages = selectedApps.toSet()  // Convert to a set to avoid duplicates
            putStringSet("selected_packages", selectedPackages)
            apply()
        }
    }
}