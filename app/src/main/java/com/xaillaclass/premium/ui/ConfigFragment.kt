package com.xaillaclass.premium.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.xaillaclass.premium.R
import com.xaillaclass.premium.model.ServerConfig
import com.xaillaclass.premium.util.EncryptionHelper
import com.xaillaclass.premium.util.PrefsHelper
import com.xaillaclass.premium.v2ray.ConfigParser
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ConfigFragment : Fragment() {
    private lateinit var adapter: ConfigAdapter

    private val uploadLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { prosesUploadConfig(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_config, container, false)
        
        val etLink = view.findViewById<EditText>(R.id.etLink)
        val btnAdd = view.findViewById<Button>(R.id.btnAdd)
        val recycler = view.findViewById<RecyclerView>(R.id.recycler)
        
        // Toolbar: Upload .xailla (ikon upload di pojok kanan)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbarConfig)
        toolbar.inflateMenu(R.menu.menu_config)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_upload -> { openFilePicker(); true }
                else -> false
            }
        }

        adapter = ConfigAdapter(
            items = PrefsHelper.loadConfigs(requireContext()),
            onSelect = { config ->
                PrefsHelper.setSelected(requireContext(), config.id)
                Toast.makeText(requireContext(), "Dipilih: ${config.name}", Toast.LENGTH_SHORT).show()
            },
            onEdit = { config -> showEditDialog(config) },
            onCopy = { config -> copyConfigUrl(config) },
            onSave = { config -> saveSingleConfigToDownload(config) },
            onDelete = { config -> showDeleteDialog(config) }
        )
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        btnAdd.setOnClickListener {
            val link = etLink.text.toString().trim()
            if (link.isEmpty()) return@setOnClickListener
            val config = ConfigParser.parseLink(link)
            if (config != null) {
                val list = PrefsHelper.loadConfigs(requireContext())
                list.add(config)
                PrefsHelper.saveConfigs(requireContext(), list)
                adapter.updateData(list)
                etLink.setText("")
                Toast.makeText(requireContext(), "Config ditambahkan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Link tidak valid", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    // ============ EDIT DIALOG ============
    private fun showEditDialog(config: ServerConfig) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_config, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etEditName)
        val etAddress = dialogView.findViewById<TextInputEditText>(R.id.etEditAddress)
        val etPort = dialogView.findViewById<TextInputEditText>(R.id.etEditPort)
        etName.setText(config.name)
        etAddress.setText(config.address)
        etPort.setText(config.port.toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Config")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val newName = etName.text.toString().trim()
                val newAddress = etAddress.text.toString().trim()
                val newPort = etPort.text.toString().toIntOrNull() ?: config.port
                if (newName.isEmpty() || newAddress.isEmpty()) {
                    Toast.makeText(requireContext(), "Nama & address tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val list = PrefsHelper.loadConfigs(requireContext())
                val idx = list.indexOfFirst { it.id == config.id }
                if (idx != -1) {
                    list[idx] = config.copy(name = newName, address = newAddress, port = newPort)
                    PrefsHelper.saveConfigs(requireContext(), list)
                    adapter.updateData(list)
                    Toast.makeText(requireContext(), "Config diupdate", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // ============ COPY URL ============
    private fun copyConfigUrl(config: ServerConfig) {
        val url = if (config.protocol == "vmess" || config.protocol == "vless") {
            "${config.protocol}://${config.uuid}@${config.address}:${config.port}"
        } else {
            "${config.protocol}://${config.password}@${config.address}:${config.port}"
        }
        val cm = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("config", url))
        Toast.makeText(requireContext(), "URL disalin ke clipboard", Toast.LENGTH_SHORT).show()
    }

    // ============ SAVE 1 CONFIG KE DOWNLOAD ============
    private fun saveSingleConfigToDownload(config: ServerConfig) {
        try {
            val plainText = if (config.protocol == "vmess" || config.protocol == "vless") {
                "${config.protocol}://${config.uuid}@${config.address}:${config.port}"
            } else {
                "${config.protocol}://${config.password}@${config.address}:${config.port}"
            }
            val encrypted = EncryptionHelper.encrypt(plainText)

            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadDir.exists()) downloadDir.mkdirs()
            val safeName = config.name.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val file = File(downloadDir, "config_${safeName}.xailla")
            FileOutputStream(file).use { it.write(encrypted.toByteArray()) }
            Toast.makeText(requireContext(), "Tersimpan: Download/${file.name}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal save: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ============ HAPUS ============
    private fun showDeleteDialog(config: ServerConfig) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Config")
            .setMessage("Yakin hapus ${config.name}?")
            .setPositiveButton("Hapus") { _, _ ->
                val list = PrefsHelper.loadConfigs(requireContext())
                list.removeAll { it.id == config.id }
                PrefsHelper.saveConfigs(requireContext(), list)
                adapter.updateData(list)
                Toast.makeText(requireContext(), "Config dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // ============ UPLOAD .xailla ============
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        uploadLauncher.launch(Intent.createChooser(intent, "Pilih File .xailla"))
    }

    private fun prosesUploadConfig(uri: Uri) {
        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val fileContent = inputStream?.bufferedReader()?.use { it.readText() }
            if (fileContent.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "File kosong", Toast.LENGTH_SHORT).show()
                return
            }
            val decryptedText = EncryptionHelper.decrypt(fileContent)
            if (decryptedText.isEmpty()) {
                Toast.makeText(requireContext(), "File .xailla tidak valid", Toast.LENGTH_SHORT).show()
                return
            }
            var count = 0
            val list = PrefsHelper.loadConfigs(requireContext())
            decryptedText.lines().forEach { line ->
                val config = ConfigParser.parseLink(line.trim())
                if (config != null) { list.add(config); count++ }
            }
            if (count > 0) {
                PrefsHelper.saveConfigs(requireContext(), list)
                adapter.updateData(list)
                Toast.makeText(requireContext(), "Import $count config berhasil", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Format file tidak dikenali", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal upload: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
