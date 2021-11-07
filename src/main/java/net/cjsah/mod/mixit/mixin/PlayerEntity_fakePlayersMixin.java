package net.cjsah.mod.mixit.mixin;

import net.cjsah.mod.mixit.patch.EntityPlayerMPFake;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = PlayerEntity.class, remap = false)
public abstract class PlayerEntity_fakePlayersMixin
{
    /**
     * To make sure player attacks are able to knockback fake players
     */
    @Redirect(
            method = "attackTargetEntityWithCurrentItem",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/Entity;velocityChanged:Z",
                    ordinal = 0
            )
    )
    private boolean velocityModifiedAndNotCarpetFakePlayer(Entity target)
    {
        return target.velocityChanged && !(target instanceof EntityPlayerMPFake);
    }
}
