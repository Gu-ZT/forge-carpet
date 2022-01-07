package net.cjsah.mod.carpet.mixins;

import net.cjsah.mod.carpet.fakes.ThreadedAnvilChunkStorageInterface;
import net.cjsah.mod.carpet.helpers.TickSpeed;
import net.cjsah.mod.carpet.utils.CarpetProfiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import com.google.common.collect.Lists;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkManager_tickMixin
{

    @Shadow @Final private ServerLevel world;

    @Shadow @Final
    public ChunkMap threadedAnvilChunkStorage;

    CarpetProfiler.ProfilerToken currentSection;

    @Inject(method = "tickChunks", at = @At("HEAD"))
    private void startSpawningSection(CallbackInfo ci)
    {
        currentSection = CarpetProfiler.start_section(world, "Spawning and Random Ticks", CarpetProfiler.TYPE.GENERAL);
    }

    @Inject(method = "tickChunks", at = @At("RETURN"))
    private void stopSpawningSection(CallbackInfo ci)
    {
        if (currentSection != null)
        {
            CarpetProfiler.end_current_section(currentSection);
        }
    }

    //// Tick freeze
    @Redirect(method = "tickChunks", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerWorld;isDebugWorld()Z"
    ))
    private boolean skipChunkTicking(ServerLevel serverWorld)
    {
        boolean debug = serverWorld.isDebug();
        if (!TickSpeed.process_entities)
        {
            // simplified chunk tick iteration assuming world is frozen otherwise as suggested by Hadron67
            // to be kept in sync with the original injection source
            if (!debug){
                List<ChunkHolder> holders = Lists.newArrayList(((ThreadedAnvilChunkStorageInterface)threadedAnvilChunkStorage).getChunksCM());
                Collections.shuffle(holders);
                for (ChunkHolder holder: holders){
                    Optional<LevelChunk> optional = holder.getTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left();
                    if (optional.isPresent()){
                        holder.broadcastChanges(optional.get());
                    }
                }
            }
            return true;
        }
        return debug;
    }

}
