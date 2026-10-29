package com.xaillaclass.premium.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.xaillaclass.premium.R
import java.io.File

class LogFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_log, container, false)
        val tvLog = view.findViewById<TextView>(R.id.tvLog)
        
        tvLog.text = readLogs(requireContext())
        return view
    }

    private fun readLogs(context: Context): String {
        val logFile = File(context.filesDir, "xailla.log")
        return if (logFile.exists()) {
            logFile.readText()
        } else {
            "Belum ada log.\nTekan Connect untuk memulai VPN."
        }
    }
}
