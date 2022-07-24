package co.luoja.recyclerspinner.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.*
import android.widget.Toast
import co.luoja.recyclerspinner.*
import com.luoja.recyclerspinner.demo.R
import com.luoja.recyclerspinner.demo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val adapter = DemoNestedAdapter()

        adapter.submitNestedList(
            nestedListOf(
                item("Alpha"),
                item("Beta"),
                section("Etc", "Gamma", "Kappa", "Epsilon")
            )
        )

        adapter.setOnItemSelectedListener {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        binding.recyclerSpinner.adapter = adapter

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

}