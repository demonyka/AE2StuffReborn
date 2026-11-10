# Changelog

## 1.0.2 - 2026-11-10

The project is now called AE2 Stuff Reborn. No gameplay changes.

- The mod is listed as AE2 Stuff in the mod list, and the jar is named AE2Stuff-1.16.5-<version>.jar.
- Source code moved to https://github.com/demonyka/AE2StuffReborn.

## 1.0.1 - 2026-11-10

Bug fixes for multiplayer servers and translation updates.

- Wireless Connector no longer draws power for a link whose other end is not loaded.
- A Wireless Connector whose partner was removed now clears its link by itself. Breaking or re-linking a connector no longer loads the chunk of the other end.
- Machines always drop with their contents when blown up.
- An empty machine stacks with newly crafted ones again after being picked up.
- The Pattern Encoder screen no longer closes while the ME network is rebooting.
- Shift-clicking an encoded pattern with a full inventory no longer puts it into an armor or off-hand slot.
- Idle machines no longer update their block twice every 5 seconds: less chunk saving and no more observer pulses.
- Crystal Growth Chamber causes fewer block updates while growing.
- Network Visualisation Tool:
  - The overlay is cleared when the bound network is not available, is in another dimension, or after leaving the server.
  - Very large networks no longer risk a disconnect.
  - Channel counts above 127 are shown correctly.
  - Channel labels are cheaper to draw and are shown within 64 blocks.
  - Right-clicking a non-ME block with the tool no longer swings or blocks the item in the off-hand.
- Completed the German, Italian, Korean, Brazilian Portuguese and Chinese translations.

## 1.0.0 - 2026-11-07

First release for Minecraft 1.16.5 with Forge 36 and Applied Energistics 2 8.4.7.

- Ported every block and item of AE2 Stuff 0.7.0 for 1.12.2: Pattern Encoder, Crystal Growth Chamber, Advanced Inscriber, Wireless Connector, Wireless Setup Kit and Network Visualisation Tool.
- Recipes, settings, translations and item ids are the same as in the original.
- JEI recipe transfer into the Pattern Encoder.
- Hwyla tooltips for machine power and wireless links.
- Rewritten for the AE2 8.x API: recipe based crafting patterns, inscriber recipes from the recipe system, new upgrade panels, wrench handling.
- Hardened for multiplayer: no item duplication or loss through the machine GUIs and the network tool toolbox, no chunk loading by wireless links and tools, wrench dismantling respects protection mods.
- Works with CraftTweaker, KubeJS, data packs and `/reload`.
