package script.systems.crafting.armor;

import script.draft_schematic;
import script.obj_id;
import script.library.armor_enhancement;

public class crafting_bio_mechanics_component extends script.systems.crafting.crafting_base
{
    public crafting_bio_mechanics_component()
    {
    }

    public static final String VERSION = "v1.00.00";

    public int OnManufactureObject(obj_id self, obj_id player, obj_id newObject, draft_schematic schematic, boolean isPrototype, boolean isRealObject) throws InterruptedException
    {
        if (!armor_enhancement.validateBioMechanicsSubcomponent(newObject))
        {
            CustomerServiceLog("armor_enhancement", "Rejected invalid bio-mechanics subcomponent payload on prototype " + newObject);
            return SCRIPT_OVERRIDE;
        }
        return super.OnManufactureObject(self, player, newObject, schematic, isPrototype, isRealObject);
    }

    public void calcAndSetPrototypeProperties(obj_id prototype, draft_schematic.attribute[] itemAttributes) throws InterruptedException
    {
        for (draft_schematic.attribute itemAttribute : itemAttributes) {
            if (itemAttribute == null)
            {
                continue;
            }
            if (calcAndSetPrototypeProperty(prototype, itemAttribute))
            {
                continue;
            }
            if ((itemAttribute.name).equals("power"))
            {
                int skillIndex = getSkillIndexForTemplate(getTemplateName(prototype));
                int value = Math.round(itemAttribute.currentValue);
                if (skillIndex < 0 || !armor_enhancement.storeSkillPayload(prototype, armor_enhancement.SKILL_MODS[skillIndex], value))
                {
                    CustomerServiceLog("armor_enhancement", "Failed to store bio-mechanics component payload on prototype " + prototype);
                }
            }
        }
    }

    private static int getSkillIndexForTemplate(String template) throws InterruptedException
    {
        for (int i = 0; i < armor_enhancement.BIO_MECHANICS_SUBCOMPONENT_TEMPLATES.length; ++i)
        {
            if (armor_enhancement.BIO_MECHANICS_SUBCOMPONENT_TEMPLATES[i].equals(template))
            {
                return i;
            }
        }
        return -1;
    }
}
