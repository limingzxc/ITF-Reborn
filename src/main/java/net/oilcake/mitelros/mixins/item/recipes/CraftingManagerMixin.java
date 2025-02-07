package net.oilcake.mitelros.mixins.item.recipes;

import net.minecraft.*;
import net.oilcake.mitelros.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin({CraftingManager.class})
public class CraftingManagerMixin {
    @Shadow
    private List recipes = new ArrayList();

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void checkRecipe(Item item, int subtype_or_0) {
        if ((item.isCraftingProduct() || item.isRepairable()) && item.getLowestCraftingDifficultyToProduce() == Float.MAX_VALUE) {
            if (item.hasMaterial(Material.rusted_iron)) {
                Object peer;
                if (item instanceof ItemArmor) {
                    ItemArmor var10000 = (ItemArmor) item;
                    peer = ItemArmor.getMatchingArmor(item.getClass(), Material.copper, ((ItemArmor) item).isChainMail());
                } else {
                    peer = Item.getMatchingItem(item.getClass(), Material.copper);
                }
                if (peer != null)
                    item.setLowestCraftingDifficultyToProduce(((Item) peer).getLowestCraftingDifficultyToProduce());
            }
            if (item.getLowestCraftingDifficultyToProduce() == Float.MAX_VALUE) ;
        }
        if (item.isCraftingComponent(subtype_or_0) && item.getCraftingDifficultyAsComponent(new ItemStack(item, 1, subtype_or_0)) < 0.0F) {
            float lowest_crafting_difficulty_to_produce = item.getLowestCraftingDifficultyToProduce();
            if (lowest_crafting_difficulty_to_produce != Float.MAX_VALUE)
                item.setCraftingDifficultyAsComponent(lowest_crafting_difficulty_to_produce);
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public CraftingResult findMatchingRecipe(InventoryCrafting par1InventoryCrafting, World par2World, EntityPlayer player) {
        ItemStack item_stack_sinew, item_stack_armor;
        if (player != null && player.openContainer != null)
            player.openContainer.repair_fail_condition = 0;
        int var3 = 0;
        ItemStack var4 = null;
        ItemStack var5 = null;
        int var6;
        for (var6 = 0; var6 < par1InventoryCrafting.getSizeInventory(); var6++) {
            item_stack_sinew = par1InventoryCrafting.getStackInSlot(var6);
            if (item_stack_sinew != null) {
                if (var3 == 0)
                    var4 = item_stack_sinew;
                if (var3 == 1)
                    var5 = item_stack_sinew;
                var3++;
            }
        }
        if (var3 == 2 && var4.itemID == var5.itemID && var4.stackSize == 1 && var5.stackSize == 1 && Item.itemsList[var4.itemID].isRepairable()) {
            if (var4.isItemDamaged() && var5.isItemDamaged()) {
                if (var4.getQuality() != var5.getQuality())
                    return null;
                if (!var4.isItemEnchanted() && !var5.isItemEnchanted()) {
                    if (var4.isDyed() || var5.isDyed()) {
                        if (!var4.isDyed() || !var5.isDyed())
                            return null;
                        if (var4.getDyedColor() != var5.getDyedColor())
                            return null;
                    }
                    float crafting_difficulty = var4.getItem().getLowestCraftingDifficultyToProduce();
                    if (var4.getItem().hasQuality() && player != null && var4.getQuality().isHigherThan(player.getMaxCraftingQuality(crafting_difficulty, var4.getItem(), var4.getItem().getSkillsetsThatCanRepairThis())))
                        player.openContainer.repair_fail_condition = 1;
                    item_stack_armor = (new ItemStack(var4.itemID, 1, var4.getItemSubtype())).setItemDamage(CraftingManager.getResultingDurabilityFromCombiningItems(var4, var5));
                    if (var4.isDyed())
                        item_stack_armor.copyDyedColor(var4);
                    CraftingResult craftingResult = (new CraftingResult(item_stack_armor, crafting_difficulty / 2.0F, var4.getItem().getSkillsetsThatCanRepairThis(), (IRecipe) null)).setExperienceCostExempt().setQualityOverride(var4.getQuality());
                    craftingResult.setRepair();
                    return craftingResult;
                }
                return null;
            }
            return null;
        }
        if (var3 != 2 || (var4.getItem() != Item.sinew && var5.getItem() != Item.sinew && var4.getItem() != Item.silk && var5.getItem() != Item.silk && var4.getItem() != Items.wolf_fur && var5.getItem() != Items.wolf_fur) || ((!(var4.getItem() instanceof ItemArmor) || !((ItemArmor) var4.getItem()).isLeather() || var4.stackSize != 1 || !var4.isItemDamaged()) && (!(var5.getItem() instanceof ItemArmor) || !((ItemArmor) var5.getItem()).isLeather() || var5.stackSize != 1 || !var5.isItemDamaged()))) {
            Container event_handler = par1InventoryCrafting.getEventHandler();
            for (var6 = 0; var6 < this.recipes.size(); var6++) {
                IRecipe var12 = (IRecipe) this.recipes.get(var6);
                if (var12.matches(par1InventoryCrafting, par2World) && (!(event_handler instanceof MITEContainerCrafting) || !((MITEContainerCrafting) event_handler).isRecipeForbidden(var12))) {
                    CraftingResult craftingResult = var12.getCraftingResult(par1InventoryCrafting);
                    if (craftingResult == null)
                        return null;
                    return (event_handler instanceof MITEContainerCrafting && ((MITEContainerCrafting) event_handler).isCraftingResultForbidden(craftingResult)) ? null : craftingResult;
                }
            }
            return null;
        }
        if (var4.getItem() != Item.sinew && var4.getItem() != Item.silk && var4.getItem() != Items.wolf_fur) {
            item_stack_sinew = var5;
            item_stack_armor = var4;
        } else {
            item_stack_sinew = var4;
            item_stack_armor = var5;
        }
        if (item_stack_armor.getItem().hasQuality() && player != null && item_stack_armor.getQuality().isHigherThan(player.getMaxCraftingQuality(item_stack_armor.getItem().getLowestCraftingDifficultyToProduce(), item_stack_armor.getItem(), item_stack_armor.getItem().getSkillsetsThatCanRepairThis())))
            return null;
        int damage = item_stack_armor.getItemDamage();
        int damage_repaired_per_sinew = item_stack_armor.getMaxDamage() / item_stack_armor.getItem().getRepairCost();
        int num_sinews_to_use = damage / damage_repaired_per_sinew;
        if (damage % damage_repaired_per_sinew != 0)
            num_sinews_to_use++;
        if (num_sinews_to_use > 1 && num_sinews_to_use * damage_repaired_per_sinew > damage)
            num_sinews_to_use--;
        if (num_sinews_to_use > item_stack_sinew.stackSize)
            num_sinews_to_use = item_stack_sinew.stackSize;
        int damage_repaired = num_sinews_to_use * damage_repaired_per_sinew;
        int damage_after_repair = Math.max(damage - damage_repaired, 0);
        ItemStack resulting_stack = item_stack_armor.copy().setItemDamage(damage_after_repair);
        CraftingResult crafting_result = (new CraftingResult(resulting_stack, (num_sinews_to_use * 50), item_stack_armor.getItem().getSkillsetsThatCanRepairThis(), (IRecipe) null)).setExperienceCostExempt().setQualityOverride(item_stack_armor.getQuality()).setConsumption(num_sinews_to_use);
        crafting_result.setRepair();
        return crafting_result;
    }
}
