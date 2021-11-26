package gg.scala.lemon.player.entity

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.google.common.base.Preconditions
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import gg.scala.lemon.Lemon
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.plugin.Plugin
import java.lang.reflect.InvocationTargetException
import java.util.*

open class EntityHider
@JvmOverloads
constructor(plugin: Plugin = Lemon.instance, policy: Policy = Policy.WHITELIST) : Listener
{
    protected var observerEntityMap: Table<Int, Int, Boolean?> = HashBasedTable.create()

    /**
     * The current entity visibility policy.
     * @author Kristian
     */
    enum class Policy
    {
        /**
         * All entities are invisible by default. Only entities specifically made visible may be seen.
         */
        WHITELIST,

        /**
         * All entities are visible by default. An entity can only be hidden explicitly.
         */
        BLACKLIST
    }

    private var manager: ProtocolManager

    // Listeners
    private var bukkitListener: Listener? = null
    private var protocolListener: PacketAdapter? = null

    /**
     * Retrieve the current visibility policy.
     * @return The current visibility policy.
     */
    // Current policy
    private val policy: Policy

    /**
     * Set the visibility status of a given entity for a particular observer.
     * @param observer - the observer player.
     * @param entity - ID of the entity that will be hidden or made visible.
     * @param visible - TRUE if the entity should be made visible, FALSE if not.
     * @return TRUE if the entity was visible before this method call, FALSE otherwise.
     */
    protected fun setVisibility(observer: Player, entityID: Int, visible: Boolean): Boolean
    {
        return when (policy)
        {
            Policy.BLACKLIST ->                 // Non-membership means they are visible
                !setMembership(observer, entityID, !visible)
            Policy.WHITELIST -> setMembership(observer, entityID, visible)
            else -> throw IllegalArgumentException("Unknown policy: $policy")
        }
    }

    /**
     * Add or remove the given entity and observer entry from the table.
     * @param observer - the player observer.
     * @param entityID - ID of the entity.
     * @param member - TRUE if they should be present in the table, FALSE otherwise.
     * @return TRUE if they already were present, FALSE otherwise.
     */
    // Helper method
    protected fun setMembership(observer: Player, entityID: Int, member: Boolean): Boolean
    {
        return if (member)
        {
            observerEntityMap.put(observer.entityId, entityID, true) != null
        } else
        {
            observerEntityMap.remove(observer.entityId, entityID) != null
        }
    }

    /**
     * Determine if the given entity and observer is present in the table.
     * @param observer - the player observer.
     * @param entityID - ID of the entity.
     * @return TRUE if they are present, FALSE otherwise.
     */
    protected fun getMembership(observer: Player, entityID: Int): Boolean
    {
        return observerEntityMap.contains(observer.entityId, entityID)
    }

    /**
     * Determine if a given entity is visible for a particular observer.
     * @param observer - the observer player.
     * @param entityID -  ID of the entity that we are testing for visibility.
     * @return TRUE if the entity is visible, FALSE otherwise.
     */
    protected fun isVisible(observer: Player, entityID: Int): Boolean
    {
        // If we are using a whitelist, presence means visibility - if not, the opposite is the case
        val presence = getMembership(observer, entityID)
        return if (policy == Policy.WHITELIST) presence else !presence
    }

    /**
     * Remove the given entity from the underlying map.
     * @param entity - the entity to remove.
     * @param destroyed - TRUE if the entity was killed, FALSE if it is merely unloading.
     */
    protected fun removeEntity(entity: Entity, destroyed: Boolean)
    {
        val entityID = entity.entityId
        for (maps in observerEntityMap.rowMap().values)
        {
            maps.remove(entityID)
        }
    }

    /**
     * Invoked when a player logs out.
     * @param player - the player that jused logged out.
     */
    protected fun removePlayer(player: Player)
    {
        // Cleanup
        observerEntityMap.rowMap().remove(player.entityId)
    }

    /**
     * Construct the Bukkit event listener.
     * @return Our listener.
     */
    private fun constructBukkit(): Listener
    {
        return object : Listener
        {
            @EventHandler
            fun onEntityDeath(e: EntityDeathEvent)
            {
                removeEntity(e.entity, true)
            }

            @EventHandler
            fun onChunkUnload(e: ChunkUnloadEvent)
            {
                for (entity in e.chunk.entities)
                {
                    removeEntity(entity, false)
                }
            }

            @EventHandler
            fun onPlayerQuit(e: PlayerQuitEvent)
            {
                removePlayer(e.player)
            }
        }
    }

    /**
     * Construct the packet listener that will be used to intercept every entity-related packet.
     * @param plugin - the parent plugin.
     * @return The packet listener.
     */
    private fun constructProtocol(plugin: Plugin): PacketAdapter
    {
        return object : PacketAdapter(plugin, *ENTITY_PACKETS)
        {
            override fun onPacketSending(event: PacketEvent)
            {
                val entityID = event.packet.integers.read(0)

                // See if this packet should be cancelled
                if (!isVisible(event.player, entityID))
                {
                    event.isCancelled = true
                }
            }
        }
    }

    /**
     * Toggle the visibility status of an entity for a player.
     *
     *
     * If the entity is visible, it will be hidden. If it is hidden, it will become visible.
     * @param observer - the player observer.
     * @param entity - the entity to toggle.
     * @return TRUE if the entity was visible before, FALSE otherwise.
     */
    fun toggleEntity(observer: Player, entity: Entity): Boolean
    {
        return if (isVisible(observer, entity.entityId))
        {
            hideEntity(observer, entity)
        } else
        {
            !showEntity(observer, entity)
        }
    }

    /**
     * Allow the observer to see an entity that was previously hidden.
     * @param observer - the observer.
     * @param entity - the entity to show.
     * @return TRUE if the entity was hidden before, FALSE otherwise.
     */
    fun showEntity(observer: Player, entity: Entity): Boolean
    {
        validate(observer, entity)
        val hiddenBefore = !setVisibility(observer, entity.entityId, true)

        // Resend packets
        if (manager != null && hiddenBefore)
        {
            manager!!.updateEntity(entity, Arrays.asList(observer))
        }
        return hiddenBefore
    }

    /**
     * Prevent the observer from seeing a given entity.
     * @param observer - the player observer.
     * @param entity - the entity to hide.
     * @return TRUE if the entity was previously visible, FALSE otherwise.
     */
    fun hideEntity(observer: Player, entity: Entity): Boolean
    {
        validate(observer, entity)
        val visibleBefore = setVisibility(observer, entity.entityId, false)
        if (visibleBefore)
        {
            val destroyEntity = PacketContainer(PacketType.Play.Server.ENTITY_DESTROY)
            destroyEntity.integerArrays.write(0, intArrayOf(entity.entityId))

            // Make the entity disappear
            try
            {
                manager!!.sendServerPacket(observer, destroyEntity)
            } catch (e: InvocationTargetException)
            {
                throw RuntimeException("Cannot send server packet.", e)
            }
        }
        return visibleBefore
    }

    /**
     * Determine if the given entity has been hidden from an observer.
     *
     *
     * Note that the entity may very well be occluded or out of range from the perspective
     * of the observer. This method simply checks if an entity has been completely hidden
     * for that observer.
     * @param observer - the observer.
     * @param entity - the entity that may be hidden.
     * @return TRUE if the player may see the entity, FALSE if the entity has been hidden.
     */
    fun canSee(observer: Player, entity: Entity): Boolean
    {
        validate(observer, entity)
        return isVisible(observer, entity.entityId)
    }

    // For valdiating the input parameters
    private fun validate(observer: Player, entity: Entity)
    {
        Preconditions.checkNotNull(observer, "observer cannot be NULL.")
        Preconditions.checkNotNull(entity, "entity cannot be NULL.")
    }

    fun close()
    {
        HandlerList.unregisterAll(bukkitListener)
        manager.removePacketListener(protocolListener)
    }

    companion object
    {
        // Packets that update remote player entities
        private val ENTITY_PACKETS = arrayOf(
            PacketType.Play.Server.ENTITY_EQUIPMENT,
            PacketType.Play.Server.BED,
            PacketType.Play.Server.ANIMATION,
            PacketType.Play.Server.NAMED_ENTITY_SPAWN,
            PacketType.Play.Server.COLLECT,
            PacketType.Play.Server.SPAWN_ENTITY,
            PacketType.Play.Server.SPAWN_ENTITY_LIVING,
            PacketType.Play.Server.SPAWN_ENTITY_PAINTING,
            PacketType.Play.Server.SPAWN_ENTITY_EXPERIENCE_ORB,
            PacketType.Play.Server.ENTITY_VELOCITY,
            PacketType.Play.Server.REL_ENTITY_MOVE,
            PacketType.Play.Server.ENTITY_LOOK,
            PacketType.Play.Server.ENTITY_MOVE_LOOK,
            PacketType.Play.Server.ENTITY_MOVE_LOOK,
            PacketType.Play.Server.ENTITY_TELEPORT,
            PacketType.Play.Server.ENTITY_HEAD_ROTATION,
            PacketType.Play.Server.ENTITY_STATUS,
            PacketType.Play.Server.ATTACH_ENTITY,
            PacketType.Play.Server.ENTITY_METADATA,
            PacketType.Play.Server.ENTITY_EFFECT,
            PacketType.Play.Server.REMOVE_ENTITY_EFFECT,
            PacketType.Play.Server.BLOCK_BREAK_ANIMATION // We don't handle DESTROY_ENTITY though
        )
    }

    /**
     * Construct a new entity hider.
     * @param plugin - the plugin that controls this entity hider.
     * @param policy - the default visibility policy.
     */
    init
    {
        Preconditions.checkNotNull(plugin, "plugin cannot be NULL.")

        // Save policy
        this.policy = policy
        manager = ProtocolLibrary.getProtocolManager()

        // Register events and packet listener
        plugin.server.pluginManager.registerEvents(
            constructBukkit().also {
                bukkitListener = it
            }, plugin
        )

        manager.addPacketListener(
            constructProtocol(plugin).also {
                protocolListener = it
            }
        )
    }
}