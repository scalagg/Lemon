package gg.scala.lemon.util

import net.evilblock.cubed.util.hook.VaultHook

/**
 * @author GrowlyX
 * @since 9/12/2021
 */
object VaultUtil
{
    @JvmStatic
    fun usePermissions(lambda: (Permission) -> Unit)
    {
        try
        {
            VaultHook.usePermissions(lambda)
        } catch (ignored: Exception)
        {
        }
    }
}
