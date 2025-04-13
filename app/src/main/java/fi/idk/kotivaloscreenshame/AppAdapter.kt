package fi.idk.kotivaloscreenshame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(
    private val apps: List<AppItem>,
    private val onAppSelected: (AppItem) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {
    inner class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.appIcon)
        private val name: TextView = view.findViewById(R.id.appName)
        private val checkBox: CheckBox = view.findViewById(R.id.appCheckBox)

        fun bind(app: AppItem) {
            name.text = app.appName
            icon.setImageDrawable(app.icon)
            checkBox.isChecked = app.isChecked

            // When checkbox is clicked, update the state in the model
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                app.isChecked = isChecked
                onAppSelected(app)  // Notify the selection change (add to selectedApps)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.app_item, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.bind(app)
    }

    override fun getItemCount(): Int = apps.size
}