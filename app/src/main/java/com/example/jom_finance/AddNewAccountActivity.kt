package com.example.jom_finance

import android.R.attr
import android.graphics.Color.*
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.maltaisn.icondialog.IconDialog
import com.maltaisn.icondialog.IconDialogSettings
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.pack.IconDrawableLoader
import com.maltaisn.icondialog.pack.IconPack
import com.maltaisn.icondialog.pack.IconPackLoader
import com.maltaisn.iconpack.defaultpack.createDefaultIconPack
import kotlinx.android.synthetic.main.activity_add_new_account.*
import java.lang.String
import android.R.attr.button
import android.R.attr.button
import android.graphics.PorterDuff

import androidx.core.graphics.drawable.DrawableCompat







class AddNewAccountActivity : AppCompatActivity(), IconDialog.Callback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_account)


        //ICON PICKER
        // If dialog is already added to fragment manager, get it. If not, create a new instance.
        val iconDialog = supportFragmentManager.findFragmentByTag(ICON_DIALOG_TAG) as IconDialog?
            ?: IconDialog.newInstance(IconDialogSettings())

        // Create an icon pack loader with application context.
        val loader = IconPackLoader(this)

        // Create an icon pack and load all drawables.
        val iconPack = createDefaultIconPack(loader)
        iconPack.loadDrawables(loader.drawableLoader)

        val drawable = iconPack.getIconDrawable(278, IconDrawableLoader(this))

        accountIcon_img.setImageDrawable(drawable)

        accountColour_btn.setBackgroundColor(accountColour_btn.context.resources.getColor(R.color.iris))



        accountIcon_img.setOnClickListener {
            // Open icon dialog
            iconDialog.show(supportFragmentManager, ICON_DIALOG_TAG)
        }

        accountColour_btn.setOnClickListener {
            val colors = intArrayOf(RED, GREEN, BLUE)
            val subColors = arrayOf( // size = 3
                intArrayOf(MAGENTA, RED, YELLOW, WHITE),
                intArrayOf( GREEN, DKGRAY, GRAY),
                intArrayOf(CYAN, BLUE, LTGRAY, BLACK)
            )

            MaterialDialog(this).show {
                title(R.string.colors)
                colorChooser(colors, subColors = subColors) { dialog, color ->
                    setbuttonColour(color)
                }
                positiveButton(R.string.select)
                negativeButton(R.string.cancel)
            }
        }

    }

    fun setbuttonColour(color: Int){
        accountColour_btn.setBackgroundColor(color);
    }

    override val iconDialogIconPack: IconPack?
        get() = (application as App).iconPack

    override fun onIconDialogIconsSelected(dialog: IconDialog, icons: List<Icon>) {

        val id = icons.map { it.id }[0]

        // Create an icon pack loader with application context.
        val loader = IconPackLoader(this)

        // Create an icon pack and load all drawables.
        val iconPack = createDefaultIconPack(loader)
        iconPack.loadDrawables(loader.drawableLoader)

        val drawable = iconPack.getIconDrawable(id, IconDrawableLoader(this))

        accountIcon_img.setImageDrawable(drawable)
        Toast.makeText(this, "Icons selected: ${icons.map { it.id }}", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val ICON_DIALOG_TAG = "icon-dialog"
    }
}