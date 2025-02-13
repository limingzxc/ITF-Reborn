package net.oilcake.mitelros.mixins.entity.mob;

import net.minecraft.*;
import net.oilcake.mitelros.api.ITFWorld;
import net.oilcake.mitelros.config.ITFConfig;
import net.oilcake.mitelros.enchantment.Enchantments;
import net.oilcake.mitelros.item.potion.PotionExtend;
import net.oilcake.mitelros.util.Constant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityMob.class)
public class EntityMobMixin extends EntityCreature {
    private boolean modified_attribute;

    @Inject(method = "attackEntityAsMob(Lnet/minecraft/Entity;)Lnet/minecraft/EntityDamageResult;", at = @At("HEAD"), cancellable = true)
    private void inject(Entity target, CallbackInfoReturnable<EntityDamageResult> cir) {
        if (this.isPotionActive(PotionExtend.stunning)) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "attackEntityAsMob(Lnet/minecraft/Entity;)Lnet/minecraft/EntityDamageResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/EntityMob;isFrenzied()Z"))
    private void explosion(Entity target, CallbackInfoReturnable<EntityDamageResult> cir) {
        ItemStack held_item = this.getHeldItemStack();
        if (EnchantmentHelper.hasEnchantment(held_item, Enchantments.enchantmentDestroying)) {
            int destorying = EnchantmentHelper.getEnchantmentLevel(Enchantments.enchantmentDestroying, held_item);
            target.worldObj.createExplosion(this, target.posX, target.posY, target.posZ, 0.0F, destorying * 0.5F, true);
            //target.setFire(120); TODO why comment
        }
    }

    @ModifyArg(method = "attackEntityAsMob(Lnet/minecraft/Entity;)Lnet/minecraft/EntityDamageResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/EntityLivingBase;addPotionEffect(Lnet/minecraft/PotionEffect;)V"))
    private PotionEffect stun(PotionEffect par1PotionEffect) {
        int stun = par1PotionEffect.getAmplifier() / 5;
        return new PotionEffect(PotionExtend.stunning.id, stun * 60, 0);
    }

    @Inject(method = "attackEntityAsMob(Lnet/minecraft/EntityLiving;Lnet/minecraft/Entity;)Lnet/minecraft/EntityDamageResult;", at = @At("HEAD"), cancellable = true)
    private static void inject_1(EntityLiving attacker, Entity target, CallbackInfoReturnable<EntityDamageResult> cir) {
        if (attacker.isPotionActive(PotionExtend.stunning)) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "attackEntityAsMob(Lnet/minecraft/EntityLiving;Lnet/minecraft/Entity;)Lnet/minecraft/EntityDamageResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/EntityLiving;isFrenzied()Z"))
    private static void explosion_1(EntityLiving attacker, Entity target, CallbackInfoReturnable<EntityDamageResult> cir) {
        ItemStack held_item = attacker.getHeldItemStack();
        if (EnchantmentHelper.hasEnchantment(held_item, Enchantments.enchantmentDestroying)) {
            int destorying = EnchantmentHelper.getEnchantmentLevel(Enchantments.enchantmentDestroying, held_item);
            ((ITFWorld) target.worldObj).newExplosionC(attacker, target.posX, target.posY, target.posZ, 0.0F, destorying * 0.5F, true);
        }
    }

    @ModifyArg(method = "attackEntityAsMob(Lnet/minecraft/EntityLiving;Lnet/minecraft/Entity;)Lnet/minecraft/EntityDamageResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/EntityLivingBase;addPotionEffect(Lnet/minecraft/PotionEffect;)V"))
    private static PotionEffect stun_1(PotionEffect par1PotionEffect) {
        int stun = par1PotionEffect.getAmplifier() / 5;
        return new PotionEffect(PotionExtend.stunning.id, stun * 60, 0);
    }

    public EntityMobMixin(World par1World) {
        super(par1World);
        this.modified_attribute = false;
    }

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/EntityCreature;onUpdate()V", shift = At.Shift.AFTER))
    private void challenge(CallbackInfo ci) {
        if (!this.worldObj.isRemote && !this.modified_attribute && getHealth() > 0.0F && ITFConfig.FinalChallenge.get()) {
            setEntityAttribute(SharedMonsterAttributes.maxHealth, (getMaxHealth() * (1.0F + Constant.calculateCurrentDifficulty() / 16.0F)));
            double attack_damage = getEntityAttributeValue(SharedMonsterAttributes.attackDamage);
            if (getHeldItemStack() != null && getHeldItemStack().getItem() instanceof net.minecraft.ItemTool) {
                attack_damage -= getHeldItemStack().getItemAsTool().getMaterialDamageVsEntity();
                attack_damage -= getHeldItemStack().getItemAsTool().getBaseDamageVsEntity();
            }
            setEntityAttribute(SharedMonsterAttributes.attackDamage, attack_damage * (1.0F + Constant.calculateCurrentDifficulty() / 32.0F));
            setHealth(getMaxHealth());
            this.modified_attribute = true;
        }
    }

    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
        super.writeEntityToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setBoolean("modified_attribute", this.modified_attribute);
    }

    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
        super.readEntityFromNBT(par1NBTTagCompound);
        this.modified_attribute = par1NBTTagCompound.getBoolean("modified_attribute");
    }
}
