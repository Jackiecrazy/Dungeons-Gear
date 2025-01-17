package com.infamous.dungeons_gear.items.melee;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.infamous.dungeons_gear.capabilities.offhand.OffhandProvider;
import com.infamous.dungeons_gear.combat.CombatEventHandler;
import com.infamous.dungeons_gear.compat.DungeonsGearCompatibility;
import com.infamous.dungeons_gear.registry.AttributeRegistry;
import com.infamous.dungeons_gear.registry.ItemRegistry;
import com.infamous.dungeons_gear.items.interfaces.IComboWeapon;
import com.infamous.dungeons_gear.items.interfaces.IMeleeWeapon;
import com.infamous.dungeons_gear.items.interfaces.IDualWieldWeapon;
import com.infamous.dungeons_gear.items.interfaces.ISoulGatherer;
import com.infamous.dungeons_gear.utilties.DescriptionHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.item.SwordItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;

import java.util.List;
import java.util.UUID;

import net.minecraft.item.Item.Properties;

public class DaggerItem extends SwordItem implements IDualWieldWeapon, IMeleeWeapon, ISoulGatherer, IComboWeapon {
    private static final UUID ATTACK_REACH_MODIFIER = UUID.fromString("63d316c1-7d6d-41be-81c3-41fc1a216c27");
    private final boolean unique;
    private final float attackDamage;
    private final float attackSpeed;
    private Multimap<Attribute, AttributeModifier> attributeModifiers;

    public DaggerItem(IItemTier tier, int attackDamageIn, float attackSpeedIn, Properties builder, boolean isUnique) {
        super(tier, attackDamageIn, attackSpeedIn, builder);
        this.unique = isUnique;
        attackDamage = attackDamageIn;
        attackSpeed = attackSpeedIn;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> lvt_5_1_ = ImmutableMultimap.builder();
        lvt_5_1_.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", attackDamageIn, AttributeModifier.Operation.ADDITION));
        lvt_5_1_.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", attackSpeedIn, AttributeModifier.Operation.ADDITION));
        this.attributeModifiers = lvt_5_1_.build();
    }

    public static void setAttributeModifierMultimap(DaggerItem glaiveItem) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", (double) glaiveItem.attackDamage, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", (double) glaiveItem.attackSpeed, AttributeModifier.Operation.ADDITION));
        builder.put(AttributeRegistry.ATTACK_REACH.get(), new AttributeModifier(ATTACK_REACH_MODIFIER, "Weapon modifier", -0.5, AttributeModifier.Operation.ADDITION));
        builder.put(ForgeMod.REACH_DISTANCE.get(), new AttributeModifier(ATTACK_REACH_MODIFIER, "Weapon modifier", -0.5, AttributeModifier.Operation.ADDITION));
        glaiveItem.attributeModifiers = builder.build();
    }

    @Override
    public boolean canDualWield(ItemStack stack) {
        return stack.getItem() instanceof DaggerItem;
    }

    @Override
    public boolean hasFreezingBuiltIn(ItemStack stack) {
        return stack.getItem() == ItemRegistry.FANG_OF_FROST.get();
    }

    @Override
    public boolean hasEnigmaResonatorBuiltIn(ItemStack stack) {
        return stack.getItem() == ItemRegistry.MOON_DAGGER.get();
    }

    @Override
    public boolean hasSwirlingBuiltIn(ItemStack stack) {
        return stack.getItem() == ItemRegistry.SHEAR_DAGGER.get();
    }

    @Override
    public int getComboLength(ItemStack stack, LivingEntity attacker) {
        return 6;
    }

    public Rarity getRarity(ItemStack itemStack) {

        if (this.unique) {
            return Rarity.RARE;
        }
        return Rarity.UNCOMMON;
    }

    @Override
    public void appendHoverText(ItemStack stack, World world, List<ITextComponent> list, ITooltipFlag flag) {
        super.appendHoverText(stack, world, list, flag);
        DescriptionHelper.addFullDescription(list, stack);
    }

    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlotType equipmentSlot) {
        return equipmentSlot == EquipmentSlotType.MAINHAND ? this.attributeModifiers : super.getDefaultAttributeModifiers(equipmentSlot);
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (handIn == Hand.OFF_HAND && worldIn.isClientSide && !DungeonsGearCompatibility.warDance) {
            CombatEventHandler.checkForOffhandAttack();
            ItemStack offhand = playerIn.getItemInHand(handIn);
            return new ActionResult<>(ActionResultType.SUCCESS, offhand);
        } else {
            return new ActionResult<>(ActionResultType.PASS, playerIn.getItemInHand(handIn));
        }
    }

    @Override
    public int getGatherAmount(ItemStack stack) {
        if (stack.getItem() == ItemRegistry.MOON_DAGGER.get()) {
            return 1;
        } else return 0;
    }

    @Override
    public float getActivationCost(ItemStack stack) {
        return 0;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.invulnerableTime = 0;
        if (stack.getCapability(OffhandProvider.OFFHAND_CAPABILITY).isPresent()) {
            if (!stack.getCapability(OffhandProvider.OFFHAND_CAPABILITY).resolve().get().getLinkedItemStack().isEmpty())
                stack = stack.getCapability(OffhandProvider.OFFHAND_CAPABILITY).resolve().get().getLinkedItemStack();
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (entityIn instanceof LivingEntity && !worldIn.isClientSide)
            update((LivingEntity) entityIn, stack, itemSlot);
    }


}
