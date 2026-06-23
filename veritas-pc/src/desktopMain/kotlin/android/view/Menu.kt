package android.view

interface Menu {
    fun clear()
    fun add(groupId: Int, itemId: Int, order: Int, title: CharSequence): MenuItem
}

interface MenuItem {
    val itemId: Int
    var isEnabled: Boolean
    fun setShowAsAction(actionEnum: Int): MenuItem
    var showAsAction: Int

    companion object {
        const val SHOW_AS_ACTION_NEVER = 0
        const val SHOW_AS_ACTION_IF_ROOM = 1
        const val SHOW_AS_ACTION_ALWAYS = 2
        const val SHOW_AS_ACTION_WITH_TEXT = 4
        const val SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW = 8
    }
}

