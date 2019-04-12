package android.mf.application.util

import android.os.Handler
import android.os.Message
import java.util.*

class Logcat {
    companion object {
        private var LogContent: StringBuffer? = null
        private var MessageHandler: Handler? = null
        fun i(tag: String, i: Any?): StringBuffer? {
            return Content(tag, i, "info")
        }

        fun d(tag: String, d: Any?): StringBuffer? {
            return Content(tag, d, "debug")
        }

        fun e(tag: String, e: Any?): StringBuffer? {
            return Content(tag, e, "error")
        }

        fun w(tag: String, w: Any?): StringBuffer? {
            return Content(tag, w, "warn")
        }

        fun getLogContent(): StringBuffer? {
            if (LogContent != null && LogContent!!.length > 0) {

                return LogContent!!
            } else return null
        }

        fun setHandler(handler: Handler?) {
            if (MessageHandler == null) {
                MessageHandler = handler
            }
            if (handler == null) {
                MessageHandler = null
            }
        }

        fun emptyLog() {
            if (LogContent != null) {
                LogContent!!.setLength(0)
                if (MessageHandler != null) {
                    var msg: Message = Message()
                    msg.arg1 = 0
                    MessageHandler!!.sendMessage(msg)
                }
            }
        }

        private fun Content(tag: String, obj: Any?, type: String): StringBuffer? {
            var tag = tag
            var cal: Calendar = Calendar.getInstance()
            var Date: String =
                cal.get(Calendar.YEAR).toString() + "年" + (cal.get(Calendar.MONTH) + 1) + "月" + cal.get(Calendar.DATE) + "日"
            var Time: String? = null
            if (cal.get(Calendar.AM_PM) == 0) {
                Time =
                    cal.get(Calendar.HOUR).toString() + "点" + cal.get(Calendar.MINUTE) + "分" + cal.get(Calendar.SECOND) + "秒"
            } else {
                Time =
                    (cal.get(Calendar.HOUR) + 12).toString() + "点" + cal.get(Calendar.MINUTE) + "分" + cal.get(Calendar.SECOND) + "秒"
            }
            if (LogContent == null) {
                LogContent = StringBuffer()
            }
            if (tag != null && obj != null) {
                if (tag == null || tag === "") {
                    tag = "未知"
                }
                LogContent!!.append(Date + " " + Time + "\n来自---> " + tag + ":\n" + type + ":" + obj + "\n")
                if (MessageHandler != null) {
                    var msg: Message = Message()
                    msg.obj = LogContent
                    MessageHandler!!.sendMessage(msg)
                }
            }
            return LogContent
        }
    }
}