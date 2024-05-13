package com.example.trainexplore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.trainexplore.databinding.ActivityMainBinding
import com.example.trainexplore.loginSystem.LoginActivity
import com.example.trainexplore.loginSystem.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var  binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLoginStatus()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize with the Comboios fragment
        replaceFragment(Comboios())
        binding.bottomNavigationView.selectedItemId = R.id.comboios  // Ensure correct item is highlighted

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.comboios -> replaceFragment(Comboios())
                R.id.favoritos -> replaceFragment(Favoritos())
                R.id.noticias -> replaceFragment(Noticias())
                R.id.perfil -> replaceFragment(Perfil())
            }
            true  // Returning true here marks the selection
        }
    }

    private fun checkLoginStatus() {
        if (SessionManager.userId == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}
