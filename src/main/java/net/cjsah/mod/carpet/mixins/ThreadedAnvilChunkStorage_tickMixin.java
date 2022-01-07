package net.cjsah.mod.carpet.mixins;

import net.cjsah.mod.carpet.utils.CarpetProfiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;

@Mixin(ChunkMap.class)
public class ThreadedAnvilChunkStorage_tickMixin
{
    @Shadow @Final private ServerLevel world;
    CarpetProfiler.ProfilerToken currentSection;

    @Inject(method = "tick", at = @At("HEAD"))
    private void startProfilerSection(BooleanSupplier booleanSupplier_1, CallbackInfo ci)
    {
        currentSection = CarpetProfiler.start_section(world, "Unloading", CarpetProfiler.TYPE.GENERAL);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void stopProfilerSecion(BooleanSupplier booleanSupplier_1, CallbackInfo ci)
    {
        if (currentSection != null)
        {
            CarpetProfiler.end_current_section(currentSection);
        }
    }



}
