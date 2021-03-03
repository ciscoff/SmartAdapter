package s.yarlykov.example

import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import s.yarlykov.lib.smartadapter.adapter.SmartAdapter
import s.yarlykov.lib.smartadapter.model.SmartList

class MainActivity : AppCompatActivity() {

    private val smartAdapter = SmartAdapter()

    private val model = listOf(
        TextModel("It's a header 1", "Description 1"),
        TextModel("It's a header 2", "Description 2")
    )

    private val controller1 = Controller1(R.layout.item_text_one_row)
    private val controller2 = Controller2(R.layout.item_text_two_rows)

    private val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = smartAdapter
            setPadding(0, 16.px, 0, 16.px)
        }

        SmartList.create().apply {
            repeat(2) {
                addItem(model[0], controller1)
            }
            repeat(3) {
                addItem(model[1], controller2)
            }
            repeat(1) {
                addItem(model[0], controller1)
            }
            repeat(5) {
                addItem(model[1], controller2)
            }
        }.also(smartAdapter::updateModel)
    }
}