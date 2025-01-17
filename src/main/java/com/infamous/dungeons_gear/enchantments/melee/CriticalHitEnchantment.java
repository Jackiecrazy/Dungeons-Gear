package com.infamous.dungeons_gear.enchantments.melee;

import com.infamous.dungeons_gear.config.DungeonsGearConfig;
import com.infamous.dungeons_gear.enchantments.ModEnchantmentTypes;
import com.infamous.dungeons_gear.enchantments.lists.MeleeEnchantmentList;
import com.infamous.dungeons_gear.enchantments.types.AOEDamageEnchantment;
import com.infamous.dungeons_gear.enchantments.types.DamageBoostEnchantment;
import com.infamous.dungeons_gear.items.interfaces.IComboWeapon;
import com.infamous.dungeons_gear.items.interfaces.IMeleeWeapon;
import com.infamous.dungeons_gear.utilties.ModEnchantmentHelper;
import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.infamous.dungeons_gear.DungeonsGear.MODID;

@Mod.EventBusSubscriber(modid = MODID)
public class CriticalHitEnchantment extends DamageBoostEnchantment {

    public CriticalHitEnchantment() {
        super(Enchantment.Rarity.RARE, ModEnchantmentTypes.MELEE, new EquipmentSlotType[]{
                EquipmentSlotType.MAINHAND});
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onVanillaNonCriticalHit(CriticalHitEvent event) {
        if (event.getPlayer() != null && !event.isVanillaCritical()) {
            PlayerEntity attacker = event.getPlayer();
            ItemStack mainhand = attacker.getMainHandItem();
            boolean uniqueWeaponFlag = hasCriticalHitBuiltIn(mainhand);
            boolean success = false;
            if (event.getResult() != Event.Result.ALLOW && mainhand.getItem() instanceof IComboWeapon) return;
            if (ModEnchantmentHelper.hasEnchantment(mainhand, MeleeEnchantmentList.CRITICAL_HIT)) {
                int criticalHitLevel = EnchantmentHelper.getItemEnchantmentLevel(MeleeEnchantmentList.CRITICAL_HIT, mainhand);
                float criticalHitChance;
                criticalHitChance = 0.05F + criticalHitLevel * 0.05F;
                float criticalHitRand = attacker.getRandom().nextFloat();
                if (criticalHitRand <= criticalHitChance) {
                    success = true;
                }
            }
            if (uniqueWeaponFlag) {
                float criticalHitRand = attacker.getRandom().nextFloat();
                if (criticalHitRand <= 0.1F) {
                    success = true;
                }
            }
            if (success) {
                event.setResult(Event.Result.ALLOW);
                float newDamageModifier = event.getDamageModifier() == event.getOldDamageModifier() && !(mainhand.getItem() instanceof IComboWeapon) ? event.getDamageModifier() + 1.5F : event.getDamageModifier() * 3.0F;
                event.setDamageModifier(newDamageModifier);
            }
        }
    }

    private static boolean hasCriticalHitBuiltIn(ItemStack mainhand) {
        return mainhand.getItem() instanceof IMeleeWeapon && ((IMeleeWeapon) mainhand.getItem()).hasCriticalHitBuiltIn(mainhand);
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return DungeonsGearConfig.ENABLE_OVERPOWERED_ENCHANTMENT_COMBOS.get() ||
                (!(enchantment instanceof DamageEnchantment) && !(enchantment instanceof DamageBoostEnchantment) && !(enchantment instanceof AOEDamageEnchantment));
    }
}
