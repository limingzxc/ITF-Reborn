package net.oilcake.mitelros.client.render;

import net.minecraft.*;

public class RenderHusk extends RenderBiped {
  private ModelBiped r;
  
  public RenderHusk() {
    super((ModelBiped)new ModelZombie(), 0.5F, 1.0F);
    this.r = this.modelBipedMain;
  }
  
  protected void setTextures() {
    setTexture(0, "textures/entity/zombie/husk");
  }
  
  protected ResourceLocation func_110856_a(EntityLiving par1EntityLiving) {
    return this.textures[0];
  }
  
  protected ResourceLocation getEntityTexture(Entity par1Entity) {
    return func_110856_a((EntityLiving)par1Entity);
  }
}
