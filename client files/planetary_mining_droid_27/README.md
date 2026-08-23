# Interplanetary Mining Droid Loose Client Files 27

Install these files at their client-relative paths as loose files. Do not package this PMD revision as a TRE.

This complete revision includes the Mining Droid MK3 appearance and crafting preview, fixed 100% Quality, experimental Duration and Charges, the revised five-slot recipe, Survey License UI data, and the `returnPlanetaryMiningDroid` command. The command appears in the command browser's Other tab as **Return Planetary Mining Droid**, can be dragged to a toolbar, and uses the existing `droid_stay` R2-style icon.

Revision 18 changes the PMD crafting presentation to use Load-bearing Frame, Bore Mechanism, Cargo Hold, Survey Operations Computer, and Terrain Manipulator. It also adds the PMD item description.

Revision 19 corrects the shared schematic attribute identity so the normal crafting datapad can receive and display each experimentation line's resource weights.

Revision 20 corrects the shared draft schematic's bottom description to use the PMD description string.

Revision 21 restores stock droid item-wide experimentation weighting, removes ingredient-slot suffixes from the datapad weight display, and labels the three groups Experimental Charges, Experimental Duration, and Experimental Quality.

Revision 22 removes the duplicate schematic description binding. The datapad preview now obtains the PMD description exclusively from its crafted shared template, matching stock droid repair kits.

Revision 23 replaces revision 22's stale crafted tangible IFF with the verified complete IFF that binds `detailedDescription` to `planetary_mining_droid:description`. Loose files replace the complete client-relative asset; install the entire revision, not individual STF entries or IFF chunks.

Revision 24 adds a complete `string/en/static_item_d.stf` containing `static_item_d:item_planetary_mining_droid`. Static-item grants override a template description with this key, so its value matches the PMD crafted-item description.

Revision 25 adds one required Hand Sample Module to the PMD schematic. It updates the complete shared draft IFF while retaining revision 24's complete static-item description table.

Revision 26 relabels that required component slot as Resource Extraction Equipment while retaining the Hand Sample Module template requirement.

Revision 27 matches the deployed complexity, quality, and duration values and
extends the PMD description with: `It will inherit its owners expertise and
modifications when launched`.
