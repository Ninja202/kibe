package io.github.lucaargolo.kibe.mixin;

import io.github.lucaargolo.kibe.blocks.miscellaneous.Elevator;
import io.github.lucaargolo.kibe.items.ItemCompendiumKt;
import io.github.lucaargolo.kibe.items.miscellaneous.Glider;
import io.github.lucaargolo.kibe.items.miscellaneous.SleepingBag;
import io.github.lucaargolo.kibe.utils.SlimeBounceHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow public abstract BlockState getBlockState();

    @Shadow public abstract ItemStack getStackInHand(Hand hand);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At("HEAD"), method = "swingHand(Lnet/minecraft/util/Hand;)V", cancellable = true)
    private void swingHand(Hand hand, CallbackInfo info) {
        ItemStack stack = this.getStackInHand(hand);
        if(stack.getItem() instanceof Glider && Glider.Companion.isEnabled(stack)) {
            info.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "isSleepingInBed", cancellable = true)
    private void isSleepingInBed(CallbackInfoReturnable<Boolean> info) {
        if(SleepingBag.Companion.getPlayersSleeping().contains(this)) {
            info.setReturnValue(true);
        }
    }

    @Inject(at = @At("HEAD"), method = "jump", cancellable = true)
    private void jump(CallbackInfo info) {
        BlockPos pos = this.getBlockPos();
        Block block = this.world.getBlockState(pos.down()).getBlock();
        if (block instanceof Elevator && world.getBlockState(pos).getCollisionShape(world, pos).isEmpty()) {
            while(pos.getY() < this.world.getHeight()) {
                if(world.getBlockState(pos.up()).getBlock().equals(block) && Elevator.Companion.isElevatorValid(world, pos.up())) {
                    world.playSound(null, pos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.25F + 0.6F);
                    this.teleport(this.getPos().x, pos.up().getY()+1.15, this.getPos().z);
                    break;
                }else{
                    pos = pos.up();
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "handleFallDamage", cancellable = true)
    private void handleFallDamage(float fallDistance, float damageMultiplier, CallbackInfoReturnable<Boolean> info) {
        if((Object) this instanceof PlayerEntity) {
            PlayerEntity player = ((PlayerEntity) ((Object) this));
            if (player.getEquippedStack(EquipmentSlot.FEET).getItem() == ItemCompendiumKt.getSLIME_BOOTS()) {
                if(!isSneaking() && fallDistance > 2) {
                    this.fallDistance = 0;

                    if(world.isClient) {
                        setVelocity(getVelocity().x, getVelocity().y*-0.9, getVelocity().z);
                        velocityDirty = true;
                        onGround = false;
                        double f = 0.91d + 0.04d;
                        setVelocity(getVelocity().x/f, getVelocity().y, getVelocity().z/f);
                    }else{
                        info.cancel();
                    }

                    this.playSound(SoundEvents.ENTITY_SLIME_SQUISH, 1f, 1f);
                    SlimeBounceHandler.Companion.addBounceHandler(player, getVelocity().y);

                }else if(!world.isClient && isSneaking()) {
                    if(fallDistance > 5) this.fallDistance = 5;
                }
            }
        }

    }

}
