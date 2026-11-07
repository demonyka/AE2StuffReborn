# Changelog

## 1.0.0 - 2026-11-07

First release for Minecraft 1.16.5 with Forge 36 and Applied Energistics 2 8.4.7.

- Ported every block and item of AE2 Stuff 0.7.0 for 1.12.2: Pattern Encoder, Crystal Growth Chamber, Advanced Inscriber, Wireless Connector, Wireless Setup Kit and Network Visualisation Tool.
- Recipes, settings, translations and item ids are the same as in the original.
- JEI recipe transfer into the Pattern Encoder.
- Hwyla tooltips for machine power and wireless links.
- Rewritten for the AE2 8.x API: recipe based crafting patterns, inscriber recipes from the recipe system, new upgrade panels, wrench handling.
- Hardened for multiplayer: no item duplication or loss through the machine GUIs and the network tool toolbox, no chunk loading by wireless links and tools, wrench dismantling respects protection mods.
- Works with CraftTweaker, KubeJS, data packs and `/reload`.
