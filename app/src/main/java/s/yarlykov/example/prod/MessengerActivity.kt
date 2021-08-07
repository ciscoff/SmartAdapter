package s.yarlykov.example.prod

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import s.yarlykov.example.R
import s.yarlykov.example.databinding.ActivityMessengerBinding

class MessengerActivity : AppCompatActivity() {

    lateinit var viewModel: MessengerViewModel

    private lateinit var binding: ActivityMessengerBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessengerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MessengerViewModel::class.java)


    }

    /**
     * Вспомогательная функция, заменяющая повсеместное использование 'binding?'
     */
    private fun viewBinding(op: ActivityMessengerBinding.() -> Unit) {
        binding.op()
    }
}