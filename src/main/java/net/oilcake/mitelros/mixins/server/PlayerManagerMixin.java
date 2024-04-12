package net.oilcake.mitelros.mixins.server;

import net.minecraft.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.PrintStream;

@Mixin({PlayerManager.class})
public class PlayerManagerMixin {
    @Redirect(method = {"updateMountedMovingPlayer(Lnet/minecraft/ServerPlayer;)V"}, at = @At(value = "INVOKE", target = "Ljava/io/PrintStream;println(Ljava/lang/String;)V"))
    public void removeInfo(PrintStream printStream, String x) {
    }
}
