package com.example.trainexplore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.trainexplore.databinding.ActivityMainBinding
import com.example.trainexplore.loginSystem.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var  binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLoginStatus()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(Comboios())

        binding.bottomNavigationView.setOnItemSelectedListener {

            when(it.itemId){

                R.id.comboios -> replaceFragment(Comboios())
                R.id.favoritos -> replaceFragment(Favoritos())
                R.id.noticias -> replaceFragment(Noticias())
                R.id.perfil -> replaceFragment(Perfil())

                else ->{

                }
            }
            true
        }

    }

    private fun checkLoginStatus() {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            "MeuPerfil",
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val isUtilizadorLoggedIn = sharedPreferences.getBoolean("IsUtilizadorLoggedIn",false)

        if (!isUtilizadorLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }




    private fun replaceFragment(fragment : Fragment)
    {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }
}