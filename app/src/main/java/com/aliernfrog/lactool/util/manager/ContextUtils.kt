package com.aliernfrog.lactool.util.manager

import android.content.Context

class ContextUtils(
    context: Context
) {
    var run: (block: (Context) -> Unit) -> Unit
        private set

    var getString: (id: Int) -> String
        private set

    init {
        run = {
            it(context)
        }

        getString = {
            context.getString(it)
        }
    }
}