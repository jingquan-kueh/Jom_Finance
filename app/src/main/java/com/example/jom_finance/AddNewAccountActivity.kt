package com.example.jom_finance


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
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
import android.content.ContentValues.TAG
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import com.example.jom_finance.models.Account
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception


class AddNewAccountActivity : AppCompatActivity(), IconDialog.Callback {

    private lateinit var fAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var userID: String

    private lateinit var accountName: String
    private var accountAmount: Double = 0.0
    private var accountIcon: Int = 278
    private var accountColor: Int = -123456


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_account)
        setupDataBase()


        //setup ICON PICKER
        // If dialog is already added to fragment manager, get it. If not, create a new instance.
        val iconDialog = supportFragmentManager.findFragmentByTag(ICON_DIALOG_TAG) as IconDialog?
            ?: IconDialog.newInstance(IconDialogSettings())

        // Create an icon pack loader with application context.
        val loader = IconPackLoader(this)

        // Create an icon pack and load all drawables.
        val iconPack = createDefaultIconPack(loader)
        iconPack.loadDrawables(loader.drawableLoader)

        //Load default icon
        val drawable = iconPack.getIconDrawable(278, IconDrawableLoader(this))
        accountIcon_img.setImageDrawable(drawable)

        //Load default color
        accountColour_btn.setBackgroundColor(accountColour_btn.context.resources.getColor(R.color.iris))


        //Open Icon dialog
        accountIcon_img.setOnClickListener {
            iconDialog.show(supportFragmentManager, ICON_DIALOG_TAG)
        }

        //Open Color dialog
        accountColour_btn.setOnClickListener {
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
                    setButtonColor(color)
                    accountColor = color
                }
                positiveButton(R.string.select)
                negativeButton(R.string.cancel)
            }
        }

        //Add new account to database
        addNewAccountConfirm_btn.setOnClickListener {

            accountName = accountName_outlinedTextField.editText?.text.toString()
            accountAmount = balanceAmount_edit.text.toString().toDouble()


            try {
                fStore.collection("accounts/$userID/account_detail")
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {

                            //set pathway
                            val documentReference =
                                fStore.collection("accounts/$userID/account_detail").document(accountName)

                            //Account Details
                            val accountDetail = Account(accountName, accountAmount, accountIcon, accountColor)

                            //Insert to database
                            documentReference.set(accountDetail).addOnCompleteListener {
                                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.w(TAG, "Error adding document", e)
            }
        }
    }


    private fun setButtonColor(color: Int){
        accountColour_btn.setBackgroundColor(color)
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

        accountIcon = id
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