package android.mf.application.ui

import android.mf.application.R
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.logcat.*

class LogcatUI : AppCompatActivity() {

    private var TAG: String = "LogcatUI"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.logcat)
        setSupportActionBar(toolbar)
        if (android.mf.application.util.Logcat.getLogContent() != null) {
            Logcat.setText(android.mf.application.util.Logcat.getLogContent())
        }
        android.mf.application.util.Logcat.setHandler(handler)
        Logcat.setMovementMethod(ScrollingMovementMethod.getInstance());
        Logcat.setSelection(Logcat.getText().length, Logcat.getText().length)

        Logcat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                var offset = Logcat.getLineCount() * Logcat.getLineHeight()
                if (offset > Logcat.getHeight()) {
                    Logcat.scrollTo(0, offset - Logcat.getHeight())
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.logcat_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.Log_Up -> {
                Logcat.scrollTo(0, 0);
            }
            R.id.Log_Down -> {
                var offset = Logcat.getLineCount() * Logcat.getLineHeight()
                if (offset > Logcat.getHeight()) {
                    Logcat.scrollTo(0, offset - Logcat.getHeight())
                }
            }
            R.id.emptyLog -> {
                android.mf.application.util.Logcat.emptyLog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        android.mf.application.util.Logcat.setHandler(null)
        super.onDestroy()
    }

    private var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.arg1 == 0) {
                Logcat.scrollTo(0, 0)
                Logcat.setText(null)
            } else{
                Logcat.setText(msg.obj.toString())
            }
            super.handleMessage(msg)
        }
    }
}