package script.systems.crafting.armor;

import script.library.craftinglib;
import script.resource_weight;

public class crafting_bio_mechanics_fire_component extends script.systems.crafting.armor.crafting_bio_mechanics_component
{
    public static final String[] REQUIRED_SKILLS = { "crafting_armorsmith_novice" };
    public static final String[] ASSEMBLY_SKILL_MODS = { "armor_assembly" };
    public static final String[] EXPERIMENT_SKILL_MODS = { "armor_experimentation" };
    public static final resource_weight[] ATTRIBUTE_RESOURCES =
    {
        new resource_weight("power", new resource_weight.weight[]
        {
            new resource_weight.weight(craftinglib.RESOURCE_QUALITY, 1),
            new resource_weight.weight(craftinglib.RESOURCE_HEAT_RESIST, 1),
            new resource_weight.weight(craftinglib.RESOURCE_DECAY_RESIST, 1)
        })
    };

    public String[] getRequiredSkills() throws InterruptedException { return REQUIRED_SKILLS; }
    public String[] getAssemblySkillMods() throws InterruptedException { return ASSEMBLY_SKILL_MODS; }
    public String[] getExperimentSkillMods() throws InterruptedException { return EXPERIMENT_SKILL_MODS; }
    public resource_weight[] getResourceMaxResourceWeights() throws InterruptedException { return ATTRIBUTE_RESOURCES; }
    public resource_weight[] getAssemblyResourceWeights() throws InterruptedException { return ATTRIBUTE_RESOURCES; }
}
