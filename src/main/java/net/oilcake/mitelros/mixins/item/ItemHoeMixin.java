package net.oilcake.mitelros.mixins.item;

import net.minecraft.*;
import net.oilcake.mitelros.block.Blocks;
import net.oilcake.mitelros.item.Materials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemHoe.class})
public class ItemHoeMixin extends ItemTool {
  protected ItemHoeMixin(int par1, Material material) {
    super(par1, material);
  }
  
  @Inject(method = {"<init>(ILnet/minecraft/Material;)V"}, at = {@At("RETURN")})
  public void injectCtor(CallbackInfo callbackInfo) {
    addMaterialsEffectiveAgainst(new Material[] { Materials.beetroot });
    addBlocksEffectiveAgainst(new Block[] { Blocks.beetroots });
  }
  
  @Shadow
  public float getBaseDamageVsEntity() {
    return 0.0F;
  }
  
  @Shadow
  public float getBaseDecayRateForBreakingBlock(Block block) {
    return 0.0F;
  }
  
  @Shadow
  public float getBaseDecayRateForAttackingEntity(ItemStack itemStack) {
    return 0.0F;
  }
  
  @Shadow
  public String getToolType() {
    return null;
  }
  
  @Shadow
  public int getNumComponentsForDurability() {
    return 0;
  }
}
