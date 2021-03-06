package io.github.lucaargolo.kibe.blocks.cooler

import io.github.lucaargolo.kibe.utils.BlockScreenHandlerFactory
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemPlacementContext
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World

class Cooler: BlockWithEntity(FabricBlockSettings.of(Material.METAL, MaterialColor.ICE).strength(0.2F).sounds(BlockSoundGroup.SNOW)) {

    override fun createBlockEntity(world: BlockView?) = CoolerBlockEntity(this)

    override fun appendProperties(stateManager: StateManager.Builder<Block?, BlockState?>) {
        stateManager.add(Properties.HORIZONTAL_FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(Properties.HORIZONTAL_FACING, ctx.playerFacing.opposite)
    }

    override fun onUse(state: BlockState?, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand?, hit: BlockHitResult?): ActionResult {
        player.openHandledScreen(BlockScreenHandlerFactory(this, pos))
        return ActionResult.SUCCESS
    }

    override fun getOutlineShape(state: BlockState, view: BlockView, pos: BlockPos, ePos: ShapeContext) = getShape(state[Properties.HORIZONTAL_FACING])

    override fun getCollisionShape(state: BlockState, view: BlockView, pos: BlockPos, ePos: ShapeContext) = getShape(state[Properties.HORIZONTAL_FACING])

    private fun getShape(facing: Direction): VoxelShape {
        return when(facing) {
            Direction.EAST, Direction.WEST -> createCuboidShape(5.0, 0.0, 1.0, 11.0, 12.0, 15.0)
            else -> createCuboidShape(1.0, 0.0, 5.0, 15.0, 12.0, 11.0)
        }
    }

    override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

}