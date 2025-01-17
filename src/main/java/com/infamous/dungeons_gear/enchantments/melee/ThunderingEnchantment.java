package com.infamous.dungeons_gear.enchantments.melee;

import com.infamous.dungeons_gear.damagesources.ElectricShockDamageSource;
import com.infamous.dungeons_gear.damagesources.OffhandAttackDamageSource;
import com.infamous.dungeons_gear.enchantments.ModEnchantmentTypes;
import com.infamous.dungeons_gear.enchantments.types.AOEDamageEnchantment;
import com.infamous.dungeons_gear.items.interfaces.IMeleeWeapon;
import com.infamous.dungeons_gear.utilties.AreaOfEffectHelper;
import com.infamous.dungeons_gear.utilties.SoundHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.infamous.dungeons_gear.DungeonsGear.MODID;

import net.minecraft.enchantment.Enchantment.Rarity;

@Mod.EventBusSubscriber(modid = MODID)
public class ThunderingEnchantment extends AOEDamageEnchantment {

    public ThunderingEnchantment() {
        super(Rarity.RARE, ModEnchantmentTypes.MELEE, new EquipmentSlotType[]{
                EquipmentSlotType.MAINHAND});
    }

    public int getMaxLevel() {
        return 1;
    }

    @Override
    public void doPostAttack(LivingEntity user, Entity target, int level) {
        if(!(target instanceof LivingEntity)) return;
        if( user.getLastHurtMobTimestamp()==user.tickCount)return;
        float chance = user.getRandom().nextFloat();
        if(chance <=  0.3F){
            SoundHelper.playLightningStrikeSounds(user);
            AreaOfEffectHelper.electrifyNearbyEnemies(user, 5, 5, Integer.MAX_VALUE);
            //AbilityUtils.castLightningBolt(user, (LivingEntity)target);
        }
    }

    @SubscribeEvent
    public static void onStormlanderAttack(LivingAttackEvent event){
        if(event.getSource().getDirectEntity() instanceof AbstractArrowEntity) return;
        if(event.getSource() instanceof OffhandAttackDamageSource) return;
        if(event.getSource() instanceof ElectricShockDamageSource) return;
        if(!(event.getSource().getEntity() instanceof LivingEntity)) return;
        LivingEntity attacker = (LivingEntity)event.getSource().getEntity();
        if(attacker.getLastHurtMobTimestamp()==attacker.tickCount)return;
        LivingEntity victim = event.getEntityLiving();
        ItemStack mainhand = attacker.getMainHandItem();
        if(hasThunderingBuiltIn(mainhand)){
            float chance = attacker.getRandom().nextFloat();
            if(chance <=  0.3F){
                SoundHelper.playLightningStrikeSounds(attacker);
                AreaOfEffectHelper.electrifyNearbyEnemies(attacker, 5, 5, Integer.MAX_VALUE);
                //AbilityUtils.castLightningBolt(attacker, victim);
            }
        }
    }

    private static boolean hasThunderingBuiltIn(ItemStack mainhand) {
        return mainhand.getItem() instanceof IMeleeWeapon && ((IMeleeWeapon) mainhand.getItem()).hasThunderingBuiltIn(mainhand);
    }
}
