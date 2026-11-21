package script.library;

import script.draft_schematic;
import script.obj_id;

public class armor_enhancement extends script.base_script
{
    public armor_enhancement()
    {
    }

    public static final String CATEGORY = "armor_enhancement";
    public static final String OBJVAR_ROOT = "armor_enhancement";
    public static final String OBJVAR_HEALTH = OBJVAR_ROOT + ".health";
    public static final String OBJVAR_SKILL_MOD = OBJVAR_ROOT + ".skill_mod";
    public static final String OBJVAR_SKILL_VALUE = OBJVAR_ROOT + ".skill_value";
    public static final String OBJVAR_VERSION = OBJVAR_ROOT + ".version";
    public static final int VERSION = 1;

    public static final String RETROFIT_ROOT = "armor_enhancement_retrofit";
    public static final String RETROFIT_ID = RETROFIT_ROOT + ".id";
    public static final String RETROFIT_HEALTH_TOTAL = RETROFIT_ROOT + ".health_total";
    public static final String RETROFIT_HEALTH = RETROFIT_ROOT + ".health";
    public static final String RETROFIT_SKILL_MOD = RETROFIT_ROOT + ".skill_mod";
    public static final String RETROFIT_SKILL_VALUE = RETROFIT_ROOT + ".skill_value";
    public static final String RETROFIT_APPLIED = RETROFIT_ROOT + ".applied";

    public static final String COMPONENT_HEALTH = "armor_enhancement_health";
    public static final String[] COMPONENT_SKILL_MODS =
    {
        "armor_enhancement_resistance_bleeding",
        "armor_enhancement_resistance_disease",
        "armor_enhancement_resistance_fire",
        "armor_enhancement_resistance_poison",
        "armor_enhancement_slope_move"
    };
    public static final String[] SKILL_MODS =
    {
        "resistance_bleeding",
        "resistance_disease",
        "resistance_fire",
        "resistance_poison",
        "slope_move"
    };

    public static final String FEATHER_SLOT = "appearance_enhancement_one";
    public static final String CARTRIDGE_SLOT = "enhancement_cartridge";
    public static final String BIO_MECHANICS_SUBCOMPONENT_SLOT = "bio_mechanics_subcomponent";
    public static final String LEGACY_FEATHER_TEMPLATE = "object/tangible/component/armor/armor_appearance_enhancement_feather.iff";
    public static final String BIO_MECHANICS_CARTRIDGE_TEMPLATE = "object/tangible/component/armor/biological_mechanics_cartridge.iff";
    public static final String[] BIO_MECHANICS_SUBCOMPONENT_TEMPLATES =
    {
        "object/tangible/component/armor/bio_mechanics_bleeding_resistance.iff",
        "object/tangible/component/armor/bio_mechanics_disease_resistance.iff",
        "object/tangible/component/armor/bio_mechanics_fire_resistance.iff",
        "object/tangible/component/armor/bio_mechanics_poison_resistance.iff",
        "object/tangible/component/armor/bio_mechanics_terrain_negotiation.iff"
    };
    public static final String[] DIRECT_FEATHER_TEMPLATES =
    {
        "object/tangible/component/armor/armor_appearance_enhancement_feather_s01.iff",
        "object/tangible/component/armor/armor_appearance_enhancement_feather_s02.iff",
        "object/tangible/component/armor/armor_appearance_enhancement_feather_s03.iff",
        "object/tangible/component/armor/armor_appearance_enhancement_feather_s04.iff",
        "object/tangible/component/armor/armor_appearance_enhancement_feather_s05.iff",
        "object/tangible/component/armor/armor_appearance_enhancement_feather_s06.iff",
        "object/tangible/component/armor/armor_appearance_enhancement_feather_s07.iff",
        "object/tangible/component/armor/armor_appearance_enhancement_feather_s08.iff",
        "object/tangible/component/armor/armor_appearance_enhancement_feather_s09.iff"
    };
    public static final int[] DIRECT_FEATHER_MIN_HEALTH = { 5, 8, 11, 15, 18, 22, 26, 32, 45 };
    public static final int[] DIRECT_FEATHER_MAX_HEALTH = { 12, 15, 19, 24, 28, 32, 39, 45, 50 };

    public static final int MAX_HEALTH = 50;
    public static final int MAX_RESISTANCE = 3;
    public static final int MAX_SLOPE_MOVE = 25;

    public static boolean isValidHealth(int value) throws InterruptedException
    {
        return value > 0 && value <= MAX_HEALTH;
    }

    public static boolean isValidSkillMod(String skillMod, int value) throws InterruptedException
    {
        int index = getSkillModIndex(skillMod);
        return index >= 0 && value > 0 && value <= getSkillModMaximum(index);
    }

    public static boolean storeHealthPayload(obj_id component, int health) throws InterruptedException
    {
        if (!isIdValid(component) || !exists(component) || !isValidHealth(health))
        {
            CustomerServiceLog("armor_enhancement", "Rejected invalid feather Health payload " + health + " for component " + component);
            return false;
        }
        setObjVar(component, getComponentRoot() + "." + COMPONENT_HEALTH, health);
        return true;
    }

    // Character-builder producers use the same finished-component contract as
    // crafting and retrofit. These methods never change production loot rolls.
    public static obj_id createTestFeather(obj_id inventory, int tier, int health) throws InterruptedException
    {
        if (!isIdValid(inventory) || !exists(inventory) || tier < 0 || tier >= DIRECT_FEATHER_TEMPLATES.length ||
            health < DIRECT_FEATHER_MIN_HEALTH[tier] || health > DIRECT_FEATHER_MAX_HEALTH[tier])
        {
            return null;
        }
        obj_id component = createObject(DIRECT_FEATHER_TEMPLATES[tier], inventory, "");
        if (!isIdValid(component) || !exists(component))
        {
            return null;
        }
        boolean valid = false;
        try
        {
            int[] bonuses = new int[NUM_ATTRIBUTES];
            bonuses[HEALTH] = health;
            setAttributeBonuses(component, bonuses);
            // Template crafting slots accept only crafted components (PlayerObject::fillSlot);
            // mark the feather the way loot.randomizeComponent marks a dropped component.
            valid = setCraftedId(component, inventory) && setCrafter(component, inventory) &&
                getFinishedFeatherHealth(component) == health;
            return valid ? component : null;
        }
        finally
        {
            if (!valid && !destroyObject(component))
            {
                CustomerServiceLog("armor_enhancement", "Failed to remove invalid frog feather " + component);
            }
        }
    }

    public static obj_id createTestCartridge(obj_id inventory, int skillModIndex, int value) throws InterruptedException
    {
        if (!isIdValid(inventory) || !exists(inventory) || skillModIndex < 0 || skillModIndex >= SKILL_MODS.length ||
            !isValidSkillMod(SKILL_MODS[skillModIndex], value))
        {
            return null;
        }
        obj_id component = createObject(BIO_MECHANICS_CARTRIDGE_TEMPLATE, inventory, "");
        if (!isIdValid(component) || !exists(component))
        {
            return null;
        }
        boolean valid = false;
        try
        {
            // Crafted marking as item.special.serialize_component does, so slots accept it.
            valid = setCraftedId(component, component) && setCrafter(component, component) &&
                applyPayload(component, false, 0, skillModIndex, value) &&
                getFinishedCartridgeSkillModIndex(component) == skillModIndex && getFinishedCartridgeSkillValue(component) == value;
            return valid ? component : null;
        }
        finally
        {
            if (!valid && !destroyObject(component))
            {
                CustomerServiceLog("armor_enhancement", "Failed to remove invalid frog cartridge " + component);
            }
        }
    }

    public static boolean storeSkillPayload(obj_id component, String skillMod, int value) throws InterruptedException
    {
        if (!isIdValid(component) || !exists(component) || !isValidSkillMod(skillMod, value))
        {
            CustomerServiceLog("armor_enhancement", "Rejected invalid cartridge payload " + skillMod + "=" + value + " for component " + component);
            return false;
        }
        int index = getSkillModIndex(skillMod);
        int previousIndex = getComponentSkillPayloadIndex(component);
        int previousValue = previousIndex >= 0 ? getIntObjVar(component, getComponentRoot() + "." + COMPONENT_SKILL_MODS[previousIndex]) : 0;
        removeCategorizedSkillModBonuses(component, CATEGORY);
        if (!setCategorizedSkillModBonus(component, CATEGORY, skillMod, value))
        {
            if (previousIndex >= 0 && isValidSkillMod(SKILL_MODS[previousIndex], previousValue))
            {
                setCategorizedSkillModBonus(component, CATEGORY, SKILL_MODS[previousIndex], previousValue);
            }
            return false;
        }
        clearComponentSkillPayload(component);
        setObjVar(component, getComponentRoot() + "." + COMPONENT_SKILL_MODS[index], value);
        return true;
    }

    public static boolean validateCraftingPayload(obj_id schematic) throws InterruptedException
    {
        if (!isIdValid(schematic) || !exists(schematic))
        {
            return false;
        }

        String internalRoot = getInternalRoot();
        String healthObjVar = internalRoot + "." + COMPONENT_HEALTH;
        boolean hasInternalPayload = hasObjVar(schematic, healthObjVar);
        if (hasObjVar(schematic, healthObjVar) && !isValidHealth(getIntObjVar(schematic, healthObjVar)))
        {
            return false;
        }

        int skillPayloadCount = 0;
        for (int i = 0; i < COMPONENT_SKILL_MODS.length; ++i)
        {
            String payloadObjVar = internalRoot + "." + COMPONENT_SKILL_MODS[i];
            if (!hasObjVar(schematic, payloadObjVar))
            {
                continue;
            }
            hasInternalPayload = true;
            ++skillPayloadCount;
            if (skillPayloadCount > 1 || !isValidSkillMod(SKILL_MODS[i], getIntObjVar(schematic, payloadObjVar)))
            {
                return false;
            }
        }
        if (!hasObjVar(schematic, OBJVAR_ROOT))
        {
            return true;
        }
        if (!hasObjVar(schematic, OBJVAR_VERSION) || getIntObjVar(schematic, OBJVAR_VERSION) != VERSION)
        {
            return false;
        }

        boolean hasHealth = hasObjVar(schematic, OBJVAR_HEALTH);
        if (hasHealth && !isValidHealth(getIntObjVar(schematic, OBJVAR_HEALTH)))
        {
            return false;
        }
        boolean hasSkillMod = hasObjVar(schematic, OBJVAR_SKILL_MOD);
        boolean hasSkillValue = hasObjVar(schematic, OBJVAR_SKILL_VALUE);
        if (hasSkillMod != hasSkillValue)
        {
            return false;
        }
        if (hasSkillMod && !isValidSkillMod(getStringObjVar(schematic, OBJVAR_SKILL_MOD), getIntObjVar(schematic, OBJVAR_SKILL_VALUE)))
        {
            return false;
        }
        return hasHealth || hasSkillMod;
    }

    public static boolean validateFinishedCartridgePayload(obj_id schematic) throws InterruptedException
    {
        return validateCraftingPayload(schematic) &&
            !hasObjVar(schematic, OBJVAR_HEALTH) &&
            !hasObjVar(schematic, getComponentRoot() + "." + COMPONENT_HEALTH) &&
            !hasObjVar(schematic, getInternalRoot() + "." + COMPONENT_HEALTH) &&
            hasObjVar(schematic, OBJVAR_SKILL_MOD) &&
            hasObjVar(schematic, OBJVAR_SKILL_VALUE);
    }

    public static boolean validateEnhancementIngredients(draft_schematic schematic) throws InterruptedException
    {
        if (schematic == null)
        {
            return false;
        }
        int healthPayloadCount = 0;
        int skillPayloadCount = 0;
        draft_schematic.slot[] slots = schematic.getSlots();
        for (draft_schematic.slot slot : slots) {
            if (slot == null || slot.ingredients == null) {
                continue;
            }
            boolean isFeatherSlot = slot.name != null && FEATHER_SLOT.equals((slot.name).getAsciiId());
            boolean isCartridgeSlot = slot.name != null && CARTRIDGE_SLOT.equals((slot.name).getAsciiId());
            for (draft_schematic.simple_ingredient ingredientData : slot.ingredients) {
                obj_id ingredient = ingredientData.ingredient;
                if (!isIdValid(ingredient) || !exists(ingredient)) {
                    continue;
                }
                String root = getComponentRoot();
                String healthObjVar = root + "." + COMPONENT_HEALTH;
                int featherTemplateIndex = getFeatherTemplateIndex(ingredient);
                if (featherTemplateIndex >= 0)
                {
                    if (!isFeatherSlot || !validateFeatherHealth(ingredient, featherTemplateIndex))
                    {
                        return false;
                    }
                    int health = getAttributeBonus(ingredient, HEALTH);
                    if (health > 0)
                    {
                        ++healthPayloadCount;
                        if (healthPayloadCount > 1)
                        {
                            return false;
                        }
                    }
                }
                else if (hasObjVar(ingredient, healthObjVar))
                {
                    return false;
                }
                int skillPayloadIndex = getComponentSkillPayloadIndex(ingredient);
                boolean isBioMechanicsCartridge = BIO_MECHANICS_CARTRIDGE_TEMPLATE.equals(getTemplateName(ingredient));
                if (skillPayloadIndex == -2 || isBioMechanicsCartridge && (!isCartridgeSlot || skillPayloadIndex < 0 || getFinishedCartridgeSkillModIndex(ingredient) != skillPayloadIndex))
                {
                    return false;
                }
                if (skillPayloadIndex >= 0)
                {
                    ++skillPayloadCount;
                    if (skillPayloadCount > 1 || !isCartridgeSlot || !isBioMechanicsCartridge)
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean prepareCartridgeIngredientPayload(obj_id manufacturingSchematic, draft_schematic schematic) throws InterruptedException
    {
        if (!isIdValid(manufacturingSchematic) || !exists(manufacturingSchematic) || schematic == null)
        {
            return false;
        }
        int componentCount = 0;
        draft_schematic.slot[] slots = schematic.getSlots();
        for (draft_schematic.slot slot : slots) {
            if (slot == null || slot.name == null || slot.ingredients == null || !BIO_MECHANICS_SUBCOMPONENT_SLOT.equals((slot.name).getAsciiId())) {
                continue;
            }
            for (draft_schematic.simple_ingredient ingredientData : slot.ingredients) {
                obj_id ingredient = ingredientData.ingredient;
                int templateIndex = getBioMechanicsSubcomponentTemplateIndex(ingredient);
                int payloadIndex = getComponentSkillPayloadIndex(ingredient);
                if (templateIndex < 0 || payloadIndex != templateIndex || ++componentCount > 1)
                {
                    return false;
                }
                int value = getIntObjVar(ingredient, getComponentRoot() + "." + COMPONENT_SKILL_MODS[payloadIndex]);
                if (!isValidSkillMod(SKILL_MODS[payloadIndex], value))
                {
                    return false;
                }
                setObjVar(manufacturingSchematic, getInternalRoot() + "." + COMPONENT_SKILL_MODS[payloadIndex], value);
            }
        }
        return componentCount == 1;
    }

    public static boolean prepareFinishedCartridgeIngredientPayload(obj_id manufacturingSchematic, draft_schematic schematic) throws InterruptedException
    {
        if (!isIdValid(manufacturingSchematic) || !exists(manufacturingSchematic) || schematic == null)
        {
            return false;
        }
        int cartridgeCount = 0;
        draft_schematic.slot[] slots = schematic.getSlots();
        for (draft_schematic.slot slot : slots) {
            if (slot == null || slot.name == null || slot.ingredients == null || !CARTRIDGE_SLOT.equals((slot.name).getAsciiId())) {
                continue;
            }
            for (draft_schematic.simple_ingredient ingredientData : slot.ingredients) {
                obj_id ingredient = ingredientData.ingredient;
                if (!isIdValid(ingredient) || !exists(ingredient) || !BIO_MECHANICS_CARTRIDGE_TEMPLATE.equals(getTemplateName(ingredient)))
                {
                    continue;
                }
                int payloadIndex = getFinishedCartridgeSkillModIndex(ingredient);
                if (payloadIndex < 0 || ++cartridgeCount > 1)
                {
                    return false;
                }
                int value = getIntObjVar(ingredient, getComponentRoot() + "." + COMPONENT_SKILL_MODS[payloadIndex]);
                if (!isValidSkillMod(SKILL_MODS[payloadIndex], value))
                {
                    return false;
                }
                setObjVar(manufacturingSchematic, getInternalRoot() + "." + COMPONENT_SKILL_MODS[payloadIndex], value);
            }
        }
        return true;
    }

    private static int getComponentSkillPayloadIndex(obj_id component) throws InterruptedException
    {
        if (!isIdValid(component) || !exists(component))
        {
            return -1;
        }
        int selected = -1;
        String root = getComponentRoot();
        for (int i = 0; i < COMPONENT_SKILL_MODS.length; ++i)
        {
            String payloadObjVar = root + "." + COMPONENT_SKILL_MODS[i];
            if (!hasObjVar(component, payloadObjVar))
            {
                continue;
            }
            int value = getIntObjVar(component, payloadObjVar);
            if (selected >= 0 || !isValidSkillMod(SKILL_MODS[i], value))
            {
                return -2;
            }
            selected = i;
        }
        return selected;
    }

    private static boolean hasInternalSkillPayload(obj_id component) throws InterruptedException
    {
        if (!isIdValid(component) || !exists(component))
        {
            return false;
        }
        String root = getInternalRoot();
        for (String componentSkillMod : COMPONENT_SKILL_MODS)
        {
            if (hasObjVar(component, root + "." + componentSkillMod))
            {
                return true;
            }
        }
        return false;
    }

    public static int getFinishedCartridgeSkillModIndex(obj_id component) throws InterruptedException
    {
        if (!isIdValid(component) || !exists(component) || !BIO_MECHANICS_CARTRIDGE_TEMPLATE.equals(getTemplateName(component)) || !validateFinishedCartridgePayload(component) || hasInternalSkillPayload(component))
        {
            return -1;
        }
        int index = getSkillModIndex(getStringObjVar(component, OBJVAR_SKILL_MOD));
        if (index < 0)
        {
            return -1;
        }
        int componentIndex = getComponentSkillPayloadIndex(component);
        int value = getIntObjVar(component, OBJVAR_SKILL_VALUE);
        return componentIndex == index && getIntObjVar(component, getComponentRoot() + "." + COMPONENT_SKILL_MODS[index]) == value ? index : -1;
    }

    public static int getFinishedCartridgeSkillValue(obj_id component) throws InterruptedException
    {
        int index = getFinishedCartridgeSkillModIndex(component);
        if (index < 0)
        {
            return 0;
        }
        int value = getIntObjVar(component, OBJVAR_SKILL_VALUE);
        return isValidSkillMod(SKILL_MODS[index], value) ? value : 0;
    }

    public static boolean validateBioMechanicsSubcomponent(obj_id component) throws InterruptedException
    {
        int templateIndex = getBioMechanicsSubcomponentTemplateIndex(component);
        int payloadIndex = getComponentSkillPayloadIndex(component);
        if (templateIndex < 0 || payloadIndex != templateIndex)
        {
            return false;
        }
        int value = getIntObjVar(component, getComponentRoot() + "." + COMPONENT_SKILL_MODS[payloadIndex]);
        return isValidSkillMod(SKILL_MODS[payloadIndex], value);
    }

    private static int getBioMechanicsSubcomponentTemplateIndex(obj_id component) throws InterruptedException
    {
        if (!isIdValid(component) || !exists(component))
        {
            return -1;
        }
        String template = getTemplateName(component);
        for (int i = 0; i < BIO_MECHANICS_SUBCOMPONENT_TEMPLATES.length; ++i)
        {
            if (BIO_MECHANICS_SUBCOMPONENT_TEMPLATES[i].equals(template))
            {
                return i;
            }
        }
        return -1;
    }

    public static boolean prepareFeatherIngredientPayload(obj_id manufacturingSchematic, draft_schematic schematic) throws InterruptedException
    {
        if (!isIdValid(manufacturingSchematic) || !exists(manufacturingSchematic) || schematic == null)
        {
            return false;
        }
        int featherCount = 0;
        draft_schematic.slot[] slots = schematic.getSlots();
        for (draft_schematic.slot slot : slots) {
            if (slot == null || slot.name == null || slot.ingredients == null || !FEATHER_SLOT.equals((slot.name).getAsciiId())) {
                continue;
            }
            for (draft_schematic.simple_ingredient ingredientData : slot.ingredients) {
                obj_id ingredient = ingredientData.ingredient;
                int featherTemplateIndex = getFeatherTemplateIndex(ingredient);
                if (featherTemplateIndex < 0)
                {
                    continue;
                }
                if (++featherCount > 1 || !validateFeatherHealth(ingredient, featherTemplateIndex))
                {
                    return false;
                }
                int health = getAttributeBonus(ingredient, HEALTH);
                if (health > 0)
                {
                    setObjVar(manufacturingSchematic, getInternalRoot() + "." + COMPONENT_HEALTH, health);
                }
            }
        }
        return true;
    }

    private static boolean validateFeatherHealth(obj_id ingredient, int featherTemplateIndex) throws InterruptedException
    {
        if (!isIdValid(ingredient) || !exists(ingredient) || featherTemplateIndex < 0)
        {
            return false;
        }
        int health = getAttributeBonus(ingredient, HEALTH);
        if (featherTemplateIndex == 0)
        {
            if (health != 0 && !isValidHealth(health))
            {
                return false;
            }
        }
        else
        {
            int tierIndex = featherTemplateIndex - 1;
            if (tierIndex < 0 || tierIndex >= DIRECT_FEATHER_TEMPLATES.length || health < DIRECT_FEATHER_MIN_HEALTH[tierIndex] || health > DIRECT_FEATHER_MAX_HEALTH[tierIndex])
            {
                return false;
            }
        }
        String healthObjVar = getComponentRoot() + "." + COMPONENT_HEALTH;
        return !hasObjVar(ingredient, healthObjVar) || health > 0 && getIntObjVar(ingredient, healthObjVar) == health;
    }

    public static int getFinishedFeatherHealth(obj_id ingredient) throws InterruptedException
    {
        int templateIndex = getFeatherTemplateIndex(ingredient);
        if (templateIndex < 0 || !validateFeatherHealth(ingredient, templateIndex) || getComponentSkillPayloadIndex(ingredient) != -1 || hasInternalSkillPayload(ingredient) ||
            hasObjVar(ingredient, OBJVAR_SKILL_MOD) || hasObjVar(ingredient, OBJVAR_SKILL_VALUE) || hasObjVar(ingredient, getInternalRoot() + "." + COMPONENT_HEALTH))
        {
            return -1;
        }
        return getAttributeBonus(ingredient, HEALTH);
    }

    private static int getFeatherTemplateIndex(obj_id ingredient) throws InterruptedException
    {
        if (!isIdValid(ingredient) || !exists(ingredient))
        {
            return -1;
        }
        String template = getTemplateName(ingredient);
        if (LEGACY_FEATHER_TEMPLATE.equals(template))
        {
            return 0;
        }
        for (int i = 0; i < DIRECT_FEATHER_TEMPLATES.length; ++i)
        {
            if (DIRECT_FEATHER_TEMPLATES[i].equals(template))
            {
                return i + 1;
            }
        }
        return -1;
    }

    public static void resetSchematicEnhancements(obj_id schematic) throws InterruptedException
    {
        if (!isIdValid(schematic) || !exists(schematic))
        {
            return;
        }

        if (hasObjVar(schematic, OBJVAR_HEALTH))
        {
            int previousHealth = getIntObjVar(schematic, OBJVAR_HEALTH);
            int[] attributeBonuses = getAttributeBonuses(schematic);
            if (isValidHealth(previousHealth) && attributeBonuses != null && attributeBonuses.length == NUM_ATTRIBUTES)
            {
                attributeBonuses[HEALTH] -= previousHealth;
                setAttributeBonuses(schematic, attributeBonuses);
            }
        }
        removeObjVar(schematic, OBJVAR_ROOT);

        String internalRoot = getInternalRoot();
        removeObjVar(schematic, internalRoot + "." + COMPONENT_HEALTH);
        for (String componentSkillMod : COMPONENT_SKILL_MODS) {
            removeObjVar(schematic, internalRoot + "." + componentSkillMod);
        }
        removeObjVar(schematic, internalRoot + "." + craftinglib.OBJVAR_COMPONENT_ATTRIB_BONUSES);
    }

    public static boolean validateFeatherIngredients(draft_schematic schematic) throws InterruptedException
    {
        if (schematic == null)
        {
            return false;
        }
        int health = 0;
        draft_schematic.slot[] slots = schematic.getSlots();
        for (draft_schematic.slot slot : slots) {
            if (slot == null || slot.ingredients == null) {
                continue;
            }
            for (draft_schematic.simple_ingredient ingredientData : slot.ingredients) {
                obj_id ingredient = ingredientData.ingredient;
                if (!isIdValid(ingredient) || !exists(ingredient)) {
                    continue;
                }
                health += getAttributeBonus(ingredient, HEALTH) * ingredientData.count;
            }
        }
        return health == 0 || isValidHealth(health);
    }

    public static boolean captureCraftedEnhancements(obj_id schematic, obj_id prototype) throws InterruptedException
    {
        if (!isIdValid(schematic) || !exists(schematic) || !isIdValid(prototype) || !exists(prototype))
        {
            CustomerServiceLog("armor_enhancement", "Cannot capture crafted enhancements from schematic " + schematic + " to prototype " + prototype);
            return false;
        }

        String internalRoot = getInternalRoot();
        String healthObjVar = internalRoot + "." + COMPONENT_HEALTH;
        boolean hasHealth = hasObjVar(schematic, healthObjVar);
        int health = 0;
        if (hasHealth)
        {
            health = getIntObjVar(schematic, healthObjVar);
            if (!isValidHealth(health))
            {
                CustomerServiceLog("armor_enhancement", "Rejected invalid crafted feather Health payload " + health + " on schematic " + schematic);
                return false;
            }
        }

        int selectedSkillMod = -1;
        int selectedSkillValue = 0;
        for (int i = 0; i < COMPONENT_SKILL_MODS.length; ++i)
        {
            String payloadObjVar = internalRoot + "." + COMPONENT_SKILL_MODS[i];
            if (!hasObjVar(schematic, payloadObjVar))
            {
                continue;
            }
            int value = getIntObjVar(schematic, payloadObjVar);
            if (selectedSkillMod >= 0 || !isValidSkillMod(SKILL_MODS[i], value))
            {
                CustomerServiceLog("armor_enhancement", "Rejected invalid or ambiguous crafted cartridge payload on schematic " + schematic);
                return false;
            }
            selectedSkillMod = i;
            selectedSkillValue = value;
        }

        if (!hasHealth && selectedSkillMod < 0)
        {
            if (hasObjVar(schematic, OBJVAR_ROOT))
            {
                return applySchematicEnhancements(schematic, prototype);
            }
            if (!applyPayload(prototype, false, 0, -1, 0))
            {
                return false;
            }
            removeObjVar(schematic, OBJVAR_ROOT);
            return true;
        }
        if (!applyPayload(prototype, hasHealth, health, selectedSkillMod, selectedSkillValue))
        {
            return false;
        }
        removeObjVar(schematic, OBJVAR_ROOT);
        setObjVar(schematic, OBJVAR_VERSION, VERSION);
        if (hasHealth)
        {
            setObjVar(schematic, OBJVAR_HEALTH, health);
        }
        if (selectedSkillMod >= 0)
        {
            setObjVar(schematic, OBJVAR_SKILL_MOD, SKILL_MODS[selectedSkillMod]);
            setObjVar(schematic, OBJVAR_SKILL_VALUE, selectedSkillValue);
        }
        return true;
    }

    public static boolean applySchematicEnhancements(obj_id schematic, obj_id prototype) throws InterruptedException
    {
        if (!isIdValid(schematic) || !exists(schematic) || !isIdValid(prototype) || !exists(prototype))
        {
            CustomerServiceLog("armor_enhancement", "Cannot apply stored enhancements from schematic " + schematic + " to prototype " + prototype);
            return false;
        }
        if (!hasObjVar(schematic, OBJVAR_VERSION))
        {
            return applyPayload(prototype, false, 0, -1, 0);
        }
        int version = getIntObjVar(schematic, OBJVAR_VERSION);
        if (version != VERSION)
        {
            CustomerServiceLog("armor_enhancement", "Rejected unsupported enhancement payload version " + version + " on schematic " + schematic);
            return false;
        }

        boolean hasHealth = hasObjVar(schematic, OBJVAR_HEALTH);
        int health = 0;
        if (hasHealth)
        {
            health = getIntObjVar(schematic, OBJVAR_HEALTH);
            if (!isValidHealth(health))
            {
                CustomerServiceLog("armor_enhancement", "Rejected invalid stored feather Health payload " + health + " on schematic " + schematic);
                return false;
            }
        }

        boolean hasSkillMod = hasObjVar(schematic, OBJVAR_SKILL_MOD);
        boolean hasSkillValue = hasObjVar(schematic, OBJVAR_SKILL_VALUE);
        if (hasSkillMod != hasSkillValue)
        {
            CustomerServiceLog("armor_enhancement", "Rejected incomplete stored cartridge payload on schematic " + schematic);
            return false;
        }
        int selectedSkillMod = -1;
        int selectedSkillValue = 0;
        if (hasSkillMod)
        {
            String skillMod = getStringObjVar(schematic, OBJVAR_SKILL_MOD);
            selectedSkillValue = getIntObjVar(schematic, OBJVAR_SKILL_VALUE);
            selectedSkillMod = getSkillModIndex(skillMod);
            if (selectedSkillMod < 0 || !isValidSkillMod(skillMod, selectedSkillValue))
            {
                CustomerServiceLog("armor_enhancement", "Rejected invalid stored cartridge payload " + skillMod + "=" + selectedSkillValue + " on schematic " + schematic);
                return false;
            }
        }
        if (!hasHealth && selectedSkillMod < 0)
        {
            CustomerServiceLog("armor_enhancement", "Rejected empty stored enhancement payload on schematic " + schematic);
            return false;
        }
        return applyPayload(prototype, hasHealth, health, selectedSkillMod, selectedSkillValue);
    }

    private static boolean applyPayload(obj_id prototype, boolean hasHealth, int health, int selectedSkillMod, int selectedSkillValue) throws InterruptedException
    {
        boolean isBioMechanicsCartridge = BIO_MECHANICS_CARTRIDGE_TEMPLATE.equals(getTemplateName(prototype));
        if (isBioMechanicsCartridge && (hasHealth || selectedSkillMod < 0))
        {
            CustomerServiceLog("armor_enhancement", "Rejected incomplete Biological Mechanics Cartridge payload on prototype " + prototype);
            return false;
        }
        String previousSkillMod = "";
        int previousSkillValue = 0;
        if (hasObjVar(prototype, OBJVAR_SKILL_MOD) && hasObjVar(prototype, OBJVAR_SKILL_VALUE))
        {
            previousSkillMod = getStringObjVar(prototype, OBJVAR_SKILL_MOD);
            previousSkillValue = getIntObjVar(prototype, OBJVAR_SKILL_VALUE);
        }
        removeCategorizedSkillModBonuses(prototype, CATEGORY);
        if (selectedSkillMod >= 0)
        {
            if (!setCategorizedSkillModBonus(prototype, CATEGORY, SKILL_MODS[selectedSkillMod], selectedSkillValue))
            {
                if (isValidSkillMod(previousSkillMod, previousSkillValue))
                {
                    setCategorizedSkillModBonus(prototype, CATEGORY, previousSkillMod, previousSkillValue);
                }
                CustomerServiceLog("armor_enhancement", "Failed to apply cartridge payload to prototype " + prototype);
                return false;
            }
        }

        // Health is not written here. Stock crafting already carries the feather's attribute
        // bonus: components roll up into the schematic (crafting_base.calcAndSetPrototypeProperties)
        // and the finished item receives the schematic's bonuses (crafting_base.OnManufactureObject).
        // Adding it again doubled the value shown in the crafting window. This records only the
        // feature-owned share, which retrofit replacement subtracts later.
        removeObjVar(prototype, OBJVAR_ROOT);
        if (hasHealth)
        {
            setObjVar(prototype, OBJVAR_HEALTH, health);
        }
        if (selectedSkillMod >= 0)
        {
            setObjVar(prototype, OBJVAR_SKILL_MOD, SKILL_MODS[selectedSkillMod]);
            setObjVar(prototype, OBJVAR_SKILL_VALUE, selectedSkillValue);
        }
        if (hasHealth || selectedSkillMod >= 0)
        {
            setObjVar(prototype, OBJVAR_VERSION, VERSION);
        }
        if (isBioMechanicsCartridge)
        {
            clearComponentSkillPayload(prototype);
            setObjVar(prototype, getComponentRoot() + "." + COMPONENT_SKILL_MODS[selectedSkillMod], selectedSkillValue);
        }
        return true;
    }

    public static boolean isEligibleRetrofitTarget(obj_id target) throws InterruptedException
    {
        if (!isIdValid(target) || !exists(target) || !isCrafted(target) || !armor.isValidArmor(target))
        {
            return false;
        }
        int armorLevel = armor.getArmorLevel(target);
        int armorCategory = armor.getArmorCategory(target);
        if (armorLevel < AL_basic || armorLevel >= AL_max || armorCategory < AC_reconnaissance || armorCategory >= AC_psg)
        {
            return false;
        }
        int got = getGameObjectType(target);
        boolean isEligibleArmor = got == GOT_armor_body || got == GOT_armor_head || got == GOT_armor_arm || got == GOT_armor_leg;
        boolean isEligibleCybernetic = got == GOT_cybernetic_arm || got == GOT_cybernetic_forearm || got == GOT_cybernetic_legs || got == GOT_cybernetic_torso;
        return isEligibleArmor || isEligibleCybernetic;
    }

    public static boolean validateAppliedPayload(obj_id target) throws InterruptedException
    {
        if (!isIdValid(target) || !exists(target))
        {
            return false;
        }
        boolean hasVersion = hasObjVar(target, OBJVAR_VERSION);
        boolean hasHealth = hasObjVar(target, OBJVAR_HEALTH);
        boolean hasSkillMod = hasObjVar(target, OBJVAR_SKILL_MOD);
        boolean hasSkillValue = hasObjVar(target, OBJVAR_SKILL_VALUE);
        if (!hasVersion && !hasHealth && !hasSkillMod && !hasSkillValue)
        {
            return true;
        }
        if (!hasVersion || getIntObjVar(target, OBJVAR_VERSION) != VERSION)
        {
            return false;
        }
        if (hasHealth && !isValidHealth(getIntObjVar(target, OBJVAR_HEALTH)))
        {
            return false;
        }
        if (hasSkillMod != hasSkillValue)
        {
            return false;
        }
        if (hasSkillMod && !isValidSkillMod(getStringObjVar(target, OBJVAR_SKILL_MOD), getIntObjVar(target, OBJVAR_SKILL_VALUE)))
        {
            return false;
        }
        return hasHealth || hasSkillMod;
    }

    public static boolean prepareRetrofitTarget(obj_id target, String operationId, int health, int skillModIndex, int skillValue) throws InterruptedException
    {
        if (!isEligibleRetrofitTarget(target) || operationId == null || operationId.length() == 0 || health < 0 || health > MAX_HEALTH || skillModIndex < 0 || skillModIndex >= SKILL_MODS.length || !isValidSkillMod(SKILL_MODS[skillModIndex], skillValue) || !validateAppliedPayload(target))
        {
            return false;
        }
        if (hasObjVar(target, RETROFIT_ID))
        {
            return operationId.equals(getStringObjVar(target, RETROFIT_ID)) &&
                hasObjVar(target, RETROFIT_HEALTH_TOTAL) &&
                hasObjVar(target, RETROFIT_HEALTH) && getIntObjVar(target, RETROFIT_HEALTH) == health &&
                hasObjVar(target, RETROFIT_SKILL_MOD) && SKILL_MODS[skillModIndex].equals(getStringObjVar(target, RETROFIT_SKILL_MOD)) &&
                hasObjVar(target, RETROFIT_SKILL_VALUE) && getIntObjVar(target, RETROFIT_SKILL_VALUE) == skillValue;
        }
        removeObjVar(target, RETROFIT_ROOT);
        int previousHealth = hasObjVar(target, OBJVAR_HEALTH) ? getIntObjVar(target, OBJVAR_HEALTH) : 0;
        int[] attributeBonuses = getAttributeBonuses(target);
        int currentHealth = attributeBonuses != null && attributeBonuses.length == NUM_ATTRIBUTES ? attributeBonuses[HEALTH] : 0;
        setObjVar(target, RETROFIT_HEALTH_TOTAL, currentHealth - previousHealth + health);
        setObjVar(target, RETROFIT_HEALTH, health);
        setObjVar(target, RETROFIT_SKILL_MOD, SKILL_MODS[skillModIndex]);
        setObjVar(target, RETROFIT_SKILL_VALUE, skillValue);
        setObjVar(target, RETROFIT_APPLIED, 0);
        setObjVar(target, RETROFIT_ID, operationId);
        return true;
    }

    public static boolean applyPreparedRetrofit(obj_id target, String operationId) throws InterruptedException
    {
        if (!isIdValid(target) || !exists(target) || operationId == null || !hasObjVar(target, RETROFIT_ID) || !operationId.equals(getStringObjVar(target, RETROFIT_ID)) ||
            !hasObjVar(target, RETROFIT_HEALTH_TOTAL) || !hasObjVar(target, RETROFIT_HEALTH) || !hasObjVar(target, RETROFIT_SKILL_MOD) || !hasObjVar(target, RETROFIT_SKILL_VALUE))
        {
            return false;
        }
        int desiredHealthTotal = getIntObjVar(target, RETROFIT_HEALTH_TOTAL);
        int health = getIntObjVar(target, RETROFIT_HEALTH);
        String skillMod = getStringObjVar(target, RETROFIT_SKILL_MOD);
        int skillValue = getIntObjVar(target, RETROFIT_SKILL_VALUE);
        if (health < 0 || health > MAX_HEALTH || !isValidSkillMod(skillMod, skillValue))
        {
            return false;
        }

        String previousSkillMod = hasObjVar(target, OBJVAR_SKILL_MOD) ? getStringObjVar(target, OBJVAR_SKILL_MOD) : "";
        int previousSkillValue = hasObjVar(target, OBJVAR_SKILL_VALUE) ? getIntObjVar(target, OBJVAR_SKILL_VALUE) : 0;
        removeCategorizedSkillModBonuses(target, CATEGORY);
        if (!setCategorizedSkillModBonus(target, CATEGORY, skillMod, skillValue))
        {
            if (isValidSkillMod(previousSkillMod, previousSkillValue))
            {
                setCategorizedSkillModBonus(target, CATEGORY, previousSkillMod, previousSkillValue);
            }
            return false;
        }
        int[] attributeBonuses = getAttributeBonuses(target);
        if (attributeBonuses == null || attributeBonuses.length != NUM_ATTRIBUTES)
        {
            attributeBonuses = new int[NUM_ATTRIBUTES];
        }
        attributeBonuses[HEALTH] = desiredHealthTotal;
        setAttributeBonuses(target, attributeBonuses);

        removeObjVar(target, OBJVAR_ROOT);
        if (health > 0)
        {
            setObjVar(target, OBJVAR_HEALTH, health);
        }
        setObjVar(target, OBJVAR_SKILL_MOD, skillMod);
        setObjVar(target, OBJVAR_SKILL_VALUE, skillValue);
        setObjVar(target, OBJVAR_VERSION, VERSION);
        setObjVar(target, RETROFIT_APPLIED, 1);
        return true;
    }

    public static boolean isRetrofitOperationApplied(obj_id target, String operationId) throws InterruptedException
    {
        return isIdValid(target) && exists(target) && operationId != null && hasObjVar(target, RETROFIT_ID) && operationId.equals(getStringObjVar(target, RETROFIT_ID)) && hasObjVar(target, RETROFIT_APPLIED) && getIntObjVar(target, RETROFIT_APPLIED) == 1;
    }

    public static boolean hasRetrofitOperation(obj_id target) throws InterruptedException
    {
        return isIdValid(target) && exists(target) && hasObjVar(target, RETROFIT_ID);
    }

    public static boolean hasMatchingRetrofitOperation(obj_id target, String operationId) throws InterruptedException
    {
        return hasRetrofitOperation(target) && operationId != null && operationId.equals(getStringObjVar(target, RETROFIT_ID));
    }

    public static boolean matchesAppliedPayload(obj_id target, int health, int skillModIndex, int skillValue) throws InterruptedException
    {
        if (!isIdValid(target) || !exists(target) || health < 0 || health > MAX_HEALTH || skillModIndex < 0 || skillModIndex >= SKILL_MODS.length || !isValidSkillMod(SKILL_MODS[skillModIndex], skillValue) || !validateAppliedPayload(target))
        {
            return false;
        }
        int storedHealth = hasObjVar(target, OBJVAR_HEALTH) ? getIntObjVar(target, OBJVAR_HEALTH) : 0;
        return storedHealth == health &&
            hasObjVar(target, OBJVAR_SKILL_MOD) && SKILL_MODS[skillModIndex].equals(getStringObjVar(target, OBJVAR_SKILL_MOD)) &&
            hasObjVar(target, OBJVAR_SKILL_VALUE) && getIntObjVar(target, OBJVAR_SKILL_VALUE) == skillValue;
    }

    public static boolean finishRetrofitTarget(obj_id target, String operationId) throws InterruptedException
    {
        if (!isIdValid(target) || !exists(target) || operationId == null || !hasObjVar(target, RETROFIT_ID) || !operationId.equals(getStringObjVar(target, RETROFIT_ID)))
        {
            return false;
        }
        removeObjVar(target, RETROFIT_ROOT);
        return true;
    }

    public static int getSkillModIndex(String skillMod) throws InterruptedException
    {
        if (skillMod == null || skillMod.length() == 0)
        {
            return -1;
        }
        for (int i = 0; i < SKILL_MODS.length; ++i)
        {
            if (SKILL_MODS[i].equals(skillMod))
            {
                return i;
            }
        }
        return -1;
    }

    public static int getSkillModMaximum(int index) throws InterruptedException
    {
        if (index < 0 || index >= SKILL_MODS.length)
        {
            return 0;
        }
        if (SKILL_MODS[index].equals("slope_move"))
        {
            return MAX_SLOPE_MOVE;
        }
        return MAX_RESISTANCE;
    }

    public static String getComponentRoot() throws InterruptedException
    {
        return craftinglib.COMPONENT_ATTRIBUTE_OBJVAR_NAME;
    }

    public static String getInternalRoot() throws InterruptedException
    {
        return craftinglib.COMPONENT_ATTRIBUTE_INTERNAL_OBJVAR_NAME;
    }

    public static void clearComponentSkillPayload(obj_id component) throws InterruptedException
    {
        String root = getComponentRoot();
        for (String componentSkillMod : COMPONENT_SKILL_MODS) {
            removeObjVar(component, root + "." + componentSkillMod);
        }
    }
}
