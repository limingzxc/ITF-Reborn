package net.oilcake.mitelros.mixins.entity.animal;

import net.minecraft.DamageSource;
import net.minecraft.EntityDireWolf;
import net.minecraft.EntityWolf;
import net.minecraft.World;
import net.oilcake.mitelros.item.Items;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityDireWolf.class)
public class EntityDireWolfMixin extends EntityWolf {
    @Override
    protected void dropFewItems(boolean recently_hit_by_player, DamageSource damage_source) {
        int looting = damage_source.getLootingModifier();
        for (int i = 0; i < 1 + this.rand.nextInt(2 + looting); i++)
            dropItem(Items.wolf_fur.itemID, 1);
    }

    public EntityDireWolfMixin(World par1World) {
        super(par1World);
    }
}
