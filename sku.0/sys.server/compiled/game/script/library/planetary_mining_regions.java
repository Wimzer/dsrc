package script.library;

import script.location;

public class planetary_mining_regions extends script.base_script
{
    public static final String SURVEY_REGIONS_TABLE = "datatables/resource/planetary_mining_regions.iff";
    public static final String[] REGION_SCENES = dataTableGetStringColumn(SURVEY_REGIONS_TABLE, "scene");
    public static final String[] REGION_NAMES = dataTableGetStringColumn(SURVEY_REGIONS_TABLE, "region");
    public static final int[] REGION_ORDERS = dataTableGetIntColumn(SURVEY_REGIONS_TABLE, "region_order");
    public static final int[] REGION_EXCLUSIONS = dataTableGetIntColumn(SURVEY_REGIONS_TABLE, "exclusion");
    public static final int[] REGION_POINT_ORDERS = dataTableGetIntColumn(SURVEY_REGIONS_TABLE, "point_order");
    public static final float[] REGION_X = dataTableGetFloatColumn(SURVEY_REGIONS_TABLE, "x");
    public static final float[] REGION_Z = dataTableGetFloatColumn(SURVEY_REGIONS_TABLE, "z");
    public static final float[] REGION_EDGE_OUTSETS = dataTableGetFloatColumn(SURVEY_REGIONS_TABLE, "edge_outset");
    public static final boolean REGION_DATA_VALID = hasValidRegionData();
    public static final float MUSTAFAR_DISPLAY_MIN_COORDINATE = -4000.0f;
    public static final float MUSTAFAR_DISPLAY_MAX_COORDINATE = 4000.0f;
    public static final float SURVEY_GRID_RADIUS = 32.0f;

    public static boolean isAllowedSurveyGrid(String planet, location surveyLocation) throws InterruptedException
    {
        if (!isWithinSceneBounds(planet, surveyLocation, SURVEY_GRID_RADIUS))
        {
            return false;
        }
        if (planet.equals("kashyyyk_main"))
        {
            return isWithinAnySurveyRegion(surveyLocation.x, surveyLocation.z, planet, false) && !isWithinAnySurveyRegion(surveyLocation.x, surveyLocation.z, planet, true);
        }
        if (planet.equals("kashyyyk_rryatt_trail") || planet.equals("kashyyyk_dead_forest") || planet.equals("kashyyyk_hunting"))
        {
            return isWithinAnySurveyRegion(surveyLocation.x, surveyLocation.z, planet, false);
        }
        for (float x = surveyLocation.x - SURVEY_GRID_RADIUS; x <= surveyLocation.x + SURVEY_GRID_RADIUS; x += SURVEY_GRID_RADIUS)
        {
            for (float z = surveyLocation.z - SURVEY_GRID_RADIUS; z <= surveyLocation.z + SURVEY_GRID_RADIUS; z += SURVEY_GRID_RADIUS)
            {
                if (!isAllowedSurveySample(planet, new location(x, 0, z, surveyLocation.area)))
                {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isAllowedKashyyykMainSurveyCenter(location surveyLocation) throws InterruptedException
    {
        return surveyLocation != null && isWithinAnySurveyRegion(surveyLocation.x, surveyLocation.z, "kashyyyk_main", false) && !isWithinAnySurveyRegion(surveyLocation.x, surveyLocation.z, "kashyyyk_main", true);
    }

    public static boolean isAllowedSurveySample(String planet, location surveyLocation) throws InterruptedException
    {
        return isWithinSceneBounds(planet, surveyLocation, 0.0f);
    }

    public static boolean isWithinSceneBounds(String planet, location surveyLocation, float inset) throws InterruptedException
    {
        if (surveyLocation == null || planet == null || !planet.equals(surveyLocation.area))
        {
            return false;
        }
        if (planet.equals("mustafar"))
        {
            float displayX = surveyLocation.x + 2880.0f;
            float displayZ = surveyLocation.z - 2976.0f;
            return displayX >= MUSTAFAR_DISPLAY_MIN_COORDINATE + inset && displayX <= MUSTAFAR_DISPLAY_MAX_COORDINATE - inset && displayZ >= MUSTAFAR_DISPLAY_MIN_COORDINATE + inset && displayZ <= MUSTAFAR_DISPLAY_MAX_COORDINATE - inset;
        }
        float maximumCoordinate = planet.equals("kashyyyk_rryatt_trail") ? 8000.0f : ((planet.equals("kashyyyk_dead_forest") || planet.equals("kashyyyk_hunting")) ? 2048.0f : (planet.equals("kashyyyk_main") ? 4096.0f : 0.0f));
        return maximumCoordinate > 0.0f && surveyLocation.x >= -maximumCoordinate + inset && surveyLocation.x <= maximumCoordinate - inset && surveyLocation.z >= -maximumCoordinate + inset && surveyLocation.z <= maximumCoordinate - inset;
    }

    public static boolean isWithinAnySurveyRegion(float x, float z, String planet, boolean exclusion)
    {
        if (!REGION_DATA_VALID || planet == null)
        {
            return false;
        }
        int excluded = exclusion ? 1 : 0;
        for (int start = 0; start < REGION_SCENES.length; )
        {
            int end = getRegionEnd(start);
            if (planet.equals(REGION_SCENES[start]) && REGION_EXCLUSIONS[start] == excluded)
            {
                if (isInPolygon(x, z, start, end) || (REGION_EDGE_OUTSETS[start] > 0.0f && isWithinPolygonOutset(x, z, start, end, REGION_EDGE_OUTSETS[start])))
                {
                    return true;
                }
            }
            start = end;
        }
        return false;
    }

    public static boolean hasValidRegionData()
    {
        if (REGION_SCENES == null || REGION_NAMES == null || REGION_ORDERS == null || REGION_EXCLUSIONS == null || REGION_POINT_ORDERS == null || REGION_X == null || REGION_Z == null || REGION_EDGE_OUTSETS == null || REGION_SCENES.length == 0)
        {
            return false;
        }
        int rowCount = REGION_SCENES.length;
        if (REGION_NAMES.length != rowCount || REGION_ORDERS.length != rowCount || REGION_EXCLUSIONS.length != rowCount || REGION_POINT_ORDERS.length != rowCount || REGION_X.length != rowCount || REGION_Z.length != rowCount || REGION_EDGE_OUTSETS.length != rowCount)
        {
            return false;
        }
        String previousScene = null;
        int expectedRegionOrder = 0;
        for (int start = 0; start < rowCount; )
        {
            int end = getRegionEnd(start);
            if (!isValidRegion(start, end))
            {
                return false;
            }
            if (REGION_SCENES[start].equals(previousScene))
            {
                ++expectedRegionOrder;
            }
            else
            {
                previousScene = REGION_SCENES[start];
                expectedRegionOrder = 0;
            }
            if (REGION_ORDERS[start] != expectedRegionOrder)
            {
                return false;
            }
            start = end;
        }
        return true;
    }

    public static int getRegionEnd(int start)
    {
        if (REGION_SCENES[start] == null || REGION_NAMES[start] == null)
        {
            return start + 1;
        }
        int end = start + 1;
        while (end < REGION_SCENES.length && REGION_SCENES[start].equals(REGION_SCENES[end]) && REGION_NAMES[start].equals(REGION_NAMES[end]) && REGION_ORDERS[start] == REGION_ORDERS[end] && REGION_EXCLUSIONS[start] == REGION_EXCLUSIONS[end])
        {
            ++end;
        }
        return end;
    }

    public static boolean isValidRegion(int start, int end)
    {
        if (end - start < 3 || REGION_SCENES[start] == null || REGION_SCENES[start].equals("") || REGION_NAMES[start] == null || REGION_NAMES[start].equals("") || REGION_ORDERS[start] < 0 || REGION_EXCLUSIONS[start] < 0 || REGION_EXCLUSIONS[start] > 1 || REGION_EDGE_OUTSETS[start] < 0.0f || Float.isNaN(REGION_EDGE_OUTSETS[start]) || Float.isInfinite(REGION_EDGE_OUTSETS[start]))
        {
            return false;
        }
        for (int row = start; row < end; ++row)
        {
            if (REGION_POINT_ORDERS[row] != row - start || REGION_EDGE_OUTSETS[row] != REGION_EDGE_OUTSETS[start] || Float.isNaN(REGION_X[row]) || Float.isInfinite(REGION_X[row]) || Float.isNaN(REGION_Z[row]) || Float.isInfinite(REGION_Z[row]))
            {
                return false;
            }
        }
        return true;
    }

    public static boolean isWithinPolygonOutset(float x, float z, int start, int end, float outset)
    {
        float outsetSquared = outset * outset;
        for (int current = start, previous = end - 1; current < end; previous = current++)
        {
            float startX = REGION_X[previous];
            float startZ = REGION_Z[previous];
            float deltaX = REGION_X[current] - startX;
            float deltaZ = REGION_Z[current] - startZ;
            float edgeLengthSquared = deltaX * deltaX + deltaZ * deltaZ;
            float projection = edgeLengthSquared > 0.0f ? ((x - startX) * deltaX + (z - startZ) * deltaZ) / edgeLengthSquared : 0.0f;
            projection = Math.max(0.0f, Math.min(1.0f, projection));
            float nearestX = startX + projection * deltaX;
            float nearestZ = startZ + projection * deltaZ;
            float distanceX = x - nearestX;
            float distanceZ = z - nearestZ;
            if (distanceX * distanceX + distanceZ * distanceZ <= outsetSquared)
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isInPolygon(float x, float z, int start, int end)
    {
        boolean inside = false;
        for (int current = start, previous = end - 1; current < end; previous = current++)
        {
            float currentX = REGION_X[current];
            float currentZ = REGION_Z[current];
            float previousX = REGION_X[previous];
            float previousZ = REGION_Z[previous];
            float crossProduct = (x - currentX) * (previousZ - currentZ) - (z - currentZ) * (previousX - currentX);
            if (Math.abs(crossProduct) < 0.001f && x >= Math.min(currentX, previousX) && x <= Math.max(currentX, previousX) && z >= Math.min(currentZ, previousZ) && z <= Math.max(currentZ, previousZ))
            {
                return true;
            }
            if ((currentZ > z) != (previousZ > z) && x < (previousX - currentX) * (z - currentZ) / (previousZ - currentZ) + currentX)
            {
                inside = !inside;
            }
        }
        return inside;
    }
}
