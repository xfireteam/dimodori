package com.dimodori.app.tv.ui.focus

/** Server item types and Seerr media types; unknown types are never inferred. */
internal fun showsTvContentFocusOutline(type: String?, focused: Boolean): Boolean =
    focused && (type.equals("Movie", ignoreCase = true) ||
        type.equals("Episode", ignoreCase = true))
