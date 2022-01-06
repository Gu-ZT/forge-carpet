package net.cjsah.mod.carpet.mixins;

import net.cjsah.mod.carpet.CarpetServer;
import net.cjsah.mod.carpet.CarpetSettings;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Commands.class)
public abstract class CommandManagerMixin
{

    @Shadow
    @Final
    private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onRegister(Commands.CommandSelection arg, CallbackInfo ci) {
        CarpetServer.registerCarpetCommands(this.dispatcher);
        CarpetServer.registerCarpetCommands(this.dispatcher, arg);
    }

    @Inject(method = "execute", at = @At("HEAD"))
    private void onExecuteBegin(CommandSourceStack serverCommandSource_1, String string_1, CallbackInfoReturnable<Integer> cir)
    {
        if (!CarpetSettings.fillUpdates)
            CarpetSettings.impendingFillSkipUpdates.set(true);
    }

    @Inject(method = "execute", at = @At("RETURN"))
    private void onExecuteEnd(CommandSourceStack serverCommandSource_1, String string_1, CallbackInfoReturnable<Integer> cir)
    {
        CarpetSettings.impendingFillSkipUpdates.set(false);
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(method = "execute", at = @At(
                value = "INVOKE",
                target = "Lorg/apache/logging/log4j/Logger;isDebugEnabled()Z"
            ),
        require = 0
    )
    private boolean doesOutputCommandStackTrace(Logger logger)
    {
        if (CarpetSettings.superSecretSetting)
            return true;
        return logger.isDebugEnabled();
    }
}