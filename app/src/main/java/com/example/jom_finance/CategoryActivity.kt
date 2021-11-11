package com.example.jom_finance

import android.content.ContentValues
import android.graphics.Color.BLACK
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.example.jom_finance.models.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.maltaisn.icondialog.IconDialog
import com.maltaisn.icondialog.IconDialogSettings
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.pack.IconDrawableLoader
import com.maltaisn.icondialog.pack.IconPack
import com.maltaisn.icondialog.pack.IconPackLoader
import com.maltaisn.iconpack.defaultpack.createDefaultIconPack
import kotlinx.android.synthetic.main.activity_category.*
import java.lang.Exception

class CategoryActivity : AppCompatActivity(), IconDialog.Callback{

    private lateinit var fAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var userID: String

    private lateinit var categoryName: String
    private var categoryIcon: Int = 278
    private var categoryColor: Int = BLACK


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)
        setupDataBase()

        //setup ICON PICKER
        // If dialog is already added to fragment manager, get it. If not, create a new instance.
        val iconDialog = supportFragmentManager.findFragmentByTag(CategoryActivity.ICON_DIALOG_TAG) as IconDialog?
            ?: IconDialog.newInstance(IconDialogSettings())

        // Create an icon pack loader with application context.
        val loader = IconPackLoader(this)

        // Create an icon pack and load all drawables.
        val iconPack = createDefaultIconPack(loader)
        iconPack.loadDrawables(loader.drawableLoader)

        //Load initial icon
        val drawable = iconPack.getIconDrawable(278, IconDrawableLoader(this))
        categoryIcon_img.setImageDrawable(drawable)

        //Load initial color
        categoryColour_btn.setBackgroundColor(categoryColour_btn.context.resources.getColor(R.color.iris))

        //Open Icon dialog
        categoryIcon_img.setOnClickListener {
            iconDialog.show(supportFragmentManager, CategoryActivity.ICON_DIALOG_TAG)
        }

        //Open Color dialog
        categoryColour_btn.setOnClickListener {
            val colors = intArrayOf(
                ResourcesCompat.getColor(resources, R.color.red_500, null),
                ResourcesCompat.getColor(resources, R.color.pink_500, null),
                ResourcesCompat.getColor(resources, R.color.purple500, null),
                ResourcesCompat.getColor(resources, R.color.indigo_500, null),
                ResourcesCompat.getColor(resources, R.color.blue_500, null),
                ResourcesCompat.getColor(resources, R.color.cyan_500, null),
                ResourcesCompat.getColor(resources, R.color.green_500, null),
                ResourcesCompat.getColor(resources, R.color.yellow_500, null),
                ResourcesCompat.getColor(resources, R.color.orange_500, null),
                ResourcesCompat.getColor(resources, R.color.deepOrange_500, null),
                ResourcesCompat.getColor(resources, R.color.brown_500, null),
                ResourcesCompat.getColor(resources, R.color.grey_500, null),
                ResourcesCompat.getColor(resources, R.color.blueGrey_500, null)
            )

            val subColors = arrayOf( // size = 3
                intArrayOf(
                    ResourcesCompat.getColor(resources, R.color.red_100, null),
                    ResourcesCompat.getColor(resources, R.color.red_200, null),
                    ResourcesCompat.getColor(resources, R.color.red_300, null),
                    ResourcesCompat.getColor(resources, R.color.red_400, null),
                    ResourcesCompat.getColor(resources, R.color.red_500, null),
                    ResourcesCompat.getColor(resources, R.color.red_600, null),
                    ResourcesCompat.getColor(resources, R.color.red_700, null),
                    ResourcesCompat.getColor(resources, R.color.red_800, null),
                    ResourcesCompat.getColor(resources, R.color.red_900, null)
                ),
                intArrayOf(
                    ResourcesCompat.getColor(resources, R.color.pink_100, null),
                    ResourcesCompat.getColor(resources, R.color.pink_200, null),
                    ResourcesCompat.getColor(resources, R.color.pink_300, null),
                    ResourcesCompat.getColor(resources, R.color.pink_400, null),
                    ResourcesCompat.getColor(resources, R.color.pink_500, null),
                    ResourcesCompat.getColor(resources, R.color.pink_600, null),
                    ResourcesCompat.getColor(resources, R.color.pink_700, null),
                    ResourcesCompat.getColor(resources, R.color.pink_800, null),
                    ResourcesCompat.getColor(resources, R.color.pink_900, null)
                ),
                intArrayOf(
                    ResourcesCompat.getColor(resources, R.color.purple_100, null),
                    ResourcesCompat.getColor(resources, R.color.purple200, null),
                    ResourcesCompat.getColor(resources, R.color.purple_300, null),
                    ResourcesCompat.getColor(resources, R.color.purple_400, null),
                    ResourcesCompat.getColor(resources, R.color.purple500, null),
                    ResourcesCompat.getColor(resources, R.color.purple_600, null),
                    ResourcesCompat.getColor(resources, R.color.purple700, null),
                    ResourcesCompat.getColor(resources, R.color.purple_800, null),
                    ResourcesCompat.getColor(resources, R.color.purple_900, null)
                ),
                intArrayOf(
                    ResourcesCompat.getColor(resources, R.color.indigo_100, null),
                    ResourcesCompat.getColor(resources, R.color.indigo_200, null),
                    ResourcesCompat.getColor(resources, R.color.indigo_300, null),
                    ResourcesCompat.getColor(resources, R.color.indigo_400, null),
                    ResourcesCompat.getColor(resources, R.color.indigo_500, null),
                    ResourcesCompat.getColor(resources, R.color.indigo_600, null),
                    ResourcesCompat.getColor(resources, R.color.indigo_700, null),
                    ResourcesCompat.getColor(resources, R.color.indigo_800, null),
                    ResourcesCompat.getColor(resources, R.color.indigo_900, null)
                ),
                intArrayOf(
                    ResourcesCompat.getColor(resources, R.color.blue_100, null),
                    ResourcesCompat.getColor(resources, R.color.blue_200, null),
                    ResourcesCompat.getColor(resources, R.color.blue_300, null),
                    ResourcesCompat.getColor(resources, R.color.blue_400, null),
                    ResourcesCompat.getColor(resources, R.color.blue_500, null),
                    ResourcesCompat.getColor(resources, R.color.blue_600, null),
                    ResourcesCompat.getColor(resources, R.color.blue_700, null),
                    ResourcesCompat.getColor(resources, R.color.blue_800, null),
                    ResourcesCompat.getColor(resources, R.color.blue_900, null)
                ),
                intArrayOf(
                    ResourcesCompat.getColor(resources, R.color.cyan_100, null),
                    ResourcesCompat.getColor(resources, R.color.cyan_200, null),
                    ResourcesCompat.getColor(resources, R.color.cyan_300, null),
                    ResourcesCompat.getColor(resources, R.color.cyan_400, null),
                    ResourcesCompat.getColor(resources, R.color.cyan_500, null),
                    ResourcesCompat.getColor(resources, R.color.cyan_600, null),
                    ResourcesCompat.getColor(resources, R.color.cyan_700, null),
                    ResourcesCompat.getColor(resources, R.color.cyan_800, null),
                    ResourcesCompat.getColor(resources, R.color.cyan_900, null)
                ),
                intArrayOf(
                    ResourcesCompat.getColor(resources, R.color.green_100, null),
                    ResourcesCompat.getColor(resources, R.color.green_200, null),
                    ResourcesCompat.getColor(resources, R.color.green_300, null),
                    ResourcesCompat.getColor(resources, R.color.green_400, null),
                    ResourcesCompat.getColor(resources, R.color.green_500, null),
                    ResourcesCompat.getColor(resources, R.color.green_600, null),
                    ResourcesCompat.getColor(resources, R.color.green_700, null),
                    ResourcesCompat.getColor(resources, R.color.green_800, null),
                    ResourcesCompat.getColor(resources, R.color.green_900, null)
                ),
                intArrayOf(
                    ResourcesCompat.getColor(resources, R.color.yellow_100, null),
                    ResourcesCompat.getColor(resources, R.color.yellow_200, null),
                    ResourcesCompat.getColor(resources, R.color.yellow_300, null),
                    ResourcesCompat.getColor(resources, R.color.yellow_400, null),
                    ResourcesCompat.getColor(resources, R.color.yellow_500, null),
                    ResourcesCompat.getColor(resources, R.color.yellow_600, null),
                    ResourcesCompat.getColor(resources, R.color.yellow_700, null),
                    ResourcesCompat.getColor(resources, R.color.yellow_800, null),
                    ResourcesCompat.getColor(resources, R.color.yellow_900, null)
                ),
                intArrayOf(
                    ResourcesCompat.getColor(resources, R.color.orange_100, null),
                    ResourcesCompat.getColor(resources, R.color.orange_200, null),
                    ResourcesCompat.getColor(resources, R.color.orange_300, null),
                    ResourcesCompat.getColor(resources, R.color.orange_400, null),
                    ResourcesCompat.getColor(resources, R.color.orange_500, null),
                    ResourcesCompat.getColor(resources, R.color.orange_600, null),
                    ResourcesCompat.getColor(resources, R.color.orange_700, null),
                    ResourcesCompat.getColor(resources, R.color.orange_800, null),
                    ResourcesCompat.getColor(resources, R.color.orange_900, null)
                ),
                intArrayOf(
                    ResourcesCompat.getColor(resources, R.color.deepOrange_100, null),
                    ResourcesCompat.getColor(resources, R.color.deepOrange_200, null),
                    ResourcesCompat.getColor(resources, R.color.deepOrange_300, null),
                    ResourcesCompat.getColor(resources, R.color.deepOrange_400, null),
                    ResourcesCompat.getColor(resources, R.color.deepOrange_500, null),
                    ResourcesCompat.getColor(resources, R.color.deepOrange_600, null),
                    ResourcesCompat.getColor(resources, R.color.deepOrange_700, null),
                    ResourcesCompat.getColor(resources, R.color.deepOrange_800, null),
                    ResourcesCompat.getColor(resources, R.color.deepOrange_900, null)
                ),
                intArrayOf(
                    ResourcesCompat.getColor(resources, R.color.brown_100, null),
                    ResourcesCompat.getColor(resources, R.color.brown_200, null),
                    ResourcesCompat.getColor(resources, R.color.brown_300, null),
                    ResourcesCompat.getColor(resources, R.color.brown_400, null),
                    ResourcesCompat.getColor(resources, R.color.brown_500, null),
                    ResourcesCompat.getColor(resources, R.color.brown_600, null),
                    ResourcesCompat.getColor(resources, R.color.brown_700, null),
                    ResourcesCompat.getColor(resources, R.color.brown_800, null),
                    ResourcesCompat.getColor(resources, R.color.brown_900, null)
                ),
                intArrayOf(
                    ResourcesCompat.getColor(resources, R.color.grey_100, null),
                    ResourcesCompat.getColor(resources, R.color.grey_200, null),
                    ResourcesCompat.getColor(resources, R.color.grey_300, null),
                    ResourcesCompat.getColor(resources, R.color.grey_400, null),
                    ResourcesCompat.getColor(resources, R.color.grey_500, null),
                    ResourcesCompat.getColor(resources, R.color.grey_600, null),
                    ResourcesCompat.getColor(resources, R.color.grey_700, null),
                    ResourcesCompat.getColor(resources, R.color.grey_800, null),
                    ResourcesCompat.getColor(resources, R.color.grey_900, null)
                ),
                intArrayOf(
                    ResourcesCompat.getColor(resources, R.color.blueGrey_100, null),
                    ResourcesCompat.getColor(resources, R.color.blueGrey_200, null),
                    ResourcesCompat.getColor(resources, R.color.blueGrey_300, null),
                    ResourcesCompat.getColor(resources, R.color.blueGrey_400, null),
                    ResourcesCompat.getColor(resources, R.color.blueGrey_500, null),
                    ResourcesCompat.getColor(resources, R.color.blueGrey_600, null),
                    ResourcesCompat.getColor(resources, R.color.blueGrey_700, null),
                    ResourcesCompat.getColor(resources, R.color.blueGrey_800, null),
                    ResourcesCompat.getColor(resources, R.color.blueGrey_900, null)
                ),
            )

            MaterialDialog(this).show {
                title(R.string.colors)
                colorChooser(colors, subColors = subColors) { dialog, color ->
                    setColor(color)
                    categoryColor = color
                }
                positiveButton(R.string.select)
                negativeButton(R.string.cancel)
            }
        }

        categoryConfirm_btn.setOnClickListener {

            categoryName = categoryName_edit.text.toString()
            Toast.makeText(this, categoryName, Toast.LENGTH_SHORT).show()

            try {
                fStore.collection("category/$userID/category_detail")
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            //set pathway
                            val documentReference =
                                fStore.collection("category/$userID/category_detail").document(categoryName)

                            //Category Details
                            val categoryDetail = Category(categoryName, categoryIcon, categoryColor)

                            //Insert to database
                            documentReference.set(categoryDetail).addOnCompleteListener {
                                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
            } catch (e: Exception) {
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
        }
    }

    private fun setColor(color: Int){
        categoryColour_btn.setBackgroundColor(color)
        categoryBackground.setBackgroundColor(color)
        backBtn_Category.setBackgroundColor(color)
        categoryIcon_img.setColorFilter(color)
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
        categoryIcon_img.setImageDrawable(drawable)
        categoryIcon_img.setColorFilter(categoryColor)

        Toast.makeText(this, "Icons selected: ${icons.map { it.id }}", Toast.LENGTH_SHORT).show()

        categoryIcon = id
    }

    companion object {
        private const val ICON_DIALOG_TAG = "icon-dialog"
    }

    private fun setupDataBase() {
        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
        val currentUser = fAuth.currentUser
        if (currentUser != null) {
            userID = currentUser.uid
        }
    }
}