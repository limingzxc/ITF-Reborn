package net.oilcake.mitelros.mixins.item;

import net.minecraft.*;
import net.oilcake.mitelros.item.Items;
import net.oilcake.mitelros.item.Materials;
import net.oilcake.mitelros.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

import static net.minecraft.ItemBow.getFractionPulled;

@Mixin(ItemBow.class)
public class ItemBowMixin {
    @Inject(method = "<clinit>", at = @At("FIELD"))
    private static void injectClinit(CallbackInfo callback) {
        possible_arrow_materials = new Material[]{Material.flint, Material.obsidian, Material.copper, Material.silver, Material.rusted_iron, Material.gold, Material.iron, Material.mithril, Material.adamantium, Material.ancient_metal,Materials.nickel,Materials.tungsten,Materials.magical};
    }
    @Final
    @Shadow
    @Mutable
    private static Material[] possible_arrow_materials;

    @Overwrite
    public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down) {
        if (!player.inCreativeMode() && player.inventory.getReadiedArrow() == null && !(EnchantmentManager.hasEnchantment(player.getHeldItemStack(), Enchantments.enchantmentInfinity))) {
            return false;
        } else {
            player.nocked_arrow = player.inventory.getReadiedArrow();
            if (player.nocked_arrow == null && player.inCreativeMode() || (EnchantmentManager.hasEnchantment(player.getHeldItemStack(), Enchantments.enchantmentInfinity))) {
                player.nocked_arrow = Items.arrowMagical;
            }

            if (player.onServer()) {
                player.sendPacketToAssociatedPlayers((new Packet85SimpleSignal(EnumSignal.nocked_arrow)).setShort(player.nocked_arrow.itemID).setEntityID(player), false);
            }

            player.setHeldItemInUse();
            return true;
        }
    }
    @Overwrite
    public void onPlayerStoppedUsing(ItemStack item_stack, World world, EntityPlayer player, int item_in_use_count) {
        if (!world.isRemote) {
            ItemArrow arrow = player.inventory.getReadiedArrow();
            if (arrow == null) {
//                if (!player.inCreativeMode()) {
//                    return;
//                }
                arrow = player.nocked_arrow;
            }

            float fraction_pulled = getFractionPulled(item_stack, item_in_use_count);
            fraction_pulled = (fraction_pulled * fraction_pulled + fraction_pulled * 2.0F) / 3.0F;
            if (!(fraction_pulled < 0.1F)) {
                if (fraction_pulled > 1.0F) {
                    fraction_pulled = 1.0F;
                }

                EntityArrow entity_arrow = new EntityArrow(world, player, fraction_pulled * 2.0F, arrow, item_stack.isItemEnchanted());
                player.nocked_arrow = null;
                if (fraction_pulled == 1.0F) {
                    entity_arrow.setIsCritical(true);
                }

                int power = EnchantmentManager.getEnchantmentLevel(Enchantment.power.effectId, item_stack);
                if (power > 0) {
                    entity_arrow.setDamage(entity_arrow.getDamage() + (double)((float)power * 0.5F) + 0.5);
                }

                int punch = EnchantmentManager.getEnchantmentLevel(Enchantment.punch.effectId, item_stack);
                if (punch > 0) {
                    entity_arrow.setKnockbackStrength(punch);
                }

                if (EnchantmentManager.getEnchantmentLevel(Enchantment.flame.effectId, item_stack) > 0) {
                    entity_arrow.setFire(100);
                }

                player.tryDamageHeldItem(DamageSource.generic, 1);
                Random rand = new Random();
                world.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 1.2F) + fraction_pulled * 0.5F);
                if (player.inCreativeMode()) {
                    entity_arrow.canBePickedUp = 2;
                } else {
                    player.inventory.consumeArrow();
                }

                if (!world.isRemote) {
                    world.spawnEntityInWorld(entity_arrow);
                }

            }
        }
    }
}
