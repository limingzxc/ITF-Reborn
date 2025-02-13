package net.oilcake.mitelros.mixins.world;

import net.minecraft.Block;
import net.minecraft.World;
import net.minecraft.WorldGenMinable;
import net.oilcake.mitelros.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(WorldGenMinable.class)
public abstract class WorldGenMinableMixin {
    @Shadow
    private int minableBlockId;

    @Shadow
    public abstract int getMinVeinHeight(World world);

    @Shadow
    public abstract int getMaxVeinHeight(World world);

    @Inject(method = "getMinVeinHeight", at = @At(value = "INVOKE", target = "Lnet/minecraft/Minecraft;setErrorMessage(Ljava/lang/String;)V"), cancellable = true)
    private void inject(World world, CallbackInfoReturnable<Integer> cir) {
        Block block = Block.blocksList[this.minableBlockId];
        if (block == Blocks.oreNickel)
            cir.setReturnValue(0);
        if (block == Blocks.blockAzurite)
            cir.setReturnValue(32);
        if (block == Blocks.oreTungsten)
            cir.setReturnValue(0);
    }

    @Inject(method = "getMaxVeinHeight", at = @At(value = "INVOKE", target = "Lnet/minecraft/Minecraft;setErrorMessage(Ljava/lang/String;)V"), cancellable = true)
    private void inject_1(World world, CallbackInfoReturnable<Integer> cir) {
        Block block = Block.blocksList[this.minableBlockId];
        if (block == Blocks.oreNickel)
            cir.setReturnValue(48);
        if (block == Blocks.blockAzurite)
            cir.setReturnValue(96);
        if (block == Blocks.oreTungsten)
            cir.setReturnValue(32);
    }

    @Inject(method = "getRandomVeinHeight", at = @At(value = "INVOKE", target = "Lnet/minecraft/Minecraft;setErrorMessage(Ljava/lang/String;)V"), cancellable = true)
    private void inject_2(World world, Random rand, CallbackInfoReturnable<Integer> cir) {
        Block block = Block.blocksList[this.minableBlockId];
        float relative_height = 0;
        if (block == Blocks.oreNickel) {
            do {
                relative_height = rand.nextFloat();
            } while (relative_height >= rand.nextFloat());
        } else if (block == Blocks.oreTungsten) {
            do {
                relative_height = rand.nextFloat();
            } while (relative_height >= rand.nextFloat());
        } else if (block == Blocks.blockAzurite) {
            do {
                relative_height = rand.nextFloat();
            } while (!(relative_height < rand.nextFloat()));
        }
        if (world.isUnderworld()) {
            if (world.underworld_y_offset != 0) {
                if (block == Blocks.oreTungsten) {
                    cir.setReturnValue(rand.nextInt(16 + world.underworld_y_offset));
                }
            }
        }
        int min_height = this.getMinVeinHeight(world);
        int height_range = this.getMaxVeinHeight(world) - min_height + 1;
        cir.setReturnValue(min_height + (int) (relative_height * (float) height_range));
    }
}
