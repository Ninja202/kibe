package io.github.lucaargolo.kibe.blocks.trashcan

import io.github.lucaargolo.kibe.blocks.getEntityType
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.collection.DefaultedList
import net.minecraft.inventory.Inventories
import net.minecraft.nbt.CompoundTag
import net.minecraft.block.BlockState

class TrashCanEntity(trashCan: TrashCan): LockableContainerBlockEntity(getEntityType(trashCan)) {

    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(1, ItemStack.EMPTY)

    override fun createScreenHandler(i: Int, playerInventory: PlayerInventory?): ScreenHandler? {
        return null
    }

    override fun size() = inventory.size


    override fun isEmpty() = inventory.all { it.isEmpty }

    override fun getStack(slot: Int): ItemStack {
        return inventory[slot]
    }

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

    override fun setStack(slot: Int, stack: ItemStack?) {
        if (stack?.getEnchantments()?.toString()?.contains("trash_safe") == true) {
            inventory[slot] = stack
        }
        else {
            inventory[slot] = ItemStack.EMPTY
        }
    }

    override fun clear() {
        inventory.clear()
    }

    override fun getContainerName(): Text = TranslatableText("screen.kibe.trash_can")

    override fun canPlayerUse(player: PlayerEntity?): Boolean {
        return if (world!!.getBlockEntity(pos) != this) {
            false
        } else {
            player!!.squaredDistanceTo(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5) <= 64.0
        }
    }

    override fun getDisplayName(): Text {
        return TranslatableText(getCachedState().getBlock().getTranslationKey())
    }

    override fun fromTag(state: BlockState, tag: CompoundTag) {
        super.fromTag(state, tag)
        inventory = DefaultedList.ofSize(1, ItemStack.EMPTY)
        Inventories.fromTag(tag, this.inventory)
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        super.toTag(tag)
        Inventories.toTag(tag, this.inventory)
        return tag
    }
}
