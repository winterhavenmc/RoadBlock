# RoadBlock

This plugin allows you to protect roads you create on your server from being broken by players. Each individual block that your road is made of is marked as protected, so your roads can twist and turn and meander through the landscape, and still be protected from breakage. Selecting blocks from protection (or de-protection) is simple and efficient. Simply give yourself the custom tool via command, then right or left click to protect or unprotect blocks of the configured materials.

# Features

*   Use a custom tool to select RoadBlocks for protection.
*   Customizable tool attributes. (material, name, lore)
*   Quickly add or remove protection to blocks.
*   Blocks are highlighted when protecting or unprotecting. Highlight is removed after a short time, or when switching away from the custom tool in your inventory.
*   Customizable blocks to be used for highlighting protected/unprotected blocks.
*   Customizable language support.
*   Customizable sound effects.
*   Snow plow option prevents snow from forming on RoadBlocks. (v1.0.3)
*   Experimental: reduced mob targeting range when players are traveling on protected roads. Reduced target distance is configurable in the config.yml file.

# Usage

*   Give yourself or admins the appropriate permission, and then use the command to give yourself the custom RoadBlock tool.
*   Make sure the material your road is constructed with is listed as a valid RoadBlock material in the config.yml file.
*   Right click on a block of your road. Any adjacent blocks that are valid RoadBlock materials will also be selected, up to a configurable distance. (Default: 100 blocks)
*   Left clicking will remove the protection from all adjacent blocks in exactly the same way.

Note that block selection will only spread on the same vertical level. This is intentional, to limit the consequences of accidentally protecting any buildings that are made from valid RoadBlock materials and are adjacent to the road being protected.  

# Commands

`/roadblock reload` | reloads the configuration without needing to restart the server.
`/roadblock show <distance>` | highlights protected RoadBlocks within specified distance. Uses `show-distance` setting in config.yml if argument is omitted.
`/roadblock status` | displays configuration settings.
`/roadblock tool` | places the custom roadblock tool in your inventory (if permission allows it).
`/rb [subcommand]` | command alias

# Permissions

<table>
<tbody>

<tr>
<th>Permission</th>
<th>Description</th>
<th>Default</th>
</tr>

<tr>
<td>`roadblock.admin`</td>
<td>Allows a player access to all RoadBlock commands and allows use of RoadBlock tool to protect/unprotect blocks.</td>
<td>op</td>
</tr>

<tr>
<td>`roadblock.break`</td>
<td>Allows breaking RoadBlock protected blocks. (Not included in roadblock.admin permission set, must be set explicitly.)</td>
<td>op</td>
</tr>

<tr>
<td>`roadblock.reload`</td>
<td>Allows reloading the config file.</td>
<td>op</td>
</tr>

<tr>
<td>`roadblock.status`</td>
<td>Allows display of config settings.</td>
<td>op</td>
</tr>

<tr>
<td>`roadblock.show`</td>
<td>Allows use of show command to highlight nearby RoadBlocks.</td>
<td>op</td>
</tr>

<tr>
<td>`roadblock.unset`</td>
<td>Allows removing RoadBlock protection on blocks.</td>
<td>op</td>
</tr>

<tr>
<td>`roadblock.tool`</td>
<td>Allows creating a RoadBlock tool in inventory.</td>
<td>op</td>
</tr>
</tbody>
</table>
