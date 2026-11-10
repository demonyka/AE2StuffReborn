A port of bdew's **AE2 Stuff** to Minecraft 1.16.5 and Applied Energistics 2 8.x. Same machines, same recipes, same behaviour as the 1.12 version.

**Requires [Applied Energistics 2](https://www.curseforge.com/minecraft/mc-mods/applied-energistics-2) 8.4.7** (on CurseForge, Modrinth only has AE2 for 1.18 and newer). JEI and Hwyla are optional, Jade is expected to work but untested.

## Machines

**Pattern Encoder**
Encodes crafting patterns from a 3x3 grid. Opens while it is connected to a powered ME network. Use the JEI "+" button to fill the grid: it picks the ingredient variants that your ME storage has. Drop an encoded pattern on the blank pattern slot to turn it back into a blank one.

**Crystal Growth Chamber**
Grows AE2 crystal seeds and makes fluix crystals from charged certus quartz, redstone and nether quartz. Up to 3 speed cards.

**Advanced Inscriber**
Runs inscriber recipes with up to 5 speed cards. Lock the press slots so pipes and buses never pull the presses out.

**Wireless Connector**
Links two points of an ME network without cables. Carries 32 channels and draws 10 + distance² AE/t.

## Tools

**Wireless Setup Kit**: click two Wireless Connectors to link them, sneak-use in the air to clear the kit. Network security is respected.

**Network Visualisation Tool**: click any ME block to bind the tool, then see nodes, links and channel counts of the whole network through walls. Sneak and scroll to switch between showing everything, nodes, channels, both, or P2P links.

## Details

- Machines run on ME network power, keep their contents, upgrades and stored power when broken, and show the network tool toolbox.
- Work with hoppers, AE2 interfaces and buses, and pipe mods. Tested with Mekanism and Pretty Pipes.
- Autocrafting ready: patterns from the Encoder work in Molecular Assemblers, and the Inscriber and Growth Chamber accept processing patterns through interfaces.
- Settings in `config/ae2stuff-common.toml`, recipes can be changed with data packs.

## Credits

AE2 Stuff was created by **bdew**. Ported to 1.16.5 by **demonyga**. MIT licensed, source on [GitHub](https://github.com/demonyka/AE2StuffReborn).
