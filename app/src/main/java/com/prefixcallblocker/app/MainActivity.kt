package com.prefixcallblocker.app

import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

/**
 * The only user-facing screen, and the only part of the app that runs when the
 * user opens it. Lets the user grant the call-screening role and manage the list
 * of blocked prefixes.
 *
 * Deliberately a plain [Activity] (not AppCompatActivity) with a framework
 * Material theme, so the app needs no AndroidX/appcompat dependency (SPEC §3).
 */
class MainActivity : Activity() {

    private lateinit var store: PrefixStore
    private lateinit var statusText: TextView
    private lateinit var enableButton: Button
    private lateinit var prefixInput: EditText
    private lateinit var adapter: PrefixAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        store = PrefixStore(this)
        statusText = findViewById(R.id.status_text)
        enableButton = findViewById(R.id.enable_button)
        prefixInput = findViewById(R.id.prefix_input)
        val addButton: Button = findViewById(R.id.add_button)
        val list: ListView = findViewById(R.id.prefix_list)

        adapter = PrefixAdapter()
        list.adapter = adapter

        enableButton.setOnClickListener { requestScreeningRole() }
        addButton.setOnClickListener { onAddClicked() }
    }

    override fun onResume() {
        super.onResume()
        // Refresh both role status and list every time the screen is shown —
        // the user may have changed the role in system settings.
        refreshStatus()
        refreshList()
    }

    private fun onAddClicked() {
        val text = prefixInput.text?.toString().orEmpty()
        if (text.isBlank()) return
        val added = store.addPrefix(text)
        prefixInput.setText("")
        if (!added) {
            Toast.makeText(this, R.string.prefix_not_added, Toast.LENGTH_SHORT).show()
        }
        refreshList()
    }

    private fun refreshList() {
        adapter.setItems(store.getPrefixes().sorted())
    }

    private fun refreshStatus() {
        val held = isScreeningRoleHeld()
        statusText.setText(if (held) R.string.status_active else R.string.status_inactive)
        enableButton.isEnabled = !held
    }

    private fun isScreeningRoleHeld(): Boolean {
        val rm = getSystemService(RoleManager::class.java) ?: return false
        return rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) &&
            rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    private fun requestScreeningRole() {
        val rm = getSystemService(RoleManager::class.java)
        if (rm == null || !rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
            Toast.makeText(this, R.string.role_unavailable, Toast.LENGTH_LONG).show()
            return
        }
        if (rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
            refreshStatus()
            return
        }
        val intent = rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
        // Classic result API keeps us off the androidx.activity dependency.
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQ_SCREENING_ROLE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_SCREENING_ROLE) {
            refreshStatus()
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, R.string.role_denied, Toast.LENGTH_LONG).show()
            }
        }
    }

    /** Minimal list adapter: one row = prefix label + a delete button. */
    private inner class PrefixAdapter : BaseAdapter() {
        private var items: List<String> = emptyList()

        fun setItems(newItems: List<String>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun getCount(): Int = items.size
        override fun getItem(position: Int): String = items[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView
                ?: layoutInflater.inflate(R.layout.row_prefix, parent, false)
            val prefix = items[position]
            view.findViewById<TextView>(R.id.prefix_label).text = prefix
            view.findViewById<Button>(R.id.delete_button).setOnClickListener {
                store.removePrefix(prefix)
                refreshList()
            }
            return view
        }
    }

    companion object {
        private const val REQ_SCREENING_ROLE = 1001
    }
}
