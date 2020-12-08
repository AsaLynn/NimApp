package com.zxn.netease.nimsdk.common.ui.recyclerview.holder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.util.Linkify
import android.util.SparseArray
import android.view.View
import android.view.View.OnLongClickListener
import android.view.View.OnTouchListener
import android.view.animation.AlphaAnimation
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * ViewHolder基类.
 */
open class BaseViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {

    /**
     * Views indexed with their IDs
     */
    private val views: SparseArray<View?> = SparseArray()
    private val childClickViewIds: LinkedHashSet<Int> = LinkedHashSet()
    private val itemChildLongClickViewIds: LinkedHashSet<Int> = LinkedHashSet()
    var convertView: View? = view
    /**
     * Retrieves the last converted object on this view.
     */
    /**
     * Should be called during convert
     */
    /**
     * Package private field to retain the associated user object and detect a change
     */
    var associatedObject: Any? = null
    fun getItemChildLongClickViewIds(): HashSet<Int> {
        return itemChildLongClickViewIds
    }

    fun getChildClickViewIds(): HashSet<Int> {
        return childClickViewIds
    }

    /**
     * Will set the text of a TextView.
     *
     * @param viewId The view id.
     * @param value  The text to put in the text view.
     * @return The BaseViewHolder for chaining.
     */
    fun setText(viewId: Int, value: CharSequence?): BaseViewHolder {
        val view = getView<TextView>(viewId)!!
        view.text = value
        return this
    }

    fun setText(viewId: Int, @StringRes strId: Int): BaseViewHolder {
        val view = getView<TextView>(viewId)!!
        view.setText(strId)
        return this
    }

    /**
     * Will set the image of an ImageView from a resource id.
     *
     * @param viewId     The view id.
     * @param imageResId The image resource id.
     * @return The BaseViewHolder for chaining.
     */
    fun setImageResource(viewId: Int, @DrawableRes imageResId: Int): BaseViewHolder {
        val view = getView<ImageView>(viewId)!!
        view.setImageResource(imageResId)
        return this
    }

    /**
     * Will set background color of a view.
     *
     * @param viewId The view id.
     * @param color  A color, not a resource id.
     * @return The BaseViewHolder for chaining.
     */
    fun setBackgroundColor(viewId: Int, color: Int): BaseViewHolder {
        val view = getView<View>(viewId)!!
        view.setBackgroundColor(color)
        return this
    }

    /**
     * Will set background of a view.
     *
     * @param viewId        The view id.
     * @param backgroundRes A resource to use as a background.
     * @return The BaseViewHolder for chaining.
     */
    fun setBackgroundRes(viewId: Int, @DrawableRes backgroundRes: Int): BaseViewHolder {
        val view = getView<View>(viewId)!!
        view.setBackgroundResource(backgroundRes)
        return this
    }

    /**
     * Will set text color of a TextView.
     *
     * @param viewId    The view id.
     * @param textColor The text color (not a resource id).
     * @return The BaseViewHolder for chaining.
     */
    fun setTextColor(viewId: Int, textColor: Int): BaseViewHolder {
        val view = getView<TextView>(viewId)!!
        view.setTextColor(textColor)
        return this
    }

    /**
     * Will set the image of an ImageView from a drawable.
     *
     * @param viewId   The view id.
     * @param drawable The image drawable.
     * @return The BaseViewHolder for chaining.
     */
    fun setImageDrawable(viewId: Int, drawable: Drawable?): BaseViewHolder {
        val view = getView<ImageView>(viewId)!!
        view.setImageDrawable(drawable)
        return this
    }

    /**
     * Add an action to set the image of an image view. Can be called multiple times.
     */
    fun setImageBitmap(viewId: Int, bitmap: Bitmap?): BaseViewHolder {
        val view = getView<ImageView>(viewId)!!
        view.setImageBitmap(bitmap)
        return this
    }

    /**
     * Add an action to set the alpha of a view. Can be called multiple times.
     * Alpha between 0-1.
     */
    fun setAlpha(viewId: Int, value: Float): BaseViewHolder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getView<View>(viewId)!!.alpha = value
        } else {
            // Pre-honeycomb hack to set Alpha value
            val alpha = AlphaAnimation(value, value)
            alpha.duration = 0
            alpha.fillAfter = true
            getView<View>(viewId)!!.startAnimation(alpha)
        }
        return this
    }

    /**
     * Set a view visibility to VISIBLE (true) or GONE (false).
     *
     * @param viewId  The view id.
     * @param visible True for VISIBLE, false for GONE.
     * @return The BaseViewHolder for chaining.
     */
    fun setVisible(viewId: Int, visible: Boolean): BaseViewHolder {
        val view = getView<View>(viewId)!!
        view.visibility = if (visible) View.VISIBLE else View.GONE
        return this
    }

    /**
     * Add links into a TextView.
     *
     * @param viewId The id of the TextView to linkify.
     * @return The BaseViewHolder for chaining.
     */
    fun linkify(viewId: Int): BaseViewHolder {
        val view = getView<TextView>(viewId)!!
        Linkify.addLinks(view, Linkify.ALL)
        return this
    }

    /**
     * Apply the typeface to the given viewId, and enable subpixel rendering.
     */
    fun setTypeface(viewId: Int, typeface: Typeface?): BaseViewHolder {
        val view = getView<TextView>(viewId)!!
        view.typeface = typeface
        view.paintFlags = view.paintFlags or Paint.SUBPIXEL_TEXT_FLAG
        return this
    }

    /**
     * Apply the typeface to all the given viewIds, and enable subpixel rendering.
     */
    fun setTypeface(typeface: Typeface?, vararg viewIds: Int): BaseViewHolder {
        for (viewId in viewIds) {
            val view = getView<TextView>(viewId)!!
            view.typeface = typeface
            view.paintFlags = view.paintFlags or Paint.SUBPIXEL_TEXT_FLAG
        }
        return this
    }

    /**
     * Sets the progress of a ProgressBar.
     *
     * @param viewId   The view id.
     * @param progress The progress.
     * @return The BaseViewHolder for chaining.
     */
    fun setProgress(viewId: Int, progress: Int): BaseViewHolder {
        val view = getView<ProgressBar>(viewId)!!
        view.progress = progress
        return this
    }

    /**
     * Sets the progress and max of a ProgressBar.
     *
     * @param viewId   The view id.
     * @param progress The progress.
     * @param max      The max value of a ProgressBar.
     * @return The BaseViewHolder for chaining.
     */
    fun setProgress(viewId: Int, progress: Int, max: Int): BaseViewHolder {
        val view = getView<ProgressBar>(viewId)!!
        view.max = max
        view.progress = progress
        return this
    }

    /**
     * Sets the range of a ProgressBar to 0...max.
     *
     * @param viewId The view id.
     * @param max    The max value of a ProgressBar.
     * @return The BaseViewHolder for chaining.
     */
    fun setMax(viewId: Int, max: Int): BaseViewHolder {
        val view = getView<ProgressBar>(viewId)!!
        view.max = max
        return this
    }

    /**
     * Sets the rating (the number of stars filled) of a RatingBar.
     *
     * @param viewId The view id.
     * @param rating The rating.
     * @return The BaseViewHolder for chaining.
     */
    fun setRating(viewId: Int, rating: Float): BaseViewHolder {
        val view = getView<RatingBar>(viewId)!!
        view.rating = rating
        return this
    }

    /**
     * Sets the rating (the number of stars filled) and max of a RatingBar.
     *
     * @param viewId The view id.
     * @param rating The rating.
     * @param max    The range of the RatingBar to 0...max.
     * @return The BaseViewHolder for chaining.
     */
    fun setRating(viewId: Int, rating: Float, max: Int): BaseViewHolder {
        val view = getView<RatingBar>(viewId)!!
        view.max = max
        view.rating = rating
        return this
    }

    /**
     * Sets the on click listener of the view.
     *
     * @param viewId   The view id.
     * @param listener The on click listener;
     * @return The BaseViewHolder for chaining.
     */
    @Deprecated("")
    fun setOnClickListener(viewId: Int, listener: View.OnClickListener?): BaseViewHolder {
        val view = getView<View>(viewId)!!
        view.setOnClickListener(listener)
        return this
    }

    /**
     * add childView id
     *
     * @param viewId add the child view id   can support childview click
     * @return
     */
    fun addOnClickListener(viewId: Int): BaseViewHolder {
        childClickViewIds.add(viewId)
        return this
    }

    /**
     * add long click view id
     *
     * @param viewId
     * @return
     */
    fun addOnLongClickListener(viewId: Int): BaseViewHolder {
        itemChildLongClickViewIds.add(viewId)
        return this
    }

    /**
     * Sets the on touch listener of the view.
     *
     * @param viewId   The view id.
     * @param listener The on touch listener;
     * @return The BaseViewHolder for chaining.
     */
    fun setOnTouchListener(viewId: Int, listener: OnTouchListener?): BaseViewHolder {
        val view = getView<View>(viewId)!!
        view.setOnTouchListener(listener)
        return this
    }

    /**
     * Sets the on long click listener of the view.
     *
     * @param viewId   The view id.
     * @param listener The on long click listener;
     * @return The BaseViewHolder for chaining.
     */
    fun setOnLongClickListener(viewId: Int, listener: OnLongClickListener?): BaseViewHolder {
        val view = getView<View>(viewId)!!
        view.setOnLongClickListener(listener)
        return this
    }

    /**
     * Sets the listview or gridview's item click listener of the view
     *
     * @param viewId   The view id.
     * @param listener The item on click listener;
     * @return The BaseViewHolder for chaining.
     */
    fun setOnItemClickListener(
        viewId: Int,
        listener: AdapterView.OnItemClickListener?
    ): BaseViewHolder {
        val view = getView<AdapterView<*>>(viewId)!!
        view.onItemClickListener = listener
        return this
    }

    /**
     * Sets the listview or gridview's item long click listener of the view
     *
     * @param viewId   The view id.
     * @param listener The item long click listener;
     * @return The BaseViewHolder for chaining.
     */
    fun setOnItemLongClickListener(
        viewId: Int,
        listener: AdapterView.OnItemLongClickListener?
    ): BaseViewHolder {
        val view = getView<AdapterView<*>>(viewId)!!
        view.onItemLongClickListener = listener
        return this
    }

    /**
     * Sets the listview or gridview's item selected click listener of the view
     *
     * @param viewId   The view id.
     * @param listener The item selected click listener;
     * @return The BaseViewHolder for chaining.
     */
    fun setOnItemSelectedClickListener(
        viewId: Int,
        listener: OnItemSelectedListener?
    ): BaseViewHolder {
        val view = getView<AdapterView<*>>(viewId)!!
        view.onItemSelectedListener = listener
        return this
    }

    /**
     * Sets the on checked change listener of the view.
     *
     * @param viewId   The view id.
     * @param listener The checked change listener of compound button.
     * @return The BaseViewHolder for chaining.
     */
    fun setOnCheckedChangeListener(
        viewId: Int,
        listener: CompoundButton.OnCheckedChangeListener?
    ): BaseViewHolder {
        val view = getView<CompoundButton>(viewId)!!
        view.setOnCheckedChangeListener(listener)
        return this
    }

    /**
     * Sets the tag of the view.
     *
     * @param viewId The view id.
     * @param tag    The tag;
     * @return The BaseViewHolder for chaining.
     */
    fun setTag(viewId: Int, tag: Any?): BaseViewHolder {
        val view = getView<View>(viewId)!!
        view.tag = tag
        return this
    }

    /**
     * Sets the tag of the view.
     *
     * @param viewId The view id.
     * @param key    The key of tag;
     * @param tag    The tag;
     * @return The BaseViewHolder for chaining.
     */
    fun setTag(viewId: Int, key: Int, tag: Any?): BaseViewHolder {
        val view = getView<View>(viewId)!!
        view.setTag(key, tag)
        return this
    }

    /**
     * Sets the checked status of a checkable.
     *
     * @param viewId  The view id.
     * @param checked The checked status;
     * @return The BaseViewHolder for chaining.
     */
    fun setChecked(viewId: Int, checked: Boolean): BaseViewHolder {
        val view = getView<View>(viewId)!!
        // View unable cast to Checkable
        if (view is CompoundButton) {
            view.isChecked = checked
        } else if (view is CheckedTextView) {
            view.isChecked = checked
        }
        return this
    }

    /**
     * Sets the adapter of a adapter view.
     *
     * @param viewId  The view id.
     * @param adapter The adapter;
     * @return The BaseViewHolder for chaining.
     */
    fun setAdapter(viewId: Int, adapter: Adapter?): BaseViewHolder {
        val view = getView<AdapterView<Adapter?>>(viewId)!!
        view.adapter = adapter
        return this
    }

    fun <T : View?> getView(viewId: Int): T? {
        var view = views[viewId]
        if (view == null) {
            view = convertView!!.findViewById(viewId)
            views.put(viewId, view)
        }
        return view as T?
    }

    val context: Context?
        get() = if (convertView == null) {
            null
        } else convertView!!.context

//    var noticeViewId: Int = 0

    var noticeView: View? = null

}