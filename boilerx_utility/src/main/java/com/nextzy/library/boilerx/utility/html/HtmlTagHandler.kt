package com.nextzy.library.boilerx.utility.html

import android.text.Editable
import android.text.Html
import org.xml.sax.XMLReader

class HtmlTagHandler : Html.TagHandler {
    private var first = true
    private var parent: String? = null
    private var index = 1

    override fun handleTag(opening: Boolean, tag: String?, output: Editable?, xmlReader: XMLReader?) {
        if (tag == HtmlUtility.TAG_UNORDERED_LIST) {
            parent = HtmlUtility.TAG_UNORDERED_LIST
        } else if (tag == HtmlUtility.TAG_ORDER_LIST) {
            parent = HtmlUtility.TAG_ORDER_LIST
        }
        if (tag == HtmlUtility.TAG_LIST) {
            if (parent == HtmlUtility.TAG_LIST) {
                first = if (first) {
                    output?.append("\n\t• ")
                    false
                } else {
                    true
                }
            } else {
                first = if (first) {
                    output?.append("\n\t")?.append(index.toString())?.append(".")
                    index++
                    false
                } else {
                    true
                }
            }
        }
    }

}