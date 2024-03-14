package com.example.clockcustomview

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.clockview.ClockView
import kotlin.random.Random

class UsageExampleFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_usage_example, container, false)
        val clock = view.findViewById<ClockView>(R.id.secondFragmentClock)
        val button = view.findViewById<Button>(R.id.hideClockButton)
        button.setOnClickListener {
            toggleClockColor(clock)
        }
        return view
    }

    private fun toggleClockColor(clockView: ClockView) {
        clockView.clockColors.backColor =
            Color.rgb(Random.nextInt(), Random.nextInt(), Random.nextInt())
    }
}