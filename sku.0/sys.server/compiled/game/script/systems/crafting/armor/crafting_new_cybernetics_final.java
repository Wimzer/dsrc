package script.systems.crafting.armor;

import script.dictionary;
import script.draft_schematic;
import script.library.armor;
import script.library.armor_enhancement;
import script.library.craftinglib;
import script.modifiable_int;
import script.obj_id;
import script.obj_var_list;

public class crafting_new_cybernetics_final extends script.systems.crafting.crafting_base
{
    public crafting_new_cybernetics_final()
    {
    }
    public static final String VERSION = "v0.00.00";
    public int OnManufacturingSchematicCreation(obj_id self, obj_id player, obj_id prototype, draft_schematic schematic, modifiable_int assemblyResult, modifiable_int experimentPoints) throws InterruptedException
    {
        if (!armor_enhancement.validateCraftingPayload(self))
        {
            assemblyResult.set(9);
            endCraftingSession(player, obj_id.NULL_ID, prototype);
            CustomerServiceLog("crafting", "WARNING: rejected stale cybernetic enhancement payload on schematic " + self);
            return SCRIPT_CONTINUE;
        }
        if (!armor_enhancement.validateEnhancementIngredients(schematic))
        {
            assemblyResult.set(9);
            endCraftingSession(player, obj_id.NULL_ID, prototype);
            CustomerServiceLog("crafting", "WARNING: rejected invalid cybernetic enhancement ingredients on schematic " + self);
            return SCRIPT_CONTINUE;
        }
        armor_enhancement.resetSchematicEnhancements(self);
        if (!armor_enhancement.prepareFeatherIngredientPayload(self, schematic))
        {
            assemblyResult.set(9);
            endCraftingSession(player, obj_id.NULL_ID, prototype);
            CustomerServiceLog("crafting", "WARNING: failed to capture direct feather payload on schematic " + self);
            return SCRIPT_CONTINUE;
        }
        if (!armor_enhancement.prepareFinishedCartridgeIngredientPayload(self, schematic))
        {
            assemblyResult.set(9);
            endCraftingSession(player, obj_id.NULL_ID, prototype);
            CustomerServiceLog("crafting", "WARNING: failed to capture Biological Mechanics Cartridge payload on schematic " + self);
            return SCRIPT_CONTINUE;
        }
        removeObjVar(self, armor.OBJVAR_ARMOR_LEVEL);
        removeObjVar(self, armor.OBJVAR_ARMOR_CATEGORY);
        return super.OnManufacturingSchematicCreation(self, player, prototype, schematic, assemblyResult, experimentPoints);
    }
    public int OnManufactureObject(obj_id self, obj_id player, obj_id newObject, draft_schematic schematic, boolean isPrototype, boolean isRealObject) throws InterruptedException
    {
        if (!armor_enhancement.validateCraftingPayload(self))
        {
            CustomerServiceLog("crafting", "WARNING: rejected invalid cybernetic enhancement payload on schematic " + self);
            return SCRIPT_OVERRIDE;
        }
        return super.OnManufactureObject(self, player, newObject, schematic, isPrototype, isRealObject);
    }
    public void calcAndSetPrototypeProperties(obj_id prototype, draft_schematic.attribute[] itemAttributes, dictionary craftingValuesDictionary) throws InterruptedException
    {
        setObjVar(prototype, craftinglib.OBJVAR_CRAFTER, getOwner(prototype));
        if (!armor_enhancement.captureCraftedEnhancements(getSelf(), prototype))
        {
            CustomerServiceLog("crafting", "WARNING: failed to capture cybernetic enhancement payload for schematic " + getSelf() + " to prototype " + prototype);
        }
        LOG("sissynoid", "Owner is: " + getOwner(prototype));
        LOG("sissynoid", "calcAndSetPrototypeProperties #1");
        CustomerServiceLog("crafting", "WARNING: Entered calcAndSetPrototypeProperties #1");
        if (!hasObjVar(getSelf(), craftinglib.COMPONENT_ATTRIBUTE_INTERNAL_OBJVAR_NAME + "." + armor.OBJVAR_ARMOR_LEVEL))
        {
            LOG("sissynoid", "calcAndSetPrototypeProperties #1 - No Amor Level?");
            CustomerServiceLog("crafting", "WARNING: armor final draft schematic " + getDraftSchematic(getSelf()) + " has no " + craftinglib.COMPONENT_ATTRIBUTE_INTERNAL_OBJVAR_NAME + "." + armor.OBJVAR_ARMOR_LEVEL + " objvar set");
            super.calcAndSetPrototypeProperties(prototype, itemAttributes, craftingValuesDictionary);
            return;
        }
        if (!hasObjVar(getSelf(), craftinglib.COMPONENT_ATTRIBUTE_INTERNAL_OBJVAR_NAME + "." + armor.OBJVAR_ARMOR_CATEGORY))
        {
            LOG("sissynoid", "calcAndSetPrototypeProperties #1 - No Amor Category?");
            CustomerServiceLog("crafting", "WARNING: armor final draft schematic " + getDraftSchematic(getSelf()) + " has no " + craftinglib.COMPONENT_ATTRIBUTE_INTERNAL_OBJVAR_NAME + "." + armor.OBJVAR_ARMOR_CATEGORY + " objvar set");
            super.calcAndSetPrototypeProperties(prototype, itemAttributes, craftingValuesDictionary);
            return;
        }
        CustomerServiceLog("crafting", "WARNING: PASSED Objvar checks");
        int armorLevel = getIntObjVar(getSelf(), craftinglib.COMPONENT_ATTRIBUTE_INTERNAL_OBJVAR_NAME + "." + armor.OBJVAR_ARMOR_LEVEL);
        int armorCategory = getIntObjVar(getSelf(), craftinglib.COMPONENT_ATTRIBUTE_INTERNAL_OBJVAR_NAME + "." + armor.OBJVAR_ARMOR_CATEGORY);
        setObjVar(getSelf(), armor.OBJVAR_ARMOR_LEVEL, armorLevel);
        setObjVar(getSelf(), armor.OBJVAR_ARMOR_CATEGORY, armorCategory);
        int finalRow = dataTableSearchColumnForInt(getStringCrc(armor.DATATABLE_FINAL_ROW + armorLevel), armor.DATATABLE_TYPE_COL, armor.DATATABLE_ARMOR);
        int coreRow = dataTableSearchColumnForInt(getStringCrc(armor.DATATABLE_CORE_ROW + armorLevel), armor.DATATABLE_TYPE_COL, armor.DATATABLE_ARMOR);
        if (finalRow < 0 || coreRow < 0)
        {
            CustomerServiceLog("crafting", "WARNING: armor datatable " + armor.DATATABLE_ARMOR + " has missing core or final row (" + coreRow + "," + finalRow + ")");
            return;
        }
        obj_var_list componentData = getObjVarList(getSelf(), craftinglib.COMPONENT_ATTRIBUTE_INTERNAL_OBJVAR_NAME);
        if (componentData != null)
        {
            armor.rescaleComponentData(getSelf(), componentData, coreRow, finalRow);
        }
        armor.updateItemAttributes(getSelf(), itemAttributes, finalRow);
        super.calcAndSetPrototypeProperties(prototype, itemAttributes, craftingValuesDictionary);
        LOG("sissynoid", "calcAndSetPrototypeProperties #1 - END");
    }
    public void calcAndSetPrototypeProperties(obj_id prototype, draft_schematic.attribute[] itemAttributes) throws InterruptedException
    {
        if (!armor_enhancement.applySchematicEnhancements(getSelf(), prototype))
        {
            CustomerServiceLog("crafting", "WARNING: failed to apply stored cybernetic enhancement payload for schematic " + getSelf() + " to prototype " + prototype);
        }
        LOG("sissynoid", "calcAndSetPrototypeProperties #2");
        CustomerServiceLog("crafting", "WARNING: Entered: calcAndSetPrototypeProperties #2");
        int armorLevel = 0;
        int armorCategory = 0;
        int finalRow = 0;
        if (hasObjVar(getSelf(), armor.OBJVAR_ARMOR_LEVEL) && hasObjVar(getSelf(), armor.OBJVAR_ARMOR_CATEGORY))
        {
            CustomerServiceLog("crafting", "WARNING: has Armor Cores");
            armorLevel = getIntObjVar(getSelf(), armor.OBJVAR_ARMOR_LEVEL);
            armorCategory = getIntObjVar(getSelf(), armor.OBJVAR_ARMOR_CATEGORY);
            finalRow = dataTableSearchColumnForInt(getStringCrc(armor.DATATABLE_FINAL_ROW + armorLevel), armor.DATATABLE_TYPE_COL, armor.DATATABLE_ARMOR);
            setObjVar(prototype, armor.OBJVAR_ARMOR_BASE + "." + armor.OBJVAR_ARMOR_LEVEL, armorLevel);
            setObjVar(prototype, armor.OBJVAR_ARMOR_BASE + "." + armor.OBJVAR_ARMOR_CATEGORY, armorCategory);
        }
        for (draft_schematic.attribute itemAttribute : itemAttributes) {
            CustomerServiceLog("crafting", "WARNING: Item attributes");
            if (itemAttribute == null) {
                continue;
            }
            if (!calcAndSetPrototypeProperty(prototype, itemAttribute)) {
                if (itemAttribute.currentValue < itemAttribute.minValue) {
                    itemAttribute.currentValue = itemAttribute.minValue;
                } else if (itemAttribute.currentValue > itemAttribute.maxValue) {
                    itemAttribute.currentValue = itemAttribute.maxValue;
                }
                setObjVar(prototype, armor.OBJVAR_ARMOR_BASE + "." + (itemAttribute.name).getAsciiId(), itemAttribute.currentValue);
                if ((itemAttribute.name).equals(armor.OBJVAR_CONDITION)) {
                    float hp = armor.getAbsoluteArmorAttribute(itemAttribute.currentValue, finalRow, armor.DATATABLE_MIN_CONDITION_COL);
                    if (hp != Float.MIN_VALUE) {
                        if (hasObjVar(getSelf(), armor.OBJVAR_CONDITION_MULTIPLIER)) {
                            float multiplier = getFloatObjVar(getSelf(), armor.OBJVAR_CONDITION_MULTIPLIER);
                            if (multiplier > 0) {
                                hp *= multiplier;
                            } else {
                                CustomerServiceLog("crafting", "Armor schematic " + getDraftSchematic(getSelf()) + " has an invalid condition multiplier " + multiplier);
                            }
                        }
                        setMaxHitpoints(prototype, (int) hp);
                    }
                }
            }
        }
        if (hasObjVar(getSelf(), "addArmorRefitScript"))
        {
            attachScript(prototype, getStringObjVar(getSelf(), "addArmorRefitScript"));
        }
        armor.saveScaledDataToPrototype(getSelf(), prototype, itemAttributes, armor.OBJVAR_ARMOR_BASE);
        LOG("crafting", "WARNING: FINISHED #2");
        if (hasObjVar(prototype, "armor.general_protection") && hasObjVar(prototype, "modifyCyberneticArmorValue") && !hasObjVar(prototype, "modifiedCyberneticArmorValue"))
        {
            LOG("sissynoid", "*************Entered Armor Reduction Modification**************");
            float protectionAmount = getFloatObjVar(prototype, "armor.general_protection");
            LOG("sissynoid", "******ARMOR PROTECTION BASE****** :: " + protectionAmount);
            obj_id crafter = obj_id.NULL_ID;
            int reductionExpertise = 0;
            if (hasObjVar(prototype, craftinglib.OBJVAR_CRAFTER))
            {
                crafter = getObjIdObjVar(prototype, craftinglib.OBJVAR_CRAFTER);
                if (crafter != null)
                {
                    reductionExpertise = getEnhancedSkillStatisticModifierUncapped(crafter, "expertise_cybernetic_negative_effects_reduction");
                    LOG("sissynoid", "******EXPERTISE REDUCTION AMOUNT****** :: " + reductionExpertise);
                }
            }
            float percentReductionExpertise = reductionExpertise / 100.0f;
            LOG("sissynoid", "******EXPERTISE REDUCTION AMOUNT****** :: PERCENTAGE :: " + percentReductionExpertise);
            float reductionAmount = 0.4f;
            if (percentReductionExpertise > 0)
            {
                reductionAmount = 1.0f - (reductionAmount - percentReductionExpertise);
                LOG("sissynoid", "**OLD REDUCT MULTIPLIER: " + (1.0f - reductionAmount) + " **New Reduction MULTIPLIER** " + reductionAmount);
            }
            else 
            {
                reductionAmount = 1.0f - 0.4f;
                LOG("sissynoid", "Reduction Expertise is not being found!");
            }
            protectionAmount *= reductionAmount;
            setObjVar(prototype, "armor.general_protection", protectionAmount);
            LOG("sissynoid", "**NEW PROTECTION AMOUNT** (((" + protectionAmount + " )))");
            removeObjVar(prototype, "modifyCyberneticArmorValue");
            LOG("sissynoid", "Removing Modify Objvar");
            setObjVar(prototype, "modifiedCyberneticArmorValue", true);
        }
        if (hasObjVar(prototype, "cybernetic.special_protection.type") && hasObjVar(prototype, "cybernetic.special_protection.value"))
        {
            LOG("sissynoid", "SPECIAL PROTECTIONS");
            int protectionType = getIntObjVar(prototype, "cybernetic.special_protection.type");
            float protectionValue = getFloatObjVar(prototype, "cybernetic.special_protection.value");
            LOG("sissynoid", "***Protection Type: " + protectionType + " ***Protection Value: " + protectionValue);
            LOG("sissynoid", "FINAL CRAFTING #2: protection module type: " + protectionType + " and Value: " + protectionValue);
            armor.setArmorSpecialProtectionPercent(prototype, protectionType, protectionValue);
        }
    }
}
