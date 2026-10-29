package com.xaillaclass.premium.ui
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.xaillaclass.premium.R
import java.text.SimpleDateFormat
import java.util.*
class LogFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_log, container, false)
        val now = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        view.findViewById<TextView>(R.id.tvLog).text = "[$now] XaillaClass Premium started.\n[$now] Waiting for connection...\n"
        return view
    }
}
