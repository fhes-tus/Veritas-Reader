package android.view

interface ActionMode {
    interface Callback {
        fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean
        fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean
        fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean
        fun onDestroyActionMode(mode: ActionMode)
    }
    fun finish()
}
