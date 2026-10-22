package `is`.xyz.mpv

import `is`.xyz.mpv.databinding.DialogDecimalBinding
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.core.view.isVisible

internal class SubDelayDialog(
    private val rangeMin: Double, private val rangeMax: Double
) {
    private lateinit var binding: DialogDecimalBinding

    /**
     * Called whenever the primary / secondary delay changes to a valid, in-range number.
     * Assign these *after* setting the initial [delay1] / [delay2], so the initial values aren't reported back.
     */
    var onDelay1Changed: ((Double) -> Unit)? = null
    var onDelay2Changed: ((Double) -> Unit)? = null

    fun buildView(layoutInflater: LayoutInflater): View {
        binding = DialogDecimalBinding.inflate(layoutInflater)

        binding.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) =
                handleTextChange(s?.toString(), binding.editText) { onDelay1Changed?.invoke(it) }
        })
        binding.editText2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) =
                handleTextChange(s?.toString(), binding.editText2) { onDelay2Changed?.invoke(it) }
        })
        val onClick1 = { delta: Double ->
            val value = this.delay1 ?: 0.0
            this.delay1 = roundToMs(value + delta).coerceIn(rangeMin, rangeMax)
        }
        val onClick2 = { delta: Double ->
            val value = this.delay2 ?: 0.0
            this.delay2 = roundToMs(value + delta).coerceIn(rangeMin, rangeMax)
        }
        binding.btnMinus.setOnClickListener { onClick1(-STEP) }
        binding.btnPlus.setOnClickListener { onClick1(STEP) }
        binding.btnMinus2.setOnClickListener { onClick2(-STEP) }
        binding.btnPlus2.setOnClickListener { onClick2(STEP) }

        return binding.root
    }

    private fun handleTextChange(s: String?, editText: EditText, onValid: (Double) -> Unit) {
        val value = s?.toDoubleOrNull() ?: return
        val valueBounded = value.coerceIn(rangeMin, rangeMax)
        if (valueBounded != value)
            editText.setText(valueBounded.toString()) // re-enters with the bounded value
        else
            onValid(value)
    }

    /** Primary sub delay */
    var delay1: Double?
        set(v) = binding.editText.setText(v!!.toString())
        get() = binding.editText.text.toString().toDoubleOrNull()

    /**
     * Secondary sub delay. Set to null to hide related UI parts.
     */
    var delay2: Double?
        set(v) {
            arrayOf(binding.label1, binding.label2, binding.rowSecondary).forEach {
                it.isVisible = v != null
            }
            if (v != null)
                binding.editText2.setText(v.toString())
        }
        get() = binding.editText2.text.toString().toDoubleOrNull()

    companion object {
        private const val STEP = 0.1

        // 0.1 can't be represented exactly, so repeated additions drift (0.1 + 0.2 = 0.30000000000000004).
        // Rounding to milliseconds keeps the displayed value clean.
        private fun roundToMs(v: Double) = Math.round(v * 1000.0) / 1000.0
    }
}
