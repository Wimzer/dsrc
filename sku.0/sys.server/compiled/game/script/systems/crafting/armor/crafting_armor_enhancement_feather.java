package script.systems.crafting.armor;

import script.draft_schematic;
import script.library.armor_enhancement;
import script.modifiable_int;
import script.obj_id;

public class crafting_armor_enhancement_feather extends script.systems.crafting.armor.crafting_armor_nostats
{
    public crafting_armor_enhancement_feather()
    {
    }

    public static final String VERSION = "v1.00.00";

    public int OnManufacturingSchematicCreation(obj_id self, obj_id player, obj_id prototype, draft_schematic schematic, modifiable_int assemblyResult, modifiable_int experimentPoints) throws InterruptedException
    {
        if (!armor_enhancement.validateFeatherIngredients(schematic))
        {
            assemblyResult.set(9);
            endCraftingSession(player, obj_id.NULL_ID, prototype);
            CustomerServiceLog("armor_enhancement", "Rejected invalid feather ingredient payload on schematic " + self);
            return SCRIPT_CONTINUE;
        }
        return super.OnManufacturingSchematicCreation(self, player, prototype, schematic, assemblyResult, experimentPoints);
    }

    public int OnManufactureObject(obj_id self, obj_id player, obj_id newObject, draft_schematic schematic, boolean isPrototype, boolean isRealObject) throws InterruptedException
    {
        int health = getAttributeBonus(self, HEALTH);
        if (health > 0 && !armor_enhancement.isValidHealth(health))
        {
            CustomerServiceLog("armor_enhancement", "Rejected feather attachment with invalid Health payload " + health + " on schematic " + self);
            return SCRIPT_OVERRIDE;
        }
        return super.OnManufactureObject(self, player, newObject, schematic, isPrototype, isRealObject);
    }

    public void calcAndSetPrototypeProperties(obj_id prototype, draft_schematic.attribute[] itemAttributes) throws InterruptedException
    {
        super.calcAndSetPrototypeProperties(prototype, itemAttributes);
        int health = getAttributeBonus(getSelf(), HEALTH);
        if (health <= 0)
        {
            return;
        }
        if (!armor_enhancement.storeHealthPayload(prototype, health))
        {
            CustomerServiceLog("armor_enhancement", "Failed to store feather Health payload " + health + " on prototype " + prototype);
        }
    }
}
