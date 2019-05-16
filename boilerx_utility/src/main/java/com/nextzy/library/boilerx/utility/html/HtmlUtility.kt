package com.nextzy.library.boilerx.utility.html

import android.os.Build
import android.text.Html
import android.text.Spanned

class HtmlUtility {
    companion object {
        const val TAG_UNORDERED_LIST = "tag_unordered_list"
        const val TAG_ORDER_LIST = "tag_order_list"
        const val TAG_LIST = "tag_list"

        @Suppress("DEPRECATION")
        fun fromHtml(htmlText: String?): Spanned {
            val text = customizeListTag(htmlText)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT, null,
                    HtmlTagHandler()
                )
            } else {
                Html.fromHtml(text, null, HtmlTagHandler())
            }
        }

        private fun customizeListTag(html: String?): String? {
            return if (html.isNullOrEmpty()) {
                ""
            } else {
                var customizeHtml = html
                customizeHtml = customizeHtml.replace("<ul", "<$TAG_UNORDERED_LIST")
                customizeHtml = customizeHtml.replace("</ul>", "</$TAG_UNORDERED_LIST>")
                customizeHtml = customizeHtml.replace("<ol", "<$TAG_ORDER_LIST")
                customizeHtml = customizeHtml.replace("</ol>", "</$TAG_ORDER_LIST>")
                customizeHtml = customizeHtml.replace("<li", "<$TAG_LIST")
                customizeHtml = customizeHtml.replace("</li>", "</$TAG_LIST>");
                return customizeHtml
            }
        }
    }
}