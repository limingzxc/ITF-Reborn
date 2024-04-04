package net.oilcake.mitelros.mixins.entity.player;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.*;
import net.minecraft.server.MinecraftServer;
import net.oilcake.mitelros.achivements.AchievementExtend;
import net.oilcake.mitelros.api.*;
import net.oilcake.mitelros.block.enchantreserver.EnchantReserverSlots;
import net.oilcake.mitelros.enchantment.Enchantments;
import net.oilcake.mitelros.item.ItemTotem;
import net.oilcake.mitelros.item.Items;
import net.oilcake.mitelros.item.Materials;
import net.oilcake.mitelros.item.potion.PotionExtend;
import net.oilcake.mitelros.status.*;
import net.oilcake.mitelros.util.Config;
import net.oilcake.mitelros.util.Constant;
import net.oilcake.mitelros.util.CurseExtend;
import net.xiaoyu233.fml.util.ReflectHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityLivingBase implements ICommandSender, ITFPlayer {

    @Shadow
    public abstract void triggerAchievement(StatBase par1StatBase);

    public NewPlayerManager newPlayerManager = new NewPlayerManager();

    @Override
    public NewPlayerManager getNewPlayerManager() {
        return newPlayerManager;
    }

    public DiarrheaManager diarrheaManager = new DiarrheaManager();

    public DiarrheaManager getDiarrheaManager() {
        return diarrheaManager;
    }

    @Shadow
    public EnumInsulinResistanceLevel insulin_resistance_level;

    @Shadow
    private int field_82249_h;

    @Shadow
    public PlayerCapabilities capabilities;

    private final HuntManager huntManager = new HuntManager();

    @Override
    public HuntManager getHuntManager() {
        return huntManager;
    }

    public float getBodyTemperature() {
        return BodyTemperature;
    }

    private int HeatResistance;

    private int FreezingCooldown;

    private int FreezingWarning;

    public int getCurrent_insulin_resistance_lvl() {
        if (this.insulin_resistance_level == null)
            return 0;
        return this.insulin_resistance_level.ordinal() + 1;
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void cancel(CallbackInfo ci) {
        if ((getHealth() > 5.0F && this.capabilities.getWalkSpeed() >= 0.05F && hasFoodEnergy()) || !Config.Realistic.get()) {
            return;
        }
        ci.cancel();
    }

    @Override
    public boolean isOnLadder() {
        if (Config.Realistic.get() && (getHealth() <= 5.0F || this.capabilities.getWalkSpeed() < 0.05F || !hasFoodEnergy())) {
            int x = MathHelper.floor_double(this.posX);
            int y = MathHelper.floor_double(this.boundingBox.minY);
            int z = MathHelper.floor_double(this.posZ);
            int var0 = this.worldObj.getBlockId(x, y, z);
            if (var0 == Block.ladder.blockID || var0 == Block.vine.blockID)
                return true;
            float yaw = this.rotationYaw % 360.0F;
            if (yaw < -45.0F)
                yaw += 360.0F;
            int towards = (int) ((yaw + 45.0F) % 360.0F) / 90;
            switch (towards) {
                case 0:
                    z++;
                    break;
                case 1:
                    x--;
                    break;
                case 2:
                    z--;
                    break;
                case 3:
                    x++;
                    break;
                default:
                    Minecraft.setErrorMessage("isOnLadder: Undefined Facing : " + towards + ".");
                    break;
            }
            Block block1 = this.worldObj.getBlock(x, y, z);
            Block block2 = this.worldObj.getBlock(x, y + 1, z);
            return ((this.fallDistance == 0.0F && block1 != null && block1.isSolid(0) && block2 == null) || (block2 != null && !block2.isSolid(0)));
        }
        return super.isOnLadder();
    }

    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/EntityPlayer;willDeliverCriticalStrike()Z"), cancellable = true)
    private void explosion(Entity target, CallbackInfo ci) {
        float damage = calcRawMeleeDamageVs(target, willDeliverCriticalStrike(), isSuspendedInLiquid());
        if (damage <= 0.0F) {
            ci.cancel();
            return;
        }
        ItemStack heldItemStack = getHeldItemStack();
        if (EnchantmentHelper.hasEnchantment(heldItemStack, Enchantments.enchantmentDestroying)) {
            int destorying = EnchantmentHelper.getEnchantmentLevel(Enchantments.enchantmentDestroying, heldItemStack);
            target.worldObj.createExplosion(this, target.posX, target.posY, target.posZ, 0.0F, destorying * 0.5F, true);
        }
    }

    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/Entity;attackEntityFrom(Lnet/minecraft/Damage;)Lnet/minecraft/EntityDamageResult;"))
    private void thresher(Entity target, CallbackInfo ci) {
        if (onServer() && target instanceof EntityLivingBase entity_living_base) {
            ItemStack[] item_stack_to_drop = entity_living_base.getWornItems();
            int rand = this.rand.nextInt(item_stack_to_drop.length);
            if (item_stack_to_drop[rand] != null && this.rand.nextFloat() < EnchantmentHelper.getEnchantmentLevelFraction(Enchantments.enchantmentThresher, getHeldItemStack()) && entity_living_base instanceof EntityLiving entity_living) {
                entity_living.dropItemStack(item_stack_to_drop[rand], entity_living.height / 2.0F);
                entity_living.clearMatchingEquipmentSlot(item_stack_to_drop[rand]);
                entity_living.ticks_disarmed = 40;
            }
        }
    }

    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/EntityPlayer;heal(FLnet/minecraft/EnumEntityFX;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void sweep(Entity target, CallbackInfo ci, boolean critical, float damage, int knockback, boolean was_set_on_fire, int fire_aspect, EntityDamageResult result, boolean target_was_harmed, int stunning) {
        ItemStack heldItemStack = getHeldItemStack();
        if (EnchantmentHelper.hasEnchantment(heldItemStack, Enchantments.enchantmentSweeping)) {
            List<Entity> targets = getNearbyEntities(5.0F, 5.0F);
            attackMonsters(targets, damage * EnchantmentHelper.getEnchantmentLevelFraction(Enchantments.enchantmentSweeping, heldItemStack));
        }
    }

    @ModifyConstant(method = "attackTargetEntityWithCurrentItem", constant = @org.spongepowered.asm.mixin.injection.Constant(floatValue = 18.0F))
    private float achievement(float constant) {
        return 40.0F;
    }

    @Inject(method = {"onDeath(Lnet/minecraft/DamageSource;)V"}, at = @At("TAIL"))
    public void onDeath(DamageSource par1DamageSource, CallbackInfo callbackInfo) {
        if (!this.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory"))
            ((ITFInventory) this.inventory).vanishingItems();
    }

    public int getWater() {
        return ((ITFFoodStats) getFoodStats()).getWater();
    }

    public int addWater(int water) {
        return ((ITFFoodStats) getFoodStats()).addWater(water);
    }

    public void decreaseWaterServerSide(float hungerWater) {
        if (!this.capabilities.isCreativeMode && !this.capabilities.disableDamage)
            ((ITFFoodStats) getFoodStats()).decreaseWaterServerSide(hungerWater);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean isStarving() {
        return (getNutrition() == 0);
    }

    public boolean DuringDehydration() {
        return (getWater() == 0);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean hasFoodEnergy() {
        return (getSatiation() + getNutrition() != 0 && getWater() != 0);
    }


    public boolean willRepair(ItemStack holding) {
        return EnchantmentHelper.hasEnchantment(holding, Enchantments.enchantmentMending);
    }

    private void activeNegativeUndying() {
        clearActivePotions();
        setHealth(getMaxHealth(), true, getHealFX());
        entityFX(EnumEntityFX.smoke_and_steam);
        makeSound("imported.random.totem_use", 3.0F, 1.0F + this.rand.nextFloat() * 0.1F);
        addPotionEffect(new PotionEffect(Potion.blindness.id, 40, 4));
        this.vision_dimming += 0.75F;
        triggerAchievement(AchievementExtend.cheatdeath);
    }

    protected void checkForAfterDamage(Damage damage, EntityDamageResult result) {
        if (result.entityWasDestroyed()) {
            ItemStack var5 = getHeldItemStack();
            if (var5 != null && var5.getItem() instanceof ItemTotem) {
                ((ITFDamageResult) result).setEntity_was_destroyed(false);
                ((ItemTotem) var5.getItem()).performNegativeEffect(this.getAsPlayer());
            }
            if (this.huntManager.hunt_counter > 0) {
                ((ITFDamageResult) result).setEntity_was_destroyed(false);
                setHealth(1.0F);
            }
        }
    }

    @Redirect(method = {"attackEntityFrom(Lnet/minecraft/Damage;)Lnet/minecraft/EntityDamageResult;"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/EntityLivingBase;attackEntityFrom(Lnet/minecraft/Damage;)Lnet/minecraft/EntityDamageResult;"))
    private EntityDamageResult redirectEntityAttack(EntityLivingBase caller, Damage damage) {
        EntityDamageResult entityDamageResult = super.attackEntityFrom(damage);
        if (entityDamageResult != null && getHealthFraction() <= 0.1D && !entityDamageResult.entityWasDestroyed()) {
            ItemStack var5 = getHeldItemStack();
            if (var5 != null && var5.getItem() instanceof ItemTotem) {
                ((ITFDamageResult) entityDamageResult).setEntity_was_destroyed(false);
                activeNegativeUndying();
                setHeldItemStack(null);
            }
        }
        return entityDamageResult;
    }

    public float BodyTemperature = 37.2F;

    @Shadow
    protected FoodStats foodStats;

    @Shadow
    public int experience;

    @Shadow
    public InventoryPlayer inventory;

    @Shadow
    public float vision_dimming;

    private MiscManager weightManager = new MiscManager(ReflectHelper.dyCast(this));

    public MiscManager getMiscManager() {
        return weightManager;
    }

    private WaterManager waterManager = new WaterManager();

    private TemperatureManager temperatureManager = new TemperatureManager(ReflectHelper.dyCast(this));

    public TemperatureManager getTemperatureManager() {
        return temperatureManager;
    }

    @Inject(method = {"onLivingUpdate()V"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/EntityLivingBase;onLivingUpdate()V", shift = At.Shift.AFTER)})
    private void injectTick(CallbackInfo ci) {
        if (!this.worldObj.isRemote) {
            this.diarrheaManager.update(ReflectHelper.dyCast(this));

            if (hasCurse(CurseExtend.fear_of_light)) {
                float light_modifier = (18 - this.worldObj.getBlockLightValue(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY + this.yOffset), MathHelper.floor_double(this.posZ))) / 15.0F;
                if (light_modifier >= 0.5F || hasCurse(CurseExtend.fear_of_light, true)) ;
            }
            if (hasCurse(CurseExtend.fear_of_darkness)) {
                float light_modifier = (this.worldObj.getBlockLightValue(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY + this.yOffset), MathHelper.floor_double(this.posZ)) + 3) / 15.0F;
                if (light_modifier >= 0.5F || hasCurse(CurseExtend.fear_of_darkness, true)) ;
            }
            this.huntManager.update(ReflectHelper.dyCast(this));
            if (Minecraft.inDevMode() && this.vision_dimming > 0.1F && isPlayerInCreative())
                this.vision_dimming = 0.05F;

            this.waterManager.update(ReflectHelper.dyCast(this));
            this.drunkManager.update1();
            this.temperatureManager.update();
            this.drunkManager.update2();

            if (getHealth() < 5.0F && Config.Realistic.get())
                this.vision_dimming = Math.max(this.vision_dimming, 1.0F - getHealthFraction());
        }

        this.feastManager.achievementCheck(ReflectHelper.dyCast(this));

        if (isPotionActive(Potion.moveSpeed) && isPotionActive(Potion.regeneration) && isPotionActive(Potion.fireResistance) && isPotionActive(Potion.nightVision) && isPotionActive(Potion.damageBoost) && isPotionActive(Potion.resistance) && isPotionActive(Potion.invisibility) && !this.feastManager.rewarded_disc_connected) {
            triggerAchievement(AchievementExtend.invincible);
            addExperience(2500);
            this.feastManager.rewarded_disc_connected = true;
            EntityItem RewardingRecord = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, new ItemStack(Items.recordConnected.itemID, 1));
            this.worldObj.spawnEntityInWorld(RewardingRecord);
            RewardingRecord.entityFX(EnumEntityFX.summoned);
        }
        if (this.getMiscManager().UnderArrogance()) {
            addPotionEffect(new PotionEffect(Potion.wither.id, 100, 1));
        }
        ItemStack holding = getHeldItemStack();
        if (holding != null && willRepair(holding) &&
                holding.getRemainingDurability() / holding.getMaxDamage() < 0.5F && getExperienceLevel() >= 10 + 15 * holding.getItem().getHardestMetalMaterial().min_harvest_level) {
            addExperience(-holding.getMaxDamage() / 32, false, true);
            holding.setItemDamage(holding.getItemDamage() - holding.getMaxDamage() / 8);
        }
        ItemStack[] item_stack_to_repair = getWornItems();
        for (int n = 0; n < item_stack_to_repair.length; n++) {
            if (item_stack_to_repair[n] != null && willRepair(item_stack_to_repair[n]) &&
                    item_stack_to_repair[n].getRemainingDurability() / item_stack_to_repair[n].getMaxDamage() < 0.5F && getExperienceLevel() >= 10 + 15 * item_stack_to_repair[n].getItem().getHardestMetalMaterial().min_harvest_level) {
                addExperience(-50, false, true);
                item_stack_to_repair[n].setItemDamage(item_stack_to_repair[n].getItemDamage() - 1);
            }
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void addExperience(int amount, boolean suppress_healing, boolean suppress_sound) {
        suppress_healing = true;
        if (amount < 0) {
            if (!suppress_sound)
                this.worldObj.playSoundAtEntity(this, "imported.random.level_drain");
        } else if (amount > 0) {
            addScore(amount);
            if (!suppress_sound)
                this.worldObj.playSoundAtEntity(this, "random.orb", 0.1F, 0.5F * ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.8F));
            ItemStack holding = getHeldItemStack();
            if (holding != null && willRepair(holding))
                for (; getHeldItemStack().getItemDamage() >= 4 && amount > 0; amount--)
                    getHeldItemStack().setItemDamage(holding.getItemDamage() - 4);
        }
        float health_limit_before = getHealthLimit();
        int level_before = getExperienceLevel();
        this.experience += amount;
        if (this.experience < getExperienceRequired(-40))
            this.experience = getExperienceRequired(-40);
        int level_after = getExperienceLevel();
        int level_change = level_after - level_before;
        if (level_change < 0) {
            setHealth(getHealth());
            this.foodStats.setSatiation(this.foodStats.getSatiation(), true);
            this.foodStats.setNutrition(this.foodStats.getNutrition(), true);
            addWater(0);
        } else if (level_change > 0) {
            if (getHealthLimit() > health_limit_before && this.field_82249_h < this.ticksExisted - 100.0F) {
                float volume = (level_after > 30) ? 1.0F : (level_after / 30.0F);
                if (!suppress_sound)
                    this.worldObj.playSoundAtEntity(this, "random.levelup", volume * 0.75F, 1.0F);
                this.field_82249_h = this.ticksExisted;
            }
            if (!suppress_healing)
                setHealth(getHealth() + getHealthLimit() - health_limit_before);
        }
        if (level_change != 0 && !this.worldObj.isRemote)
            MinecraftServer.getServerConfigurationManager(MinecraftServer.getServer()).sendPlayerInfoToAllPlayers(true);
    }

    public FoodStats getFoodStats() {
        return this.foodStats;
    }

    @Unique
    private FeastManager feastManager = new FeastManager();

    @Unique
    public FeastManager getFeastManager() {
        return feastManager;
    }

    @Unique
    private DrunkManager drunkManager = new DrunkManager(ReflectHelper.dyCast(this));

    @Override
    public DrunkManager getDrunkManager() {
        return drunkManager;
    }

    public EntityPlayerMixin(World par1World) {
        super(par1World);
    }

    public void displayGUIEnchantReserver(int x, int y, int z, EnchantReserverSlots slots) {
    }

//    // TODO Only for reversing the > to be <=, also I dont know why
//
//    @ModifyExpressionValue(method = "getMaxCraftingQuality", at = @At(value = "INVOKE", target = "Lnet/minecraft/EntityPlayer;getCraftingExperienceCost(F)I"))
//    private int reverse_1(int original) {
//        return -original;
//    }
//
//    @ModifyExpressionValue(method = "getMaxCraftingQuality", at = @At(value = "FIELD", target = "Lnet/minecraft/EntityPlayer;experience:I", opcode = Opcodes.GETFIELD))
//    private int reverse_2(int original) {
//        return -original;
//    }


    @Inject(method = "readEntityFromNBT", at = @At("HEAD"))
    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound, CallbackInfo ci) {
        this.diarrheaManager.setDiarrheaCounter(par1NBTTagCompound.getInteger("diarrheaCounter"));
        this.huntManager.hunt_cap = par1NBTTagCompound.getBoolean("UsedTotemOfHunt");
        this.huntManager.hunt_counter = par1NBTTagCompound.getInteger("TotemDyingCounter");
        this.newPlayerManager.setNew(par1NBTTagCompound.getBoolean("isNewPlayer"));
        this.FreezingCooldown = par1NBTTagCompound.getInteger("FreezingCooldown");
        this.FreezingWarning = par1NBTTagCompound.getInteger("FreezingWarning");
        this.drunkManager.setDrunk_duration(par1NBTTagCompound.getInteger("DrunkDuration"));
        this.HeatResistance = par1NBTTagCompound.getInteger("HeatResistance");
    }

    @Inject(method = "writeEntityToNBT", at = @At("HEAD"))
    private void writeMine(NBTTagCompound par1NBTTagCompound, CallbackInfo ci) {
        par1NBTTagCompound.setInteger("diarrheaCounter", this.diarrheaManager.getDiarrheaCounter());
        par1NBTTagCompound.setBoolean("UsedTotemOfHunt", this.huntManager.hunt_cap);
        par1NBTTagCompound.setInteger("TotemDyingCounter", this.huntManager.hunt_counter);
        par1NBTTagCompound.setBoolean("isNewPlayer", this.newPlayerManager.getNew());
        par1NBTTagCompound.setInteger("FreezingCooldown", this.FreezingCooldown);
        par1NBTTagCompound.setInteger("FreezingWarning", this.FreezingWarning);
        par1NBTTagCompound.setInteger("DrunkDuration", this.drunkManager.getDrunk_duration());
        par1NBTTagCompound.setInteger("HeatResistance", this.HeatResistance);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void checkForArmorAchievements() {
        boolean wearing_leather = false;
        boolean wearing_full_suit_plate = true;
        boolean wearing_full_suit_adamantium_plate = true;
        boolean wearing_full_suit_wolf_fur = true;
        for (int i = 0; i < 4; i++) {
            if (this.inventory.armorInventory[i] != null && this.inventory.armorInventory[i].getItem() instanceof ItemArmor armor) {
                Material material = armor.getArmorMaterial();
                if (material == Material.leather)
                    wearing_leather = true;
                if (material != Material.copper && material != Material.silver && material != Material.gold && material != Material.iron && material != Material.mithril && material != Material.adamantium && material != Material.ancient_metal && material != Materials.tungsten && material != Materials.nickel && material != Materials.ancient_metal_sacred && material != Materials.uru)
                    wearing_full_suit_plate = false;
                if (material != Material.adamantium)
                    wearing_full_suit_adamantium_plate = false;
                if (material != Materials.wolf_fur)
                    wearing_full_suit_wolf_fur = false;
            } else {
                wearing_full_suit_plate = false;
                wearing_full_suit_adamantium_plate = false;
                wearing_full_suit_wolf_fur = false;
            }
        }
        if (wearing_leather)
            triggerAchievement(AchievementList.wearLeather);
        if (wearing_full_suit_plate)
            triggerAchievement(AchievementList.wearAllPlateArmor);
        if (wearing_full_suit_adamantium_plate)
            triggerAchievement(AchievementList.wearAllAdamantiumPlateArmor);
        if (wearing_full_suit_wolf_fur)
            triggerAchievement(AchievementExtend.BravetheCold);
    }

    public float getNickelArmorCoverage() {
        float coverage = 0.0F;
        ItemStack[] worn_items = getWornItems();
        for (int i = 0; i < worn_items.length; i++) {
            ItemStack item_stack = worn_items[i];
            if (item_stack != null)
                if (item_stack.isArmor()) {
                    ItemArmor barding = item_stack.getItem().getAsArmor();
                    if (barding.getArmorMaterial() == Materials.nickel)
                        coverage += barding.getCoverage() * barding.getDamageFactor(item_stack, this);
                } else if (item_stack.getItem() instanceof ItemHorseArmor var6) {
                    if (var6.getArmorMaterial() == Materials.nickel)
                        coverage += var6.getCoverage();
                }
        }
        return coverage;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public EntityDamageResult attackEntityFrom(Damage damage) {
        float nickel_coverage = MathHelper.clamp_float(getNickelArmorCoverage(), 0.0F, 1.0F);
        if (damage.getResponsibleEntity() instanceof net.minecraft.EntityGelatinousCube) {
            System.out.println("nickel_coverage = " + nickel_coverage);
            if (nickel_coverage >= 0.999F)
                return null;
            damage.scaleAmount(1.0F - nickel_coverage);
        }
        if (this.ticksExisted < 1000 && Damage.wasCausedByPlayer(damage) && isWithinTournamentSafeZone())
            return null;
        if (this.capabilities.disableDamage && !damage.canHarmInCreative())
            return null;
        if (inBed())
            wakeUpPlayer(true, damage.getResponsibleEntity());
        if (damage.isExplosion()) {
            if (damage.getResponsibleEntity() == this)
                return null;
            damage.scaleAmount(1.5F);
        }
        if (Config.FinalChallenge.get())
            damage.scaleAmount(1.0F + Constant.CalculateCurrentDiff() / 50.0F);
        EntityDamageResult result = super.attackEntityFrom(damage);
        return result;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public static int getHealthLimit(int level) {
        int HealthLMTwithTag = 0;
        int HealthLMTwithoutTag = Math.max(Math.min(6 + level / 5 * 2, 20), 6);
        if (level <= 35) {
            HealthLMTwithTag = HealthLMTwithoutTag;
        } else {
            HealthLMTwithTag = Math.max(Math.min(14 + level / 10 * 2, 40), 20);
        }
        return Config.TagDistortion.get() ? HealthLMTwithTag : HealthLMTwithoutTag;
    }


    @Inject(method = "getCurrentPlayerStrVsBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/EntityPlayer;isInsideOfMaterial(Lnet/minecraft/Material;)Z"))
    private void inject(int x, int y, int z, boolean apply_held_item, CallbackInfoReturnable<Float> cir, @Local(ordinal = 0) LocalFloatRef str_vs_block) {// TODO unstable
        if (isPotionActive(PotionExtend.freeze)) {
            float newStr = str_vs_block.get() * (1.0F - (getActivePotionEffect(PotionExtend.freeze).getAmplifier() + 1) * 0.5F);
            str_vs_block.set(newStr);
        }
    }

    @ModifyArg(method = "getCurrentPlayerStrVsBlock", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"), index = 0)
    private float inject(float str_vs_block) {
        if (Config.FinalChallenge.get())
            str_vs_block *= 1.0F - Constant.CalculateCurrentDiff() / 100.0F;
        if (Config.Realistic.get())
            str_vs_block *= Math.min((float) Math.pow(getHealth(), 2.0D) / 25.0F, 1.0F);
        return str_vs_block;
    }


    public void attackMonsters(List<Entity> targets, float damage) {
        for (int i = 0; i < targets.size(); i++) {
            EntityLivingBase entityMonster = (targets.get(i) instanceof EntityLivingBase) ? (EntityLivingBase) targets.get(i) : null;
            if (entityMonster != null && entityMonster.getDistanceSqToEntity(this) <= getReach(EnumEntityReachContext.FOR_MELEE_ATTACK, entityMonster) && entityMonster.canEntityBeSeenFrom(this.posX, getEyePosY(), this.posZ, 5.0D))
                entityMonster.attackEntityFrom(new Damage(DamageSource.causePlayerDamage(getAsPlayer()), damage));
        }
    }

    @Inject(method = "fall", at = @At("TAIL"))
    private void TagMovingV2(float par1, CallbackInfo ci) {
        if (Config.TagMovingV2.get())
            this.setSprinting(false);
    }

    @Shadow
    public int getNutrition() {
        return 1;
    }

    @Shadow
    public int getSatiation() {
        return 1;
    }

    @Shadow
    public ItemStack[] getWornItems() {
        return new ItemStack[0];
    }

    @Shadow
    public void wakeUpPlayer(boolean get_out_of_bed, Entity entity_to_look_at) {
    }

    @Shadow
    public boolean willDeliverCriticalStrike() {
        return false;
    }

    @Shadow
    public float calcRawMeleeDamageVs(Entity target, boolean critical, boolean suspended_in_liquid) {
        return 0.0F;
    }

    @Shadow
    public abstract void addExperience(int paramInt);

    @Shadow
    public abstract float getReach(EnumEntityReachContext paramEnumEntityReachContext, Entity paramEntity);

    @Shadow
    public abstract double getEyePosY();

    @Shadow
    public void addScore(int par1) {
    }

    @Shadow
    public final int getExperienceLevel() {
        return 0;
    }

    @Shadow
    public float getHealthLimit() {
        return 0.0F;
    }

    @Shadow
    protected static final int getExperienceRequired(int level) {
        return 0;
    }

    @Shadow
    public boolean hasCurse(Curse curse) {
        return hasCurse(curse, false);
    }
}
