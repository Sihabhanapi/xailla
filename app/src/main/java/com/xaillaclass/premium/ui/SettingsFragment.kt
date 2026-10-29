package com.xaillaclass.premium.ui
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.xaillaclass.premium.R
class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        view.findViewById<TextView>(R.id.tvVersion).text = "XaillaClass Premium v${com.xaillaclass.premium.BuildConfig.VERSION_NAME}"
        return view
    }
}
