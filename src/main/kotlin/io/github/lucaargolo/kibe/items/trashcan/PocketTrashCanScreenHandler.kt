package io.github.lucaargolo.kibe.items.trashcan

import io.github.lucaargolo.kibe.items.POCKET_TRASH_CAN
import io.github.lucaargolo.kibe.items.getContainerInfo
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.util.Hand
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World
import net.minecraft.inventory.Inventories
import net.minecraft.block.BlockState
import net.minecraft.text.TranslatableText
import net.minecraft.text.Text
import net.minecraft.util.ItemScatterer

@Suppress("UNUSED_PARAMETER")
class PocketTrashCanScreenHandler(syncId: Int, playerInventory: PlayerInventory, hand: Hand, val world: World, tag: CompoundTag?): ScreenHandler(getContainerInfo(POCKET_TRASH_CAN)?.handlerType, syncId) {

    val inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(1, ItemStack.EMPTY)

    private var synchronizedInventory: Inventory = object: Inventory {

        override fun markDirty() {
            return
        }

        override fun clear() {
            inventory.clear()
        }

        override fun setStack(slot: Int, stack: ItemStack?) {
            if (stack?.getEnchantments()?.toString()?.contains("trash_safe") == true) {
                inventory[slot] = stack
            }
            else {
                inventory[slot] = ItemStack.EMPTY
            }
        }

        override fun isEmpty() = inventory.all { it.isEmpty }

        override fun removeStack(slot: Int, amount: Int): ItemStack {
            var result: ItemStack = Inventories.splitStack(inventory, slot, amount)
            if (!result.isEmpty()) {
                markDirty()
            }
            return result
        }

        override fun removeStack(slot: Int): ItemStack {
            return Inventories.removeStack(inventory, slot)
        }

        override fun getStack(slot: Int): ItemStack {
            return inventory[slot]
        }

        override fun canPlayerUse(player: PlayerEntity?): Boolean {
            return true
        }

        override fun size() = inventory.size

    }

    init {
        checkSize(synchronizedInventory, 1)
        synchronizedInventory.onOpen(playerInventory.player)
        val i: Int = (3 - 4) * 18

        addSlot(Slot(synchronizedInventory, 0, 8 + 4*18,  36))

        (0..2).forEach {n ->
            (0..8).forEach { m ->
                addSlot(Slot(playerInventory, m + n * 9 + 9, 8 + m * 18, 103 + n * 18 + i))
            }
        }

        (0..8).forEach { n ->
            addSlot(Slot(playerInventory, n, 8 + n * 18, 161 + i))
        }

    }

    override fun canUse(player: PlayerEntity): Boolean {
        return true
    }

    override fun transferSlot(player: PlayerEntity?, invSlot: Int): ItemStack? {
        var itemStack = ItemStack.EMPTY
        val slot = this.slots[invSlot]
        if (slot != null && slot.hasStack()) {
            val itemStack2 = slot.stack
            itemStack = itemStack2.copy()
            if (invSlot < 1) {
                if (!insertItem(itemStack2, 1, this.slots.size, true)) {
                    return ItemStack.EMPTY
                }
            } else if (!insertItem(itemStack2, 0, 1, false)) {
                return ItemStack.EMPTY
            }
            if (itemStack2.isEmpty) {
                slot.stack = ItemStack.EMPTY
            } else {
                slot.markDirty()
            }
        }
        return itemStack
    }

    override fun close(player: PlayerEntity?) {
        super.close(player)
        dropInventory(player, world, synchronizedInventory)
    }
}
