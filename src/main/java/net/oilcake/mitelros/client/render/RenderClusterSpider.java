package net.oilcake.mitelros.client.render;

import net.minecraft.EntityLivingBase;
import net.minecraft.ModelArachnid;
import net.minecraft.RenderArachnid;
import org.lwjgl.opengl.GL11;

public class RenderClusterSpider extends RenderArachnid {
  private float scale;
  
  protected boolean forceGlowOverride() {
    return true;
  }
  
  public RenderClusterSpider(float scale) {
    super(new ModelArachnid(), new ModelArachnid(), scale);
    this.scale = scale;
  }
  
  protected void preRenderCallback(EntityLivingBase par1EntityLivingBase, float par2) {
    super.preRenderCallback(par1EntityLivingBase, par2);
    GL11.glScalef(this.scale, this.scale, this.scale);
  }
  
  public String getSubtypeName() {
    return "cluster_spider";
  }
}
