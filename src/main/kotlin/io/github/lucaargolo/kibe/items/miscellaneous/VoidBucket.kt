package io.github.lucaargolo.kibe.items.miscellaneous

import io.github.lucaargolo.kibe.items.VOID_BUCKET
import io.github.lucaargolo.kibe.utils.FakePlayerEntity
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.block.FluidDrainable
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluids
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.stat.Stats
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.RaycastContext
import net.minecraft.world.World

class VoidBucket(settings: Settings): Item(settings) {

    override fun use(world: World, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack> {
        val itemStack = user.getStackInHand(hand)
        val hitResult: HitResult = raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY)

        return (hitResult as? BlockHitResult)?.let { blockHitResult ->
            val dir = blockHitResult.side
            val pos = blockHitResult.blockPos
            val offsetPos = pos.offset(dir)

            if(fakeInteraction(world, pos, blockHitResult)) {
                return TypedActionResult.success(itemStack)
            }

            if (world.canPlayerModifyAt(user, pos) && user.canPlaceOn(offsetPos, dir, itemStack)) {
                val blockState = world.getBlockState(pos)
                if (blockState.block is FluidDrainable) {
                    val fluid = (blockState.block as FluidDrainable).tryDrainFluid(world, pos, blockState)
                    if(fluid != Fluids.EMPTY) {
                        user.incrementStat(Stats.USED.getOrCreateStat(this))
                        user.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0f, 1.0f)
                        if (!world.isClient) {
                            Criteria.FILLED_BUCKET.trigger(user as ServerPlayerEntity, ItemStack(VOID_BUCKET))
                        }
                        return TypedActionResult.success(itemStack)
                    }
                }
            }

            TypedActionResult.fail(itemStack)

        } ?: TypedActionResult.pass(itemStack)
    }

    private fun fakeInteraction(world: World, pos: BlockPos, blockHitResult: BlockHitResult): Boolean {
        val fakePlayer = FakePlayerEntity(world)
        fakePlayer.setStackInHand(Hand.MAIN_HAND, ItemStack(Items.BUCKET))
        val blockState = world.getBlockState(pos)
        val block = blockState.block
        block.onUse(blockState, world, pos, fakePlayer, Hand.MAIN_HAND, blockHitResult)
        val resultStack = fakePlayer.getStackInHand(Hand.MAIN_HAND)
        val resultItem = resultStack.item
        return resultItem != Items.BUCKET
    }

}