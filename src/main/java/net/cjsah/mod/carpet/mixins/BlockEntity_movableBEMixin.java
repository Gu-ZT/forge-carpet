package net.cjsah.mod.carpet.mixins;

import net.cjsah.mod.carpet.fakes.BlockEntityInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockEntity.class)
public abstract class BlockEntity_movableBEMixin implements BlockEntityInterface
{
    @Mutable
    @Shadow @Final protected BlockPos pos;

    public void setCMPos(BlockPos newPos)
    {
        pos = newPos;
    };
}
