package net.oilcake.mitelros.mixins.world.biome;

import net.minecraft.BiomeGenBase;
import net.minecraft.BiomeGenJungle;
import net.minecraft.EntityChicken;
import net.oilcake.mitelros.config.ITFConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({BiomeGenJungle.class})
public class BiomeJungleMixin extends BiomeGenBase {
  protected BiomeJungleMixin(int par1) {
    super(par1);
  }
  
  @Inject(method = {"<init>(I)V"}, at = {@At("RETURN")})
  public void injectCtor(CallbackInfo callbackInfo) {
    if (((Boolean) ITFConfig.TagApocalypse.get()).booleanValue())
      removeEntityFromSpawnableLists(EntityChicken.class); 
  }
}
