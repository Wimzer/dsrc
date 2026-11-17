package script.systems.crafting.armor;

import script.dictionary;
import script.draft_schematic;
import script.library.armor_enhancement;
import script.library.craftinglib;
import script.modifiable_int;
import script.obj_id;
import script.resource_weight;

public class crafting_biological_mechanics_cartridge extends script.systems.crafting.crafting_base
{
    public static final String[] REQUIRED_SKILLS = { "crafting_armorsmith_novice" };
    public static final String[] ASSEMBLY_SKILL_MODS = { "armor_assembly" };
    public static final String[] EXPERIMENT_SKILL_MODS = { "armor_experimentation" };
    public static final resource_weight[] ASSEMBLY_RESOURCE_WEIGHTS =
    {
        new resource_weight("quality", new resource_weight.weight[]
        {
            new resource_weight.weight(craftinglib.RESOURCE_QUALITY, 1),
            new resource_weight.weight(craftinglib.RESOURCE_TOUGHNESS, 1),
            new resource_weight.weight(craftinglib.RESOURCE_SHOCK_RESIST, 1),
            new resource_weight.weight(craftinglib.RESOURCE_MALLEABILITY, 1)
        })
    };

    public String[] getRequiredSkills() throws InterruptedException { return REQUIRED_SKILLS; }
    public String[] getAssemblySkillMods() throws InterruptedException { return ASSEMBLY_SKILL_MODS; }
    public String[] getExperimentSkillMods() throws InterruptedException { return EXPERIMENT_SKILL_MODS; }
    public resource_weight[] getResourceMaxResourceWeights() throws InterruptedException { return ASSEMBLY_RESOURCE_WEIGHTS; }
    public resource_weight[] getAssemblyResourceWeights() throws InterruptedException { return ASSEMBLY_RESOURCE_WEIGHTS; }

    public int OnManufacturingSchematicCreation(obj_id self, obj_id player, obj_id prototype, draft_schematic schematic, modifiable_int assemblyResult, modifiable_int experimentPoints) throws InterruptedException
    {
        if (!armor_enhancement.validateCraftingPayload(self))
        {
            assemblyResult.set(9);
            endCraftingSession(player, obj_id.NULL_ID, prototype);
            return SCRIPT_CONTINUE;
        }
        armor_enhancement.resetSchematicEnhancements(self);
        if (!armor_enhancement.prepareCartridgeIngredientPayload(self, schematic))
        {
            assemblyResult.set(9);
            endCraftingSession(player, obj_id.NULL_ID, prototype);
            CustomerServiceLog("armor_enhancement", "Rejected invalid bio-mechanics subcomponent on schematic " + self);
            return SCRIPT_CONTINUE;
        }
        return super.OnManufacturingSchematicCreation(self, player, prototype, schematic, assemblyResult, experimentPoints);
    }

    public int OnManufactureObject(obj_id self, obj_id player, obj_id newObject, draft_schematic schematic, boolean isPrototype, boolean isRealObject) throws InterruptedException
    {
        if (!armor_enhancement.validateFinishedCartridgePayload(self))
        {
            CustomerServiceLog("armor_enhancement", "Rejected invalid Biological Mechanics Cartridge payload on schematic " + self);
            return SCRIPT_OVERRIDE;
        }
        return super.OnManufactureObject(self, player, newObject, schematic, isPrototype, isRealObject);
    }

    public void calcAndSetPrototypeProperties(obj_id prototype, draft_schematic.attribute[] itemAttributes, dictionary craftingValuesDictionary) throws InterruptedException
    {
        super.calcAndSetPrototypeProperties(prototype, itemAttributes, craftingValuesDictionary);
        if (!armor_enhancement.captureCraftedEnhancements(getSelf(), prototype))
        {
            CustomerServiceLog("armor_enhancement", "Failed to capture Biological Mechanics Cartridge payload on prototype " + prototype);
        }
    }

    public void calcAndSetPrototypeProperties(obj_id prototype, draft_schematic.attribute[] itemAttributes) throws InterruptedException
    {
        for (draft_schematic.attribute itemAttribute : itemAttributes) {
            calcAndSetPrototypeProperty(prototype, itemAttribute);
        }
        if (!armor_enhancement.applySchematicEnhancements(getSelf(), prototype))
        {
            CustomerServiceLog("armor_enhancement", "Failed to apply Biological Mechanics Cartridge payload on prototype " + prototype);
        }
    }
}
