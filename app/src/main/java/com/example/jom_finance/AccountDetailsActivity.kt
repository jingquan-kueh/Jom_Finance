package com.example.jom_finance

import android.content.Intent
import android.graphics.Color
import android.graphics.Color.WHITE
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.maltaisn.icondialog.pack.IconDrawableLoader
import com.maltaisn.icondialog.pack.IconPackLoader
import com.maltaisn.iconpack.defaultpack.createDefaultIconPack
import kotlinx.android.synthetic.main.activity_account_details.*

class AccountDetailsActivity : AppCompatActivity() {

    private lateinit var accountName: String
    private var accountAmount: Double = 0.0
    private var accountIcon: Int = 278
    private var accountColor: Int = Color.BLACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_details)

        setFavourite_btn.visibility = View.GONE

        //Icon loader
        val loader = IconPackLoader(this)
        val iconPack = createDefaultIconPack(loader)

        accountName = intent?.extras?.getString("account_name").toString()
        if(accountName != "null") {
            accountAmount = intent?.extras?.getDouble("account_amount")!!
            accountIcon = intent?.extras?.getInt("account_icon")!!
            accountColor = intent?.extras?.getInt("account_color")!!

            accountDetails_bg.setBackgroundColor(accountColor)

            val drawable = iconPack.getIconDrawable(accountIcon, IconDrawableLoader(this))
            accountDetailIcon_img.setImageDrawable(drawable)
            accountDetailIcon_img.setColorFilter(WHITE)

            accountDetailName_text.text = accountName
            accountDetailBalance_text.text = "RM " + String.format("%.2f", accountAmount)


        }

        editAccount_btn.setOnClickListener {
            val intent = Intent(this, AddNewAccountActivity::class.java)
            intent.putExtra("account_name", accountName)
            intent.putExtra("account_amount", accountAmount)
            intent.putExtra("account_icon", accountIcon)
            intent.putExtra("account_color", accountColor)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        setFavourite_btn.setOnClickListener{
            setFavourite()
        }

    }
    private fun setFavourite(){
        setFavourite_btn.setImageResource(R.drawable.ic_baseline_star_24)
    }
}