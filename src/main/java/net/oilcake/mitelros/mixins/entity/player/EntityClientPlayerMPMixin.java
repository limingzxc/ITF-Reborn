package net.oilcake.mitelros.mixins.entity.player;

import net.minecraft.*;
import net.oilcake.mitelros.api.ITFClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityClientPlayerMP.class)
public abstract class EntityClientPlayerMPMixin extends ClientPlayer implements ITFClientPlayer {

    @Unique
    private int phytonutrients;
    @Unique
    private int protein;

    public EntityClientPlayerMPMixin(Minecraft par1Minecraft, World par2World, Session par3Session, NetClientHandler par4NetClientHandler) {
        super(par1Minecraft, par2World, par3Session, 0);
    }

    @Unique
    public int getPhytonutrients() {
        return this.phytonutrients;
    }

    @Unique
    public void setPhytonutrients(int phytonutrients) {
        this.phytonutrients = phytonutrients;
    }

    @Unique
    public int getProtein() {
        return this.protein;
    }

    @Unique
    public void setProtein(int protein) {
        this.protein = protein;
    }
}
