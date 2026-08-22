package script.systems.crafting.droid.crafted_items;

import script.library.craftinglib;
import script.draft_schematic;
import script.modifiable_int;
import script.obj_id;
import script.resource_weight;

public class crafting_planetary_mining_droid extends script.systems.crafting.droid.crafting_base_droid_component
{
    public static final int MAX_EXPERIMENT_POINTS = 12;
    public static final String[] REQUIRED_SKILLS =
    {
        "crafting_droidengineer_novice"
    };
    public static final String[] ASSEMBLY_SKILL_MODS =
    {
        "droid_assembly"
    };
    public static final String[] EXPERIMENT_SKILL_MODS =
    {
        "droid_experimentation"
    };
    public static final String[] CUSTOMIZATION_SKILL_MODS =
    {
        "droid_customization"
    };
    public static final resource_weight[] OBJ_ASSEMBLY_ATTRIBUTE_RESOURCES =
    {
        new resource_weight("quality", new resource_weight.weight[]
        {
            new resource_weight.weight(craftinglib.RESOURCE_QUALITY, 1),
            new resource_weight.weight(craftinglib.RESOURCE_SHOCK_RESIST, 1),
            new resource_weight.weight(craftinglib.RESOURCE_HEAT_RESIST, 1)
        }),
        new resource_weight("duration", new resource_weight.weight[]
        {
            new resource_weight.weight(craftinglib.RESOURCE_COLD_RESIST, 1),
            new resource_weight.weight(craftinglib.RESOURCE_HEAT_RESIST, 1),
            new resource_weight.weight(craftinglib.RESOURCE_SHOCK_RESIST, 1),
            new resource_weight.weight(craftinglib.RESOURCE_TOUGHNESS, 1)
        }),
        new resource_weight("charges", new resource_weight.weight[]
        {
            new resource_weight.weight(craftinglib.RESOURCE_TOUGHNESS, 1),
            new resource_weight.weight(craftinglib.RESOURCE_SHOCK_RESIST, 1)
        })
    };

    public String[] getRequiredSkills() throws InterruptedException
    {
        return REQUIRED_SKILLS;
    }

    public String[] getAssemblySkillMods() throws InterruptedException
    {
        return ASSEMBLY_SKILL_MODS;
    }

    public String[] getExperimentSkillMods() throws InterruptedException
    {
        return EXPERIMENT_SKILL_MODS;
    }

    public String[] getCustomizationSkillMods() throws InterruptedException
    {
        return CUSTOMIZATION_SKILL_MODS;
    }

    public int OnManufacturingSchematicCreation(obj_id self, obj_id player, obj_id prototype, draft_schematic schematic, modifiable_int assemblyResult, modifiable_int experimentPoints) throws InterruptedException
    {
        int result = super.OnManufacturingSchematicCreation(self, player, prototype, schematic, assemblyResult, experimentPoints);
        if (experimentPoints.value() > MAX_EXPERIMENT_POINTS)
        {
            experimentPoints.set(MAX_EXPERIMENT_POINTS);
        }
        return result;
    }

    public resource_weight[] getResourceMaxResourceWeights() throws InterruptedException
    {
        return OBJ_ASSEMBLY_ATTRIBUTE_RESOURCES;
    }

    public resource_weight[] getAssemblyResourceWeights() throws InterruptedException
    {
        return OBJ_ASSEMBLY_ATTRIBUTE_RESOURCES;
    }
}
