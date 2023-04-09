package net.cjsah.mod.carpet.mixin;

import net.cjsah.mod.carpet.patch.EntityPlayerMPFake;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnderPearlEntity.class)
public abstract class ThrownEnderpearlEntity_fakePlayersMixin extends ProjectileItemEntity
{
    public ThrownEnderpearlEntity_fakePlayersMixin(EntityType<? extends ProjectileItemEntity> entityType_1, World world_1)
    {
        super(entityType_1, world_1);
    }

    @Redirect(method =  "onHit", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/NetworkManager;isConnected()Z"
    ))
    private boolean isConnectionGood(NetworkManager networkManager)
    {
        return networkManager.isConnected() || getOwner() instanceof EntityPlayerMPFake;
    }
}
