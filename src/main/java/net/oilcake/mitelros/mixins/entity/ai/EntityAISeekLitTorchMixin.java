package net.oilcake.mitelros.mixins.entity.ai;

import net.minecraft.EntityAIBase;
import net.minecraft.EntityAISeekLitTorch;
import net.oilcake.mitelros.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EntityAISeekLitTorch.class)
public abstract class EntityAISeekLitTorchMixin extends EntityAIBase {
    @ModifyArg(method = "findPathToLitTorch", at = @At(value = "INVOKE", target = "Lnet/minecraft/World;getNearestBlockCandidates(DDDIII[I[I[I[I[D)I"), index = 6)
    private int[] addTorches(int[] block_ids) {
        int[] temp = new int[block_ids.length + 1];
        System.arraycopy(block_ids, 0, temp, 0, block_ids.length);
        temp[block_ids.length] = Blocks.torchWoodIdle.blockID;
        return temp;
    }
}
