/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  script.base_script
 *  script.dictionary
 *  script.library.ai_lib
 *  script.library.create
 *  script.library.locations
 *  script.library.planetary_map
 *  script.library.prose
 *  script.library.utils
 *  script.location
 *  script.map_location
 *  script.obj_id
 *  script.prose_package
 *  script.region
 *  script.string_id
 */
package script.holocron;

import java.util.StringTokenizer;
import java.util.Vector;
import script.base_script;
import script.dictionary;
import script.library.ai_lib;
import script.library.create;
import script.library.locations;
import script.library.planetary_map;
import script.library.prose;
import script.library.utils;
import script.location;
import script.map_location;
import script.obj_id;
import script.prose_package;
import script.region;
import script.string_id;

public class newbie_handoff
extends base_script {
    private static final int MAX_DESTROY_TARGETS = 10;
    private static final int MAX_SEARCH_RANGE = 500;
    public static final String STF_PLANET_MAP_CAT = "map_loc_cat_n";
    private static final String TARGET_DATATABLE = "datatables/newbie_handoff/destroy_targets.iff";
    private static final String DATATABLE_HOLOCRON_EVENTS = "datatables/holocron/events.iff";
    private static final String VAR_FIND_BASE = "find";
    public static final String VAR_FIND_WAYPOINT = "find.waypoint";
    private static final String[] NPC_TYPES = new String[]{"artisan", "bodyguard", "businessman", "bothan_diplomat", "entertainer", "explorer", "farmer", "gambler", "info_broker", "miner", "medic", "noble", "pilot", "scientist"};
    private static final string_id SID_NEWBIE_MISSION_DESTROY_START = new string_id("newbie_handoff/messages", "destroy_mission_start");
    private static final string_id SID_NEWBIE_MISSION_DESTROY_COMPLETE = new string_id("newbie_handoff/messages", "destroy_mission_complete");
    private static final string_id PROSE_NEWBIE_MISSION_SURVEY_START = new string_id("newbie_handoff/messages", "survey_mission_start");
    private static final string_id SID_NEWBIE_MISSION_SURVEY_COMPLETE = new string_id("newbie_handoff/messages", "survey_mission_complete");
    private static final string_id SID_NEWBIE_MISSION_GIG_DANCE_START = new string_id("newbie_handoff/messages", "gig_mission_dance_start");
    private static final string_id SID_NEWBIE_MISSION_GIG_MUSIC_START = new string_id("newbie_handoff/messages", "gig_mission_music_start");
    private static final string_id SID_NEWBIE_MISSION_GIG_ARRIVAL = new string_id("newbie_handoff/messages", "gig_mission_arrival");
    private static final string_id SID_NEWBIE_MISSION_GIG_COMPLETE = new string_id("newbie_handoff/messages", "gig_mission_complete");
    private static final string_id PROSE_NEWBIE_MISSION_HEAL_START = new string_id("newbie_handoff/messages", "heal_mission_start");
    private static final string_id PROSE_NEWBIE_MISSION_HEAL_ARRIVAL = new string_id("newbie_handoff/messages", "heal_mission_arrival");
    private static final string_id SID_NEWBIE_MISSION_HEAL_COMPLETE = new string_id("newbie_handoff/messages", "heal_mission_complete");
    public static final string_id SID_NEWBIE_MISSION_HEAL_MENU = new string_id("newbie_handoff/messages", "heal_mission_menu");
    public static final string_id SID_NEWBIE_MISSION_HEAL_USE = new string_id("newbie_handoff/messages", "heal_mission_use");
    public static final string_id SID_NEWBIE_MISSION_HEAL_NEED_TARGET = new string_id("newbie_handoff/messages", "heal_mission_need_target");
    public static final string_id SID_NEWBIE_MISSION_HEAL_WRONG_TARGET = new string_id("newbie_handoff/messages", "heal_mission_wrong_target");
    private static final string_id SID_NEWBIE_MISSION_HARVEST_START = new string_id("newbie_handoff/messages", "harvest_mission_start");
    private static final string_id SID_NEWBIE_MISSION_HARVEST_KILL = new string_id("newbie_handoff/messages", "harvest_mission_target_dead");
    private static final string_id SID_NEWBIE_MISSION_HARVEST_COMPLETE = new string_id("newbie_handoff/messages", "harvest_mission_complete");

    public int OnAttach(obj_id self) throws InterruptedException {
        if (newbie_handoff.isJedi((obj_id)self)) {
            return 1;
        }
        return 1;
    }

    public int OnSpeaking(obj_id self, String text) throws InterruptedException {
        if (!newbie_handoff.hasObjVar((obj_id)self, (String)"gm")) {
            return 1;
        }
        String arg = this.firstTokenOrWhole(text);
        if (arg != null && arg.startsWith("/")) {
            arg = arg.substring(1);
        }
        this.handleNewbieHandoffAction(self, arg);
        return 1;
    }

    private int handleNewbieHandoffAction(obj_id self, String arg) throws InterruptedException {
        if (arg == null || arg.equals("")) {
            return 1;
        }
        switch (arg) {
            case "findBank": {
                newbie_handoff.LOG((String)"newbie_handoff", (String)"Attempting to find closest Bank");
                newbie_handoff.messageTo((obj_id)self, (String)"tourBank", null, (float)0.0f, (boolean)false);
                break;
            }
            case "findCantina": {
                newbie_handoff.LOG((String)"newbie_handoff", (String)"Attempting to find closest Cantina");
                newbie_handoff.messageTo((obj_id)self, (String)"tourCantina", null, (float)0.0f, (boolean)false);
                break;
            }
            case "findHospital": {
                newbie_handoff.LOG((String)"newbie_handoff", (String)"Attempting to find closest Hospital");
                newbie_handoff.messageTo((obj_id)self, (String)"tourHospital", null, (float)0.0f, (boolean)false);
                break;
            }
            case "findSkillTrainer": {
                newbie_handoff.LOG((String)"newbie_handoff", (String)"Attempting to find closest Skill Trainer");
                newbie_handoff.messageTo((obj_id)self, (String)"tourSkillTrainer", null, (float)0.0f, (boolean)false);
                break;
            }
            case "holocronProfession": {
                newbie_handoff.LOG((String)"newbie_handoff", (String)"Opening Holocron to my profession");
                newbie_handoff.messageTo((obj_id)self, (String)"openHolocronToProfession", null, (float)0.0f, (boolean)false);
                break;
            }
            case "missionArtisan": {
                newbie_handoff.LOG((String)"newbie_handoff", (String)"Creating new Artisan Mission");
                newbie_handoff.messageTo((obj_id)self, (String)"missionArtisanSurvey", null, (float)0.0f, (boolean)false);
                break;
            }
            case "missionDancer": {
                newbie_handoff.LOG((String)"newbie_handoff", (String)"Creating new Dance Mission");
                newbie_handoff.messageTo((obj_id)self, (String)"missionEntertainerDanceGig", null, (float)0.0f, (boolean)false);
                break;
            }
            case "missionMusician": {
                newbie_handoff.LOG((String)"newbie_handoff", (String)"Creating new Music Mission");
                newbie_handoff.messageTo((obj_id)self, (String)"missionEntertainerMusicGig", null, (float)0.0f, (boolean)false);
                break;
            }
            case "missionMedic": {
                newbie_handoff.LOG((String)"newbie_handoff", (String)"Creating new Medic Mission");
                newbie_handoff.messageTo((obj_id)self, (String)"missionMedicHeal", null, (float)0.0f, (boolean)false);
                break;
            }
            case "missionBrawler": {
                newbie_handoff.LOG((String)"newbie_handoff", (String)"Creating new Brawler Mission");
                newbie_handoff.messageTo((obj_id)self, (String)"missionBrawlerDestroy", null, (float)0.0f, (boolean)false);
                break;
            }
            case "missionMarksman": {
                newbie_handoff.LOG((String)"newbie_handoff", (String)"Creating new Marksman Mission");
                newbie_handoff.messageTo((obj_id)self, (String)"missionMarksmanDestroy", null, (float)0.0f, (boolean)false);
                break;
            }
            case "missionScout": {
                newbie_handoff.LOG((String)"newbie_handoff", (String)"Creating new Scout Mission");
                newbie_handoff.messageTo((obj_id)self, (String)"missionScoutHarvest", null, (float)0.0f, (boolean)false);
                break;
            }
        }
        return 1;
    }

    private String firstTokenOrWhole(String text) throws InterruptedException {
        if (text == null) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(text);
        if (st.countTokens() > 0) {
            return st.nextToken();
        }
        return text;
    }

    public int OnArrivedAtLocation(obj_id self, String name) throws InterruptedException {
        switch (name) {
            case "tour.bank": {
                this.openHolocronToNewbiePage(self, "WelcomeToSWG.WhereDoIBegin.Exploring.FindBank.ArrivedBank");
                this.clearHolocronDatapadEntry(self, name);
                break;
            }
            case "tour.cloning_center": {
                this.openHolocronToNewbiePage(self, "WelcomeToSWG.WhereDoIBegin.Exploring.FindCloning.ArrivedCloning");
                this.clearHolocronDatapadEntry(self, name);
                break;
            }
            case "tour.hospital": {
                this.openHolocronToNewbiePage(self, "WelcomeToSWG.WhereDoIBegin.Exploring.FindHospital.ArrivedHospital");
                this.clearHolocronDatapadEntry(self, name);
                break;
            }
            case "tour.cantina": {
                this.openHolocronToNewbiePage(self, "WelcomeToSWG.WhereDoIBegin.Exploring.FindCantina.ArrivedCantina");
                this.clearHolocronDatapadEntry(self, name);
                break;
            }
            case "tour.skill_trainer": {
                this.openHolocronToNewbiePage(self, "WelcomeToSWG.WhereDoIBegin.Exploring.FindSkillTrainer.ArrivedSkillTrainer");
                this.clearHolocronDatapadEntry(self, name);
                break;
            }
            case "mission.destroy": {
                newbie_handoff.removeLocationTarget((String)name);
                break;
            }
            case "mission.destroy.spawnGuy": {
                newbie_handoff.removeLocationTarget((String)name);
                location spawnLoc = newbie_handoff.getLocationObjVar((obj_id)self, (String)"newbie_handoff.mission.destroy.loc");
                String npcToSpawn = newbie_handoff.getStringObjVar((obj_id)self, (String)"newbie_handoff.mission.destroy.npc");
                String missionType = newbie_handoff.getStringObjVar((obj_id)self, (String)"newbie_handoff.mission.destroy.type");
                if (npcToSpawn == null) {
                    newbie_handoff.debugSpeakMsg((obj_id)self, (String)"Can't find anyone to spawn");
                    return 1;
                }
                obj_id npc = create.object((String)npcToSpawn, (location)spawnLoc);
                newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.destroy.npc_id", (obj_id)npc);
                newbie_handoff.pvpSetAlignedFaction((obj_id)npc, (int)84709322);
                newbie_handoff.pvpSetPermanentPersonalEnemyFlag((obj_id)npc, (obj_id)self);
                newbie_handoff.setObjVar((obj_id)npc, (String)"newbie_handoff.mission.type", (String)missionType);
                newbie_handoff.setObjVar((obj_id)npc, (String)"newbie_handoff.mission.player", (obj_id)self);
                newbie_handoff.attachScript((obj_id)npc, (String)"holocron.destroy_target");
                break;
            }
            case "mission.harvest": {
                newbie_handoff.removeLocationTarget((String)name);
                break;
            }
            case "mission.harvest.spawnGuy": {
                newbie_handoff.removeLocationTarget((String)name);
                location spawnLoc = newbie_handoff.getLocationObjVar((obj_id)self, (String)"newbie_handoff.mission.harvest.loc");
                String npcToSpawn = newbie_handoff.getStringObjVar((obj_id)self, (String)"newbie_handoff.mission.harvest.npc");
                String missionType = newbie_handoff.getStringObjVar((obj_id)self, (String)"newbie_handoff.mission.harvest.type");
                if (npcToSpawn == null) {
                    newbie_handoff.debugSpeakMsg((obj_id)self, (String)"Can't find anyone to spawn");
                    return 1;
                }
                obj_id npc = create.object((String)npcToSpawn, (location)spawnLoc);
                newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.harvest.npc_id", (obj_id)npc);
                newbie_handoff.pvpSetAlignedFaction((obj_id)npc, (int)84709322);
                newbie_handoff.pvpSetPermanentPersonalEnemyFlag((obj_id)npc, (obj_id)self);
                newbie_handoff.setObjVar((obj_id)npc, (String)"newbie_handoff.mission.type", (String)missionType);
                newbie_handoff.setObjVar((obj_id)npc, (String)"newbie_handoff.mission.player", (obj_id)self);
                newbie_handoff.attachScript((obj_id)npc, (String)"holocron.destroy_target");
                break;
            }
            case "mission.gig": {
                newbie_handoff.removeLocationTarget((String)name);
                newbie_handoff.sendSystemMessage((obj_id)self, (string_id)SID_NEWBIE_MISSION_GIG_ARRIVAL);
                break;
            }
            case "mission.heal": {
                newbie_handoff.removeLocationTarget((String)name);
                obj_id npc = newbie_handoff.getObjIdObjVar((obj_id)self, (String)"newbie_handoff.mission.heal.npc_id");
                prose_package pp = prose.getPackage((string_id)PROSE_NEWBIE_MISSION_HEAL_ARRIVAL, (obj_id)npc);
                newbie_handoff.sendSystemMessageProse((obj_id)self, (prose_package)pp);
                break;
            }
            default: {
                this.clearHolocronDatapadEntry(self, name);
            }
        }
        return 1;
    }

    public int startHandoff(obj_id self, dictionary params) throws InterruptedException {
        newbie_handoff.playMusic((obj_id)self, (String)"sound/tut_00_holocron.snd");
        newbie_handoff.sendSystemMessageTestingOnly((obj_id)self, (String)"newbie_handoff: startHandoff fired; attempting openHolocronToNewbiePage...");
        this.openHolocronToNewbiePage(self, "WelcomeToSWG.WhereDoIBegin");
        // POC: force direct holocron open and log return
        boolean directOpened = openHolocronToPage(self, "WelcomeToSWG");
        sendSystemMessageTestingOnly(self, "[HOL-O-POC] openHolocronToPage('WelcomeToSWG') returned: " + directOpened);
        newbie_handoff.sendSystemMessageTestingOnly((obj_id)self, (String)"newbie_handoff: openHolocronToNewbiePage returned (UI may still be client-side).");
        return 1;
    }

    public int disableNewbieHandoff(obj_id self, dictionary params) throws InterruptedException {
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.disabled", (int)1);
        return 1;
    }

    public int enableNewbieHandoff(obj_id self, dictionary params) throws InterruptedException {
        newbie_handoff.removeObjVar((obj_id)self, (String)"newbie_handoff.disabled");
        return 1;
    }

    public int tourBank(obj_id self, dictionary params) throws InterruptedException {
        if (newbie_handoff.hasObjVar((obj_id)self, (String)"newbie_handoff.tour.bank")) {
            this.clearHolocronDatapadEntry(self, "tour.bank");
            newbie_handoff.messageTo((obj_id)self, (String)"tourBank", null, (float)1.0f, (boolean)false);
            return 1;
        }
        map_location destination = planetary_map.findClosestLocation((obj_id)self, (String)"bank", (String)"");
        location loc = new location((float)destination.getX(), 0.0f, (float)destination.getY());
        obj_id waypoint = this.addHolocronDatapadWaypoint(self, "tour.bank", "City Tour: Bank", loc, 20.0f);
        this.addHolocronWaypointPath(self, waypoint);
        newbie_handoff.closeHolocron((obj_id)self);
        return 1;
    }

    public int tourCloningCenter(obj_id self, dictionary params) throws InterruptedException {
        if (newbie_handoff.hasObjVar((obj_id)self, (String)"newbie_handoff.tour.cloning_center")) {
            this.clearHolocronDatapadEntry(self, "tour.cloning_center");
            newbie_handoff.messageTo((obj_id)self, (String)"tourCloningCenter", null, (float)1.0f, (boolean)false);
            return 1;
        }
        this.clearHolocronDatapadEntry(self, "tour.cloning_center");
        map_location destination = planetary_map.findClosestLocation((obj_id)self, (String)"cloningfacility", (String)"");
        location loc = new location((float)destination.getX(), 0.0f, (float)destination.getY());
        obj_id waypoint = this.addHolocronDatapadWaypoint(self, "tour.cloning_center", "City Tour: Cloning Center", loc, 5.0f);
        this.addHolocronWaypointPath(self, waypoint);
        newbie_handoff.closeHolocron((obj_id)self);
        return 1;
    }

    public int tourHospital(obj_id self, dictionary params) throws InterruptedException {
        if (newbie_handoff.hasObjVar((obj_id)self, (String)"newbie_handoff.tour.hospital")) {
            this.clearHolocronDatapadEntry(self, "tour.hospital");
            newbie_handoff.messageTo((obj_id)self, (String)"tourHospital", null, (float)1.0f, (boolean)false);
            return 1;
        }
        String[] priSearchList = new String[]{"hospital", "medicalcenter"};
        String[] secSearchList = new String[]{"tavern", "barracks"};
        obj_id waypoint = this.addHolocronDatapadWaypoint(self, "tour.hospital", "City Tour: Hospital", this.findClosestStructure(self, priSearchList, secSearchList), 5.0f);
        this.addHolocronWaypointPath(self, waypoint);
        newbie_handoff.closeHolocron((obj_id)self);
        return 1;
    }

    public int tourCantina(obj_id self, dictionary params) throws InterruptedException {
        if (newbie_handoff.hasObjVar((obj_id)self, (String)"newbie_handoff.tour.cantina")) {
            this.clearHolocronDatapadEntry(self, "tour.cantina");
            newbie_handoff.messageTo((obj_id)self, (String)"tourCantina", null, (float)1.0f, (boolean)false);
            return 1;
        }
        String[] priSearchList = new String[]{"cantina", "guild_theater", "hotel"};
        String[] secSearchList = new String[]{"tavern", "barracks"};
        obj_id waypoint = this.addHolocronDatapadWaypoint(self, "tour.cantina", "City Tour: Cantina", this.findClosestStructure(self, priSearchList, secSearchList), 5.0f);
        this.addHolocronWaypointPath(self, waypoint);
        newbie_handoff.closeHolocron((obj_id)self);
        return 1;
    }

    public int tourSkillTrainer(obj_id self, dictionary params) throws InterruptedException {
        if (newbie_handoff.hasObjVar((obj_id)self, (String)"newbie_handoff.tour.skill_trainer")) {
            this.clearHolocronDatapadEntry(self, "tour.skill_trainer");
            newbie_handoff.messageTo((obj_id)self, (String)"tourSkillTrainer", null, (float)1.0f, (boolean)false);
            return 1;
        }
        String profession = this.getStartingProfession(self);
        String trainer_type = this.getTrainerType(profession);
        newbie_handoff.LOG((String)"newbie_handoff", (String)("Looking for trainer: " + profession + ", " + trainer_type));
        map_location destination = planetary_map.findClosestLocation((obj_id)self, (String)"trainer", (String)trainer_type);
        if (destination == null) {
            return 1;
        }
        obj_id waypoint = this.addHolocronDatapadWaypoint(self, "tour.skill_trainer", "City Tour: Skill Trainer", new location((float)destination.getX(), 0.0f, (float)destination.getY()), 5.0f);
        this.addHolocronWaypointPath(self, waypoint);
        newbie_handoff.closeHolocron((obj_id)self);
        return 1;
    }

    public int openHolocronToProfession(obj_id self, dictionary params) throws InterruptedException {
        String profession = newbie_handoff.toUpper((String)this.getSimpleProfession(this.getStartingProfession(self)), (int)0);
        String targetpage = "WelcomeToSWG.WhereDoIBegin.Professions.Playing" + profession;
        String professionKey = this.getStartingProfession(self);
        String suffix = this.getHolocronProfessionSuffix(professionKey);
        if (suffix == null || suffix.equals("")) {
            this.openHolocronToNewbiePage(self, "WelcomeToSWG.WhereDoIBegin.Professions.Playing");
            return 1;
        }
        String page = "WelcomeToSWG.WhereDoIBegin.Professions.Playing" + suffix;
        this.openHolocronToNewbiePage(self, page);
        return 1;
    }

    public int missionArtisanSurvey(obj_id self, dictionary params) throws InterruptedException {
        if (newbie_handoff.hasObjVar((obj_id)self, (String)"newbie_handoff.mission.survey")) {
            this.clearHolocronDatapadEntry(self, "mission.survey");
            newbie_handoff.messageTo((obj_id)self, (String)"missionArtisanSurvey", null, (float)1.0f, (boolean)false);
            return 1;
        }
        int MAX_RESOURCES = 10;
        obj_id[] resource = new obj_id[10];
        float[] efficiency = new float[10];
        obj_id resourceMax = null;
        float efficiencyMax = 0.0f;
        int resourceCount = 0;
        location here = newbie_handoff.getLocation((obj_id)self);
        for (int x = 0; x < 50; ++x) {
            obj_id curResource = newbie_handoff.pickRandomNonDepeletedResource((String)"mineral");
            float curEfficiency = newbie_handoff.getResourceEfficiency((obj_id)curResource, (location)here);
            if ((double)curEfficiency > 0.4 && (double)curEfficiency < 0.55) {
                resource[resourceCount] = curResource;
                efficiency[resourceCount] = curEfficiency;
                ++resourceCount;
            }
            if (curEfficiency > efficiencyMax && (double)curEfficiency < 0.55) {
                resourceMax = curResource;
                efficiencyMax = curEfficiency;
            }
            if (resourceCount >= 10) break;
        }
        int randResource = newbie_handoff.rand((int)0, (int)(resourceCount - 1));
        obj_id targetResource = resource[randResource];
        float targetEfficiency = efficiency[randResource];
        if (resourceCount == 0) {
            randResource = -1;
            targetResource = resourceMax;
            targetEfficiency = efficiencyMax;
        }
        targetEfficiency = (float)((double)targetEfficiency + (0.7 - (double)targetEfficiency) * (double)0.15f);
        newbie_handoff.LOG((String)"newbie_handoff", (String)("Survey Mission Create:  Using Resource #" + randResource + " of " + resourceCount + " resources."));
        newbie_handoff.LOG((String)"newbie_handoff", (String)("\t\t\tTarget Resource: " + targetResource + "; Target Efficiency: " + targetEfficiency));
        this.addHolocronDatapadEntry(self, "mission.survey", "Artisan Survey Mission");
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.survey.type", (String)"artisan");
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.survey.loc", (location)here);
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.survey.resource", (obj_id)targetResource);
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.survey.efficiency", (float)targetEfficiency);
        newbie_handoff.closeHolocron((obj_id)self);
        String resourceName = "INSERT NAME HERE";
        String resourceClass = "INSERT CLASS HERE";
        int resourceEfficiency = (int)(targetEfficiency * 100.0f);
        prose_package pp = prose.getPackage((string_id)PROSE_NEWBIE_MISSION_SURVEY_START, null, null, null, null, (String)resourceName, null, null, (String)resourceClass, null, (int)resourceEfficiency, (float)0.0f);
        newbie_handoff.sendSystemMessageProse((obj_id)self, (prose_package)pp);
        return 1;
    }

    public int missionEntertainerDanceGig(obj_id self, dictionary params) throws InterruptedException {
        if (newbie_handoff.hasObjVar((obj_id)self, (String)"newbie_handoff.mission.gig")) {
            this.clearHolocronDatapadEntry(self, "mission.gig");
            newbie_handoff.messageTo((obj_id)self, (String)"missionEntertainerDanceGig", null, (float)1.0f, (boolean)false);
            return 1;
        }
        map_location destination = planetary_map.findClosestLocation((obj_id)self, (String)"cantina", (String)"");
        location loc = new location((float)destination.getX(), 0.0f, (float)destination.getY());
        this.addHolocronDatapadWaypoint(self, "mission.gig", "Entertainer Dance Mission", loc, 25.0f);
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.gig.type", (String)"dancer");
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.gig.loc", (location)loc);
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.gig.obj", (obj_id)destination.getLocationId());
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.gig.duration", (int)120);
        newbie_handoff.closeHolocron((obj_id)self);
        newbie_handoff.sendSystemMessage((obj_id)self, (string_id)SID_NEWBIE_MISSION_GIG_DANCE_START);
        return 1;
    }

    public int missionEntertainerMusicGig(obj_id self, dictionary params) throws InterruptedException {
        if (newbie_handoff.hasObjVar((obj_id)self, (String)"newbie_handoff.mission.gig")) {
            this.clearHolocronDatapadEntry(self, "mission.gig");
            newbie_handoff.messageTo((obj_id)self, (String)"missionEntertainerMusicGig", null, (float)1.0f, (boolean)false);
            return 1;
        }
        map_location destination = planetary_map.findClosestLocation((obj_id)self, (String)"cantina", (String)"");
        location loc = new location((float)destination.getX(), 0.0f, (float)destination.getY());
        this.addHolocronDatapadWaypoint(self, "mission.gig", "Entertainer Music Mission", loc, 25.0f);
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.gig.type", (String)"musician");
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.gig.loc", (location)loc);
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.gig.obj", (obj_id)destination.getLocationId());
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.gig.duration", (int)120);
        newbie_handoff.closeHolocron((obj_id)self);
        newbie_handoff.sendSystemMessage((obj_id)self, (string_id)SID_NEWBIE_MISSION_GIG_MUSIC_START);
        return 1;
    }

    public int missionMedicHeal(obj_id self, dictionary params) throws InterruptedException {
        if (newbie_handoff.hasObjVar((obj_id)self, (String)"newbie_handoff.mission.heal")) {
            this.clearHolocronDatapadEntry(self, "mission.heal");
            newbie_handoff.messageTo((obj_id)self, (String)"missionMedicHeal", null, (float)1.0f, (boolean)false);
            return 1;
        }
        String planet = newbie_handoff.getCurrentSceneName();
        region city = locations.getCityRegion((location)newbie_handoff.getLocation((obj_id)self));
        if (city == null || planet == null) {
            return 1;
        }
        location destination = null;
        for (int x = 0; x < 10; ++x) {
            newbie_handoff.LOG((String)"newbie_handoff", (String)("Medic Mission Create:  Attepmt #" + (x + 1) + "; Distance=" + (10 + x * 10)));
            destination = locations.getGoodCityLocation((region)city, (String)planet);
            if (destination != null) break;
        }
        if (destination == null) {
            return 1;
        }
        this.addHolocronDatapadWaypoint(self, "mission.heal", "Medic Heal Mission", destination, 5.0f);
        obj_id npc = create.object((String)NPC_TYPES[newbie_handoff.rand((int)0, (int)(NPC_TYPES.length - 1))], (location)destination);
        newbie_handoff.setObjVar((obj_id)npc, (String)"newbie_handoff.mission.type", (String)"medic");
        newbie_handoff.setObjVar((obj_id)npc, (String)"newbie_handoff.mission.player", (obj_id)self);
        newbie_handoff.attachScript((obj_id)npc, (String)"holocron.heal_target");
        obj_id inv = newbie_handoff.getObjectInSlot((obj_id)self, (String)"inventory");
        obj_id med = newbie_handoff.createObject((String)"object/tangible/medicine/newbie_heal_mission_medicine.iff", (obj_id)inv, (String)"");
        newbie_handoff.setObjVar((obj_id)med, (String)"newbie_handoff.mission.type", (String)"medic");
        newbie_handoff.setObjVar((obj_id)med, (String)"newbie_handoff.mission.player", (obj_id)self);
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.heal.type", (String)"medic");
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.heal.loc", (location)destination);
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.heal.npc_id", (obj_id)npc);
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.heal.med_id", (obj_id)med);
        newbie_handoff.closeHolocron((obj_id)self);
        prose_package pp = prose.getPackage((string_id)PROSE_NEWBIE_MISSION_HEAL_START, (obj_id)npc);
        newbie_handoff.sendSystemMessageProse((obj_id)self, (prose_package)pp);
        return 1;
    }

    public int missionBrawlerDestroy(obj_id self, dictionary params) throws InterruptedException {
        if (newbie_handoff.hasObjVar((obj_id)self, (String)"newbie_handoff.mission.destroy")) {
            this.clearHolocronDatapadEntry(self, "mission.destroy");
            newbie_handoff.messageTo((obj_id)self, (String)"missionBrawlerDestroy", null, (float)1.0f, (boolean)false);
            return 1;
        }
        region city = locations.getCityRegion((location)newbie_handoff.getLocation((obj_id)self));
        if (city == null) {
            return 1;
        }
        location destination = null;
        for (int x = 0; x < 10; ++x) {
            newbie_handoff.LOG((String)"newbie_handoff", (String)("Brawler Mission Create:  Attepmt #" + (x + 1) + "; Distance=" + (10 + x * 10)));
            destination = locations.getGoodLocationOutsideOfRegion((region)city, (float)30.0f, (float)30.0f, (float)(10 + x * 10), (boolean)false, (boolean)true);
            if (destination != null) break;
        }
        if (destination == null) {
            return 1;
        }
        this.addHolocronDatapadWaypoint(self, "mission.destroy", "Brawler Destroy Mission", destination, 25.0f);
        newbie_handoff.addLocationTarget((String)"mission.destroy.spawnGuy", destination, (float)100.0f);
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.destroy.type", (String)"brawler");
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.destroy.loc", destination);
        String npc = this.determineDestroyTarget(self);
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.destroy.npc", (String)npc);
        newbie_handoff.closeHolocron((obj_id)self);
        newbie_handoff.sendSystemMessage((obj_id)self, (string_id)SID_NEWBIE_MISSION_DESTROY_START);
        return 1;
    }

    public int missionMarksmanDestroy(obj_id self, dictionary params) throws InterruptedException {
        if (newbie_handoff.hasObjVar((obj_id)self, (String)"newbie_handoff.mission.destroy")) {
            this.clearHolocronDatapadEntry(self, "mission.destroy");
            newbie_handoff.messageTo((obj_id)self, (String)"missionMarksmanDestroy", null, (float)1.0f, (boolean)false);
            return 1;
        }
        region city = locations.getCityRegion((location)newbie_handoff.getLocation((obj_id)self));
        if (city == null) {
            return 1;
        }
        location destination = null;
        for (int x = 0; x < 10; ++x) {
            newbie_handoff.LOG((String)"newbie_handoff", (String)("Marksman Mission Create:  Attepmt #" + (x + 1) + "; Distance=" + (10 + x * 10)));
            destination = locations.getGoodLocationOutsideOfRegion((region)city, (float)30.0f, (float)30.0f, (float)(10 + x * 10), (boolean)false, (boolean)true);
            if (destination != null) break;
        }
        if (destination == null) {
            return 1;
        }
        this.addHolocronDatapadWaypoint(self, "mission.destroy", "Marksman Destroy Mission", destination, 25.0f);
        newbie_handoff.addLocationTarget((String)"mission.destroy.spawnGuy", destination, (float)100.0f);
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.destroy.type", (String)"marksman");
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.destroy.loc", destination);
        String npc = this.determineDestroyTarget(self);
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.destroy.npc", (String)npc);
        newbie_handoff.closeHolocron((obj_id)self);
        newbie_handoff.sendSystemMessage((obj_id)self, (string_id)SID_NEWBIE_MISSION_DESTROY_START);
        return 1;
    }

    public int missionScoutHarvest(obj_id self, dictionary params) throws InterruptedException {
        if (newbie_handoff.hasObjVar((obj_id)self, (String)"newbie_handoff.mission.harvest")) {
            this.clearHolocronDatapadEntry(self, "mission.harvest");
            newbie_handoff.messageTo((obj_id)self, (String)"missionScoutHarvest", null, (float)1.0f, (boolean)false);
            return 1;
        }
        region city = locations.getCityRegion((location)newbie_handoff.getLocation((obj_id)self));
        if (city == null) {
            return 1;
        }
        location destination = null;
        for (int x = 0; x < 10; ++x) {
            newbie_handoff.LOG((String)"newbie_handoff", (String)("Scout Mission Create:  Attepmt #" + (x + 1) + "; Distance=" + (10 + x * 10)));
            destination = locations.getGoodLocationOutsideOfRegion((region)city, (float)30.0f, (float)30.0f, (float)(10 + x * 10), (boolean)false, (boolean)true);
            if (destination != null) break;
        }
        if (destination == null) {
            return 1;
        }
        this.addHolocronDatapadWaypoint(self, "mission.harvest", "Scout Harvest Mission", destination, 25.0f);
        newbie_handoff.addLocationTarget((String)"mission.harvest.spawnGuy", destination, (float)100.0f);
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.harvest.type", (String)"scout");
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.harvest.loc", destination);
        String npc = this.determineDestroyTarget(self);
        newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.mission.harvest.npc", (String)npc);
        newbie_handoff.closeHolocron((obj_id)self);
        newbie_handoff.sendSystemMessage((obj_id)self, (string_id)SID_NEWBIE_MISSION_HARVEST_START);
        return 1;
    }

    public void commandOpenCraftingTool(obj_id player) throws InterruptedException {
    }

    public void prototypeCompleted() throws InterruptedException {
    }

    public int eventEarnedSkillBox(obj_id self, obj_id target, String params, float defaultTime) throws InterruptedException {
        if (newbie_handoff.isSpaceScene()) {
            return 1;
        }
        dictionary holocronParams = new dictionary();
        holocronParams.put((Object)"eventName", "EarnedSkillBox");
        newbie_handoff.messageTo((obj_id)self, (String)"handleHolocronEvent", (dictionary)holocronParams, (float)0.0f, (boolean)false);
        return 1;
    }

    public int eventTrainedSkillBox(obj_id self, dictionary params) throws InterruptedException {
        dictionary holocronParams = new dictionary();
        holocronParams.put((Object)"eventName", "TrainedSkillBox");
        newbie_handoff.messageTo((obj_id)self, (String)"handleHolocronEvent", (dictionary)holocronParams, (float)0.0f, (boolean)false);
        return 1;
    }

    public int handleHolocronEvent(obj_id self, dictionary params) throws InterruptedException {
        if (newbie_handoff.isSpaceScene()) {
            return 1;
        }
        if (newbie_handoff.hasObjVar((obj_id)self, (String)"newbie_handoff.disabled")) {
            return 1;
        }
        if (newbie_handoff.isJedi((obj_id)self)) {
            return 1;
        }
        String event = params.getString((Object)"eventName");
        int eventFlag = newbie_handoff.dataTableGetInt((String)DATATABLE_HOLOCRON_EVENTS, (String)event, (String)"eventFlag");
        String eventEntry = newbie_handoff.dataTableGetString((String)DATATABLE_HOLOCRON_EVENTS, (String)event, (String)"eventEntry");
        if (eventFlag == -1 || eventEntry == null || eventEntry.equals("")) {
            return 1;
        }
        int bitFieldNum = eventFlag / 32;
        int bitFlag = eventFlag % 32;
        int bitField = newbie_handoff.getIntObjVar((obj_id)self, (String)("newbie_handoff.events.bitField" + bitFieldNum));
        if (!utils.checkBit((int)bitField, (int)bitFlag)) {
            bitField = utils.setBit((int)bitField, (int)bitFlag);
            newbie_handoff.setObjVar((obj_id)self, (String)("newbie_handoff.events.bitField" + bitFieldNum), (int)bitField);
            this.openHolocronToNewbiePage(self, eventEntry);
        }
        return 1;
    }

    public int missionSurveyComplete(obj_id self, dictionary params) throws InterruptedException {
        newbie_handoff.LOG((String)"newbie_handoff", (String)"Survey Mission Complete");
        newbie_handoff.LOG((String)"newbie_handoff", (String)"\tClearing Mission Data");
        this.clearHolocronDatapadEntry(self, "mission.survey");
        newbie_handoff.sendSystemMessage((obj_id)self, (string_id)SID_NEWBIE_MISSION_SURVEY_COMPLETE);
        newbie_handoff.LOG((String)"newbie_handoff", (String)"Opening Holocron to Artisan Survey Complete Section");
        this.openHolocronToNewbiePage(self, "WelcomeToSWG.WhereDoIBegin.Professions.PlayingArtisan.Resources.Survey.Complete");
        return 1;
    }

    public int missionGigComplete(obj_id self, dictionary params) throws InterruptedException {
        newbie_handoff.LOG((String)"newbie_handoff", (String)"Gig Mission Complete");
        newbie_handoff.LOG((String)"newbie_handoff", (String)"\tClearing Mission Data");
        this.clearHolocronDatapadEntry(self, "mission.gig");
        newbie_handoff.sendSystemMessage((obj_id)self, (string_id)SID_NEWBIE_MISSION_GIG_COMPLETE);
        newbie_handoff.messageTo((obj_id)self, (String)"openHolocronAfterGigMission", null, (float)5.0f, (boolean)false);
        return 1;
    }

    public int missionHealComplete(obj_id self, dictionary params) throws InterruptedException {
        newbie_handoff.LOG((String)"newbie_handoff", (String)"Heal Mission Complete");
        newbie_handoff.LOG((String)"newbie_handoff", (String)"\tClearing Mission Data");
        this.clearHolocronDatapadEntry(self, "mission.heal");
        newbie_handoff.sendSystemMessage((obj_id)self, (string_id)SID_NEWBIE_MISSION_HEAL_COMPLETE);
        newbie_handoff.messageTo((obj_id)self, (String)"openHolocronAfterHealMission", null, (float)5.0f, (boolean)false);
        return 1;
    }

    public int missionDestroyComplete(obj_id self, dictionary params) throws InterruptedException {
        newbie_handoff.LOG((String)"newbie_handoff", (String)"Destroy Mission Complete");
        newbie_handoff.LOG((String)"newbie_handoff", (String)"\tClearing Location Targets");
        this.clearHolocronDatapadEntry(self, "mission.destroy");
        String profession = params.getString((Object)"type");
        newbie_handoff.LOG((String)"newbie_handoff", (String)("\tRetreiving destroy mission type: " + profession));
        if (profession.equals("brawler")) {
            newbie_handoff.LOG((String)"newbie_handoff", (String)"\tSending messeage to open Holocron");
            newbie_handoff.sendSystemMessage((obj_id)self, (string_id)SID_NEWBIE_MISSION_DESTROY_COMPLETE);
            newbie_handoff.messageTo((obj_id)self, (String)"openHolocronAfterBrawlerMission", null, (float)5.0f, (boolean)false);
        } else {
            newbie_handoff.LOG((String)"newbie_handoff", (String)"\tSending messeage to open Holocron");
            newbie_handoff.sendSystemMessage((obj_id)self, (string_id)SID_NEWBIE_MISSION_DESTROY_COMPLETE);
            newbie_handoff.messageTo((obj_id)self, (String)"openHolocronAfterMarksmanMission", null, (float)5.0f, (boolean)false);
        }
        return 1;
    }

    public int missionHarvestTargetDead(obj_id self, dictionary params) throws InterruptedException {
        newbie_handoff.LOG((String)"newbie_handoff_harvest", (String)"Creature Killed: Telling player to harvest.");
        newbie_handoff.sendSystemMessage((obj_id)self, (string_id)SID_NEWBIE_MISSION_HARVEST_KILL);
        return 1;
    }

    public int missionHarvestComplete(obj_id self, dictionary params) throws InterruptedException {
        newbie_handoff.LOG((String)"newbie_handoff", (String)"Harvest Mission Complete");
        newbie_handoff.LOG((String)"newbie_handoff", (String)"\tClearing Location Targets");
        this.clearHolocronDatapadEntry(self, "mission.harvest");
        newbie_handoff.LOG((String)"newbie_handoff", (String)"\tSending messeage to open Holocron");
        newbie_handoff.sendSystemMessage((obj_id)self, (string_id)SID_NEWBIE_MISSION_HARVEST_COMPLETE);
        newbie_handoff.messageTo((obj_id)self, (String)"openHolocronAfterScoutMission", null, (float)5.0f, (boolean)false);
        return 1;
    }

    public int openHolocronAfterGigMission(obj_id self, dictionary params) throws InterruptedException {
        newbie_handoff.LOG((String)"newbie_handoff", (String)"Opening Holocron to Entertainer Gig Complete Section");
        this.openHolocronToNewbiePage(self, "WelcomeToSWG.WhereDoIBegin.Professions.PlayingEntertainer.Instruments.Gig.Complete");
        return 1;
    }

    public int openHolocronAfterHealMission(obj_id self, dictionary params) throws InterruptedException {
        newbie_handoff.LOG((String)"newbie_handoff", (String)"Opening Holocron to Medic Heal Complete Section");
        this.openHolocronToNewbiePage(self, "WelcomeToSWG.WhereDoIBegin.Professions.PlayingMedic");
        return 1;
    }

    public int openHolocronAfterBrawlerMission(obj_id self, dictionary params) throws InterruptedException {
        newbie_handoff.LOG((String)"newbie_handoff", (String)"Opening Holocron to Brawler Destroy Complete Section");
        if (ai_lib.isInCombat((obj_id)self)) {
            newbie_handoff.LOG((String)"newbie_handoff", (String)"\tStill in combat, wait 15 seconds");
            newbie_handoff.messageTo((obj_id)self, (String)"openHolocronAfterBrawlerMission", null, (float)15.0f, (boolean)false);
        } else {
            this.openHolocronToNewbiePage(self, "WelcomeToSWG.WhereDoIBegin.Professions.PlayingBrawler.MeleeTactics.Destroy.Complete");
        }
        return 1;
    }

    public int openHolocronAfterMarksmanMission(obj_id self, dictionary params) throws InterruptedException {
        newbie_handoff.LOG((String)"newbie_handoff", (String)"Opening Holocron to Marksman Destroy Complete Section");
        if (ai_lib.isInCombat((obj_id)self)) {
            newbie_handoff.LOG((String)"newbie_handoff", (String)"\tStill in combat, wait 15 seconds");
            newbie_handoff.messageTo((obj_id)self, (String)"openHolocronAfterMarksmanMission", null, (float)15.0f, (boolean)false);
        } else {
            this.openHolocronToNewbiePage(self, "WelcomeToSWG.WhereDoIBegin.Professions.PlayingMarksman.RangedTactics.Destroy.Complete");
        }
        return 1;
    }

    public int openHolocronAfterScoutMission(obj_id self, dictionary params) throws InterruptedException {
        newbie_handoff.LOG((String)"newbie_handoff", (String)"Opening Holocron to Scout Harvest Complete Section");
        if (ai_lib.isInCombat((obj_id)self)) {
            newbie_handoff.LOG((String)"newbie_handoff", (String)"\tStill in combat, wait 15 seconds");
            newbie_handoff.messageTo((obj_id)self, (String)"openHolocronAfterScoutMission", null, (float)15.0f, (boolean)false);
        } else {
            this.openHolocronToNewbiePage(self, "WelcomeToSWG.WhereDoIBegin.Professions.PlayingScout.Harvesting.Harvest.Complete");
        }
        return 1;
    }

    public int OnWaypointDestroyed(obj_id self, obj_id waypoint) throws InterruptedException {
        newbie_handoff.LOG((String)"newbie_handoff", (String)("\tTrying to remove data for Waypoint: " + waypoint));
        if (newbie_handoff.hasObjVar((obj_id)self, (String)("newbie_handoff.waypoint." + waypoint))) {
            dictionary params = new dictionary();
            params.put((Object)"waypoint", (Object)waypoint);
            params.put((Object)"name", newbie_handoff.getStringObjVar((obj_id)self, (String)("newbie_handoff.waypoint." + waypoint)));
            params.put((Object)"index", -1);
            newbie_handoff.LOG((String)"newbie_handoff", (String)"\tSending message to remove data");
            newbie_handoff.messageTo((obj_id)self, (String)"removeDatapadEntryData", (dictionary)params, (float)0.0f, (boolean)false);
        } else {
            Vector waypointIds = newbie_handoff.getResizeableObjIdArrayObjVar((obj_id)self, (String)"newbie_handoff.waypoint.idList");
            Vector waypointNames = newbie_handoff.getResizeableStringArrayObjVar((obj_id)self, (String)"newbie_handoff.waypoint.nameList");
            int idx = this.findWaypointIndex(waypointIds, waypoint);
            if (idx != -1) {
                dictionary params = new dictionary();
                params.put((Object)"waypoint", (Object)waypoint);
                params.put((Object)"name", (String)waypointNames.get(idx));
                params.put((Object)"index", idx);
                newbie_handoff.LOG((String)"newbie_handoff", (String)"\tSending message to remove data");
                newbie_handoff.messageTo((obj_id)self, (String)"removeDatapadEntryData", (dictionary)params, (float)0.0f, (boolean)false);
            }
        }
        return 1;
    }

    public int removeDatapadEntryData(obj_id self, dictionary params) throws InterruptedException {
        String name = params.getString((Object)"name");
        obj_id waypoint = params.getObjId((Object)"waypoint");
        int index = params.getInt((Object)"index");
        newbie_handoff.LOG((String)"newbie_handoff", (String)("Destroying Datapad Entry " + name));
        if (name.equals("mission.destroy") || name.equals("mission.harvest") || name.equals("mission.heal")) {
            newbie_handoff.LOG((String)"newbie_handoff", (String)"\tCleaning up spawned stuff");
            obj_id spawnedNpc = newbie_handoff.getObjIdObjVar((obj_id)self, (String)(name + ".npc_id"));
            if (newbie_handoff.isIdValid((obj_id)spawnedNpc)) {
                newbie_handoff.destroyObject((obj_id)spawnedNpc);
            }
        }
        if (name.equals("mission.destroy") || name.equals("mission.harvest") || name.equals("mission.heal")) {
            newbie_handoff.LOG((String)"newbie_handoff", (String)"\tRemoving mission spawner trigger");
            newbie_handoff.removeLocationTarget((String)(name + ".spawnGuy"));
        }
        newbie_handoff.LOG((String)"newbie_handoff", (String)"\tRemoving obj_var data");
        if (newbie_handoff.hasObjVar((obj_id)self, (String)("newbie_handoff." + name))) {
            newbie_handoff.LOG((String)"newbie_handoff", (String)("\tRemoving " + name + " data."));
            newbie_handoff.removeObjVar((obj_id)self, (String)("newbie_handoff." + name));
        }
        if (waypoint != null) {
            newbie_handoff.LOG((String)"newbie_handoff", (String)"\tRemoving waypoint data.");
            if (index != -1) {
                Vector waypointIds = newbie_handoff.getResizeableObjIdArrayObjVar((obj_id)self, (String)"newbie_handoff.waypoint.idList");
                Vector waypointNames = newbie_handoff.getResizeableStringArrayObjVar((obj_id)self, (String)"newbie_handoff.waypoint.nameList");
                waypointIds = utils.removeElementAt((Vector)waypointIds, (int)index);
                waypointNames = utils.removeElementAt((Vector)waypointNames, (int)index);
                if (waypointIds.size() > 0 && waypointNames.size() > 0) {
                    newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.waypoint.idList", (Vector)waypointIds);
                    newbie_handoff.setObjVar((obj_id)self, (String)"newbie_handoff.waypoint.nameList", (Vector)waypointNames);
                } else {
                    newbie_handoff.removeObjVar((obj_id)self, (String)"newbie_handoff.waypoint.idList");
                    newbie_handoff.removeObjVar((obj_id)self, (String)"newbie_handoff.waypoint.nameList");
                }
            } else if (newbie_handoff.hasObjVar((obj_id)self, (String)("newbie_handoff.waypoint." + waypoint))) {
                newbie_handoff.removeObjVar((obj_id)self, (String)("newbie_handoff.waypoint." + waypoint));
            }
        }
        return 1;
    }

    private obj_id addHolocronDatapadWaypoint(obj_id player, String name, String display, location loc, float radius) throws InterruptedException {
        newbie_handoff.LOG((String)"newbie_handoff", (String)("Creating Waypoint: '" + display + "' in Datapad..."));
        obj_id waypoint = newbie_handoff.createWaypointInDatapad((obj_id)player, (location)loc);
        newbie_handoff.setWaypointName((obj_id)waypoint, (String)display);
        newbie_handoff.addLocationTarget((String)name, (location)loc, (float)radius);
        newbie_handoff.setWaypointActive((obj_id)waypoint, (boolean)true);
        newbie_handoff.setObjVar((obj_id)player, (String)("newbie_handoff." + name + ".waypoint"), (obj_id)waypoint);
        Vector waypointIds = new Vector();
        waypointIds.setSize(0);
        Vector waypointNames = new Vector();
        waypointNames.setSize(0);
        if (newbie_handoff.hasObjVar((obj_id)player, (String)"newbie_handoff.waypoint.idList")) {
            waypointIds = newbie_handoff.getResizeableObjIdArrayObjVar((obj_id)player, (String)"newbie_handoff.waypoint.idList");
            waypointNames = newbie_handoff.getResizeableStringArrayObjVar((obj_id)player, (String)"newbie_handoff.waypoint.nameList");
        }
        waypointIds = utils.addElement(waypointIds, (Object)waypoint);
        waypointNames = utils.addElement(waypointNames, (Object)name);
        if (waypointIds.size() > 0 && waypointNames.size() > 0) {
            newbie_handoff.setObjVar((obj_id)player, (String)"newbie_handoff.waypoint.idList", (Vector)waypointIds);
            newbie_handoff.setObjVar((obj_id)player, (String)"newbie_handoff.waypoint.nameList", (Vector)waypointNames);
        }
        return waypoint;
    }

    private obj_id addHolocronDatapadEntry(obj_id player, String name, String display) throws InterruptedException {
        newbie_handoff.LOG((String)"newbie_handoff", (String)("Creating Entry: '" + display + "' in Datapad..."));
        obj_id datapad = newbie_handoff.getObjectInSlot((obj_id)player, (String)"datapad");
        obj_id entry = newbie_handoff.createObject((String)"object/intangible/holocron/newbie_mission.iff", (obj_id)datapad, (String)"");
        newbie_handoff.setName((obj_id)entry, (String)display);
        newbie_handoff.LOG((String)"newbie_handoff", (String)("\tCreated entry: " + entry + " in datapad object: " + datapad));
        newbie_handoff.LOG((String)"newbie_handoff", (String)("\tDatapad contains object: " + utils.isNestedWithin((obj_id)entry, (obj_id)datapad)));
        newbie_handoff.setObjVar((obj_id)player, (String)("newbie_handoff." + name + ".dataEntry"), (obj_id)entry);
        newbie_handoff.attachScript((obj_id)entry, (String)"holocron.datapad_entry");
        newbie_handoff.setObjVar((obj_id)entry, (String)"newbie_handoff.name", (String)name);
        newbie_handoff.setObjVar((obj_id)entry, (String)"newbie_handoff.player", (obj_id)player);
        return entry;
    }

    private void clearHolocronDatapadEntry(obj_id player, String name) throws InterruptedException {
        if (newbie_handoff.hasObjVar((obj_id)player, (String)("newbie_handoff." + name + ".dataEntry"))) {
            obj_id dataEntry = newbie_handoff.getObjIdObjVar((obj_id)player, (String)("newbie_handoff." + name + ".dataEntry"));
            if (newbie_handoff.isIdValid((obj_id)dataEntry)) {
                newbie_handoff.destroyObject((obj_id)dataEntry);
            }
            return;
        }
        if (newbie_handoff.hasObjVar((obj_id)player, (String)("newbie_handoff." + name + ".waypoint"))) {
            obj_id waypoint = newbie_handoff.getObjIdObjVar((obj_id)player, (String)("newbie_handoff." + name + ".waypoint"));
            if (newbie_handoff.isIdValid((obj_id)waypoint)) {
                newbie_handoff.setWaypointActive((obj_id)waypoint, (boolean)false);
                newbie_handoff.destroyWaypointInDatapad((obj_id)waypoint, (obj_id)player);
            }
            newbie_handoff.removeLocationTarget((String)name);
        }
    }

    private void addHolocronWaypointPath(obj_id player, obj_id waypoint) throws InterruptedException {
        if (utils.hasScriptVar((obj_id)player, (String)"hasClientPath")) {
            newbie_handoff.destroyClientPath((obj_id)player);
            obj_id oldWaypoint = utils.getObjIdScriptVar((obj_id)player, (String)"hasClientPath");
            if (newbie_handoff.isIdValid((obj_id)oldWaypoint)) {
                newbie_handoff.destroyWaypointInDatapad((obj_id)oldWaypoint, (obj_id)player);
            }
            utils.removeScriptVar((obj_id)player, (String)"hasClientPath");
        }
        location here = newbie_handoff.getLocation((obj_id)player);
        location there = newbie_handoff.getWaypointLocation((obj_id)waypoint);
        region[] hereCity = newbie_handoff.getRegionsWithGeographicalAtPoint((location)here, (int)20);
        region[] thereCity = newbie_handoff.getRegionsWithGeographicalAtPoint((location)there, (int)20);
        if (hereCity != null && hereCity.length > 0 && thereCity != null && thereCity.length > 0) {
            boolean areInSameCity = false;
            for (region aHereCity : hereCity) {
                for (region aThereCity : thereCity) {
                    if (!aHereCity.getName().equals(aThereCity.getName())) continue;
                    areInSameCity = true;
                    break;
                }
                if (areInSameCity) break;
            }
            if (areInSameCity) {
                there.y = newbie_handoff.getHeightAtLocation((float)there.x, (float)there.z);
                location start = here;
                if (newbie_handoff.isIdValid((obj_id)start.cell)) {
                    start = newbie_handoff.getBuildingEjectLocation((obj_id)newbie_handoff.getTopMostContainer((obj_id)player));
                }
                if (!newbie_handoff.createClientPath((obj_id)player, (location)start, (location)there)) {
                    newbie_handoff.sendSystemMessageTestingOnly((obj_id)player, (String)"The system was unable to create a client path for you.");
                } else {
                    if (here.cell != start.cell) {
                        newbie_handoff.sendSystemMessageTestingOnly((obj_id)player, (String)"Your find path has been started near the entrance of the structure you are currenly in.");
                    }
                    utils.setScriptVar((obj_id)player, (String)"hasClientPath", (obj_id)waypoint);
                }
            }
        }
    }

    private int findWaypointIndex(Vector idList, obj_id waypoint) throws InterruptedException {
        if (idList != null && idList.size() > 0) {
            for (int i = 0; i < idList.size(); ++i) {
                if (idList.get(i) != waypoint) continue;
                return i;
            }
        }
        return -1;
    }

    private void openHolocronToNewbiePage(obj_id player, String page) throws InterruptedException {
        if (!newbie_handoff.hasObjVar((obj_id)player, (String)"newbie_handoff.disabled")) {
            newbie_handoff.openHolocronToPage((obj_id)player, (String)page);
        }
    }

    private location findClosestStructure(obj_id player, String[] priSearchList, String[] secSearchList) throws InterruptedException {
        float dist;
        location loc;
        map_location destination;
        String sub;
        String cat;
        StringTokenizer st;
        float priDist = Float.POSITIVE_INFINITY;
        location priLoc = null;
        float secDist = Float.POSITIVE_INFINITY;
        location secLoc = null;
        for (String aPriSearchList : priSearchList) {
            st = new StringTokenizer(aPriSearchList, "_");
            cat = st.nextToken();
            sub = "";
            if (st.hasMoreTokens()) {
                sub = st.nextToken();
            }
            if ((destination = planetary_map.findClosestLocation((obj_id)player, (String)cat, (String)sub)) == null) continue;
            loc = new location((float)destination.getX(), 0.0f, (float)destination.getY());
            dist = utils.getDistance2D((location)newbie_handoff.getLocation((obj_id)player), (location)loc);
            if (!(dist < priDist)) continue;
            priDist = dist;
            priLoc = loc;
        }
        if (priDist < 500.0f) {
            return priLoc;
        }
        for (String aSecSearchList : secSearchList) {
            st = new StringTokenizer(aSecSearchList, "_");
            cat = st.nextToken();
            sub = "";
            if (st.hasMoreTokens()) {
                sub = st.nextToken();
            }
            if ((destination = planetary_map.findClosestLocation((obj_id)player, (String)cat, (String)sub)) == null) continue;
            loc = new location((float)destination.getX(), 0.0f, (float)destination.getY());
            dist = utils.getDistance2D((location)newbie_handoff.getLocation((obj_id)player), (location)loc);
            if (!(dist < secDist)) continue;
            secDist = dist;
            secLoc = loc;
        }
        if (secDist < 500.0f) {
            return secLoc;
        }
        if (priDist < secDist) {
            return priLoc;
        }
        return secLoc;
    }

    private String getStartingProfession(obj_id player) throws InterruptedException {
        int professionId = utils.getPlayerProfession((obj_id)player);
        switch (professionId) {
            case 1: {
                return "commando";
            }
            case 2: {
                return "smuggler";
            }
            case 3: {
                return "medic";
            }
            case 4: {
                return "officer";
            }
            case 5: {
                return "spy";
            }
            case 6: {
                return "bounty_hunter";
            }
            case 7: {
                return "force_sensitive";
            }
            case 9: {
                return "entertainer";
            }
            case 8: {
                return "trader";
            }
        }
        return "";
    }

    private String getSimpleProfession(String profession) throws InterruptedException {
        StringTokenizer st = new StringTokenizer(profession, "_");
        String tmp = st.nextToken();
        if (st.hasMoreTokens()) {
            tmp = st.nextToken();
        }
        return tmp;
    }

    private String getTrainerType(String profession) throws InterruptedException {
        if (profession == null || profession.equals("")) {
            return "";
        }
        return "trainer_" + this.getSimpleProfession(profession);
    }

    private String getHolocronProfessionSuffix(String profession) throws InterruptedException {
        if (profession == null || profession.equals("")) {
            return "";
        }
        switch (profession = profession.toLowerCase()) {
            case "bounty": {
                return "BountyHunter";
            }
            case "force": {
                return "ForceSensitive";
            }
        }
        return newbie_handoff.toUpper((String)profession, (int)0);
    }

    private String determineDestroyTarget(obj_id player) throws InterruptedException {
        String planet = newbie_handoff.getCurrentSceneName();
        String city = locations.getCityName((location)newbie_handoff.getLocation((obj_id)player));
        newbie_handoff.LOG((String)"newbie_handoff", (String)("Seraching for destroy mission target at " + planet + ": " + city));
        return this.loadRandomDestroyTarget(planet, city);
    }

    private String loadRandomDestroyTarget(String planet, String city) throws InterruptedException {
        String[] targets = new String[10];
        String[] entries = newbie_handoff.dataTableGetStringColumn((String)TARGET_DATATABLE, (String)"strPlanet");
        if (entries == null || entries.length == 0) {
            newbie_handoff.LOG((String)"newbie_handoff", (String)("WARNING: No targets defined for " + planet + ": " + city + ", using default target"));
            return newbie_handoff.dataTableGetString((String)TARGET_DATATABLE, (int)0, (String)"strTarget");
        }
        newbie_handoff.LOG((String)"newbie_handoff", (String)("Reading datatables/newbie_handoff/destroy_targets.iff:" + entries.length + "entries read."));
        int targetCount = 0;
        for (int i = 0; i < entries.length; ++i) {
            if (entries[i] == null || entries[i].equals("")) {
                entries[i] = "nullPlanet";
            }
            if (!entries[i].equals(planet) || !newbie_handoff.dataTableGetString((String)TARGET_DATATABLE, (int)i, (String)"strCity").equals(city)) continue;
            targets[targetCount] = newbie_handoff.dataTableGetString((String)TARGET_DATATABLE, (int)i, (String)"strTarget");
            newbie_handoff.LOG((String)"newbie_handoff", (String)("Possible target found: " + targets[targetCount]));
            if (++targetCount < 10) continue;
            newbie_handoff.LOG((String)"newbie_handoff", (String)"WARNING: Datatable contains more targets than MAX_DESTROY_TARGETS(10)");
            break;
        }
        if (targetCount == 0) {
            newbie_handoff.LOG((String)"newbie_handoff", (String)("WARNING: No targets defined for " + planet + ": " + city + ", using default target"));
            return newbie_handoff.dataTableGetString((String)TARGET_DATATABLE, (int)0, (String)"strTarget");
        }
        return targets[newbie_handoff.rand((int)0, (int)(targetCount - 1))];
    }
}
