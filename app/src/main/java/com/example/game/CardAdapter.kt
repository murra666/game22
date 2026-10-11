package com.example.game

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CardAdapter(
    private val cards: List<String>,
    private val opened: BooleanArray,
    private val matched: BooleanArray,
    private val onCardClick: (Int) -> Unit
) : RecyclerView.Adapter<CardAdapter.CardHolder>() {

    class CardHolder(val text: TextView) :
        RecyclerView.ViewHolder(text)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CardHolder {
        val text = TextView(parent.context)

        val size = parent.resources.displayMetrics.density

        val params = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            (parent.height / 6).coerceIn(
                (52 * size).toInt(),
                (100 * size).toInt()
            )
        )

        params.setMargins(
            (3 * size).toInt(),
            (3 * size).toInt(),
            (3 * size).toInt(),
            (3 * size).toInt()
        )

        text.layoutParams = params
        text.gravity = Gravity.CENTER
        text.textSize = 28f
        text.typeface = Typeface.DEFAULT_BOLD
        text.setTextColor(Color.WHITE)

        return CardHolder(text)
    }

    override fun onBindViewHolder(
        holder: CardHolder,
        position: Int
    ) {
        val visible = opened[position] || matched[position]
        val text = holder.text

        text.text = if (visible) cards[position] else "?"

        val background = GradientDrawable().apply {
            cornerRadius =
                12f * text.resources.displayMetrics.density

            setColor(
                when {
                    matched[position] -> Color.rgb(34, 139, 110)
                    visible -> Color.rgb(63, 79, 110)
                    else -> Color.rgb(48, 65, 91)
                }
            )
        }

        text.background = background
        text.alpha = if (matched[position]) 0.8f else 1f

        text.setOnClickListener {
            onCardClick(position)
        }
    }

    override fun getItemCount(): Int = cards.size
}
