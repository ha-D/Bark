package sh.hadi.bark

import android.view.Menu
import android.view.SubMenu
import android.view.View

abstract class BarkCommands {
    private val menuMap: MutableMap<Int, () -> Unit> = mutableMapOf()
    private var menu: Menu? = null
    private var commandNames = mutableMapOf<Int, String>()

    fun handleMenuItem(itemId: Int): Boolean {
        val handler = menuMap[itemId] ?: return false
        onCommandCalled(handler)
        return true
    }

    fun populateMenu(menu: Menu) {
        if (menuMap.isNotEmpty()) {
            return
        }
        this.menu = menu
        initializeCommands()
    }

    fun getCommandName(commandId: Int): String? = commandNames[commandId]

    inner class SubMenuCreator(private val subMenu: SubMenu) {
        fun addCommand(text: String, handler: () -> Unit) {
            val id = View.generateViewId()
            menuMap[id] = handler
            subMenu.add(0, id, Menu.NONE, text)
            commandNames[id] = text
        }

        fun subMenu(title: String, subMenuCreatorContext: SubMenuCreatorContext? = null): SubMenuCreator {
            val submenuCreator = SubMenuCreator(subMenu.addSubMenu(title))
            subMenuCreatorContext?.let { submenuCreator.subMenuCreatorContext() }
            return submenuCreator
        }
    }

    protected fun addCommand(text: String, handler: () -> Unit) {
        val id = View.generateViewId()
        menuMap[id] = handler
        menu?.add(0, id, Menu.NONE, text)
        commandNames[id] = text
    }

    protected fun subMenu(title: String, subMenuCreatorContext: SubMenuCreatorContext? = null): SubMenuCreator? {
        return menu?.let { menu ->
            val submenuCreator = SubMenuCreator(menu.addSubMenu(title))
            subMenuCreatorContext?.let { submenuCreator.subMenuCreatorContext() }
            submenuCreator
        }
    }

    abstract fun initializeCommands()
    abstract fun onCommandCalled(handler: () -> Unit)
}

typealias SubMenuCreatorContext = BarkCommands.SubMenuCreator.() -> Unit