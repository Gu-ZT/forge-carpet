package net.cjsah.mod.carpet.mixins;

import net.cjsah.mod.carpet.CarpetSettings;
import net.cjsah.mod.carpet.helpers.ThrowableSuppression;
import net.cjsah.mod.carpet.logging.LoggerRegistry;
import net.cjsah.mod.carpet.utils.Messenger;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServer_updateSuppressionCrashFixMixin {

    @Redirect(
            method = "tickWorlds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;tick(Ljava/util/function/BooleanSupplier;)V"
            ),
            require = 0
    )
    private void fixUpdateSuppressionCrashTick(ServerLevel serverWorld, BooleanSupplier shouldKeepTicking){
        if (!CarpetSettings.updateSuppressionCrashFix) {
            serverWorld.tick(shouldKeepTicking);
            return;
        }
        try {
            serverWorld.tick(shouldKeepTicking);
        } catch (ReportedException e) {
            if (!(e.getCause() instanceof ThrowableSuppression throwableSuppression)) throw e;
            logUpdateSuppression(throwableSuppression.pos);
        } catch (ThrowableSuppression e) {
            logUpdateSuppression(e.pos);
        }
    }


    private void logUpdateSuppression(BlockPos pos) {
        if(LoggerRegistry.__updateSuppressedCrashes) {
            LoggerRegistry.getLogger("updateSuppressedCrashes").log(() -> {
                return new BaseComponent[]{Messenger.c(
                        "w Server crash prevented in: ",
                        "m world tick ",
                        "w - at: ",
                        "g [ " + pos.toShortString() + " ]"
                )};
            });
        }
    }
}
