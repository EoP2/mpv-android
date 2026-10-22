package `is`.xyz.mpv

import `is`.xyz.mpv.databinding.DialogDecimalBinding
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View

internal class DecimalPickerDialog(
    private val rangeMin: Double, private val rangeMax: Double
) : PickerDialog {
    private lateinit var binding: DialogDecimalBinding

    /**
     * Called whenever the value changes to a valid, in-range number (+/- buttons, typing, reset).
     * Assign this *after* setting the initial [number], so the initial value isn't reported back.
     */
    var onNumberChanged: ((Double) -> Unit)? = null

    override fun buildView(layoutInflater: LayoutInflater): View {
        binding = DialogDecimalBinding.inflate(layoutInflater)

        // hide extraneous UI parts
        arrayOf(binding.label1, binding.label2, binding.rowSecondary).forEach {
            it.visibility = View.GONE
        }

        binding.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val value = s!!.toString().toDoubleOrNull() ?: return
                val valueBounded = value.coerceIn(rangeMin, rangeMax)
                if (valueBounded != value)
                    binding.editText.setText(valueBounded.toString()) // re-enters with the bounded value
                else
                    onNumberChanged?.invoke(value)
            }
        })
        val onClick = { delta: Double ->
            val value = this.number ?: 0.0
            this.number = roundToMs(value + delta).coerceIn(rangeMin, rangeMax)
        }
        binding.btnMinus.setOnClickListener { onClick(-STEP) }
        binding.btnPlus.setOnClickListener { onClick(STEP) }

        return binding.root
    }

    override fun isInteger(): Boolean = false

    override var number: Double?
        set(v) = binding.editText.setText(v!!.toString())
        get() = binding.editText.text.toString().toDoubleOrNull()

    companion object {
        private const val STEP = 0.1

        // 0.1 can't be represented exactly, so repeated additions drift (0.1 + 0.2 = 0.30000000000000004).
        // Rounding to milliseconds keeps the displayed value clean.
        private fun roundToMs(v: Double) = Math.round(v * 1000.0) / 1000.0
    }
}
