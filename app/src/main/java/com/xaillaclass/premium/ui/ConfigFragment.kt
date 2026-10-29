package com.xaillaclass.premium.ui
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.xaillaclass.premium.R
import com.xaillaclass.premium.util.PrefsHelper
import com.xaillaclass.premium.v2ray.ConfigParser
class ConfigFragment : Fragment() {
    private lateinit var adapter: ConfigAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_config, container, false)
        val etLink = view.findViewById<TextInputEditText>(R.id.etLink)
        val recycler = view.findViewById<RecyclerView>(R.id.recycler)
        adapter = ConfigAdapter(PrefsHelper.loadConfigs(requireContext())) { config ->
            PrefsHelper.setSelected(requireContext(), config.id)
            Toast.makeText(requireContext(), "Dipilih: ${config.name}", Toast.LENGTH_SHORT).show()
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter
        view.findViewById<View>(R.id.btnAdd).setOnClickListener {
            val link = etLink.text.toString().trim()
            if (link.isEmpty()) return@setOnClickListener
            val config = ConfigParser.parseLink(link)
            if (config != null) {
                val list = PrefsHelper.loadConfigs(requireContext())
                list.add(config)
                PrefsHelper.saveConfigs(requireContext(), list)
                adapter.updateData(list); etLink.setText("")
                Toast.makeText(requireContext(), "Config ditambahkan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Link tidak valid", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }
}
