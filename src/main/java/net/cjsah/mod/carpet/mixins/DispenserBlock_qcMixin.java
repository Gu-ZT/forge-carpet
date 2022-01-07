package net.cjsah.mod.carpet.mixins;

import net.cjsah.mod.carpet.CarpetSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DispenserBlock.class)
public class DispenserBlock_qcMixin
{
    @Redirect(method = "neighborUpdate", at = @At(
            value = "INVOKE",
            target =  "Lnet/minecraft/world/World;isReceivingRedstonePower(Lnet/minecraft/util/math/BlockPos;)Z",
            ordinal = 1
    ))
    private boolean checkUpPower(Level world, BlockPos blockPos_1)
    {
        if (!CarpetSettings.quasiConnectivity)
            return false;
        return world.hasNeighborSignal(blockPos_1);
    }
}
