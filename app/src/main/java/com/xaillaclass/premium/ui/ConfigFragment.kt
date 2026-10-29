package com.xaillaclass.premium.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.xaillaclass.premium.R
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
            val uri: Uri? = result.data?.data
            if (uri != null) {
                prosesUploadConfig(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_config, container, false)
        
        val etLink = view.findViewById<EditText>(R.id.etLink)
        val btnAdd = view.findViewById<Button>(R.id.btnAdd)
        val recycler = view.findViewById<RecyclerView>(R.id.recycler)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbarConfig)

        toolbar.inflateMenu(R.menu.menu_config)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_save -> { saveConfigToXaillaFile(); true }
                R.id.action_upload -> { openFilePicker(); true }
                else -> false
            }
        }

        adapter = ConfigAdapter(PrefsHelper.loadConfigs(requireContext())) { config ->
            PrefsHelper.setSelected(requireContext(), config.id)
            Toast.makeText(requireContext(), "Dipilih: ${config.name}", Toast.LENGTH_SHORT).show()
        }
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

    // FUNGSI SAVE: Menyimpan sebagai file .xailla (Hex AES)
    private fun saveConfigToXaillaFile() {
        try {
            val list = PrefsHelper.loadConfigs(requireContext())
            if (list.isEmpty()) {
                Toast.makeText(requireContext(), "Tidak ada config untuk disave", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Kumpulkan data asli
            val plainText = list.joinToString("\n") { 
                if (it.protocol == "vmess" || it.protocol == "vless") {
                    "${it.protocol}://${it.uuid}@${it.address}:${it.port}"
                } else {
                    "${it.protocol}://${it.password}@${it.address}:${it.port}"
                }
            }
            
            // Enkripsi dengan AES-256 (Hasilnya Hex acak)
            val encryptedText = EncryptionHelper.encrypt(plainText)
            
            // Simpan dengan ekstensi .xailla
            val file = File(requireContext().filesDir, "backup_config.xailla")
            FileOutputStream(file).use { it.write(encryptedText.toByteArray()) }
            
            Toast.makeText(requireContext(), "Tersimpan aman: ${file.name}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal save: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        uploadLauncher.launch(Intent.createChooser(intent, "Pilih File .xailla"))
    }

    // FUNGSI UPLOAD: Membaca file .xailla
    private fun prosesUploadConfig(uri: Uri) {
        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val fileContent = inputStream?.bufferedReader()?.use { it.readText() }
            
            if (fileContent.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "File kosong", Toast.LENGTH_SHORT).show()
                return
            }

            // Dekripsi AES-256
            val decryptedText = EncryptionHelper.decrypt(fileContent)
            
            if (decryptedText.isEmpty()) {
                Toast.makeText(requireContext(), "File .xailla tidak valid / rusak", Toast.LENGTH_SHORT).show()
                return
            }

            var count = 0
            val list = PrefsHelper.loadConfigs(requireContext())
            
            decryptedText.lines().forEach { line ->
                val config = ConfigParser.parseLink(line.trim())
                if (config != null) {
                    list.add(config)
                    count++
                }
            }
            
            if (count > 0) {
                PrefsHelper.saveConfigs(requireContext(), list)
                adapter.updateData(list)
                Toast.makeText(requireContext(), "Berhasil import $count config dari file .xailla", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Format file tidak dikenali", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal upload: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
