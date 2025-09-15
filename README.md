[![Codacy Badge](https://app.codacy.com/project/badge/Grade/9cc37fae1da64772b1f2bafef3b5663b)](https://app.codacy.com/gh/winterhavenmc/RoadBlock/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Codacy Badge](https://app.codacy.com/project/badge/Coverage/9cc37fae1da64772b1f2bafef3b5663b)](https://app.codacy.com/gh/winterhavenmc/RoadBlock/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_coverage)
[![Spigot Version](https://badgen.net/static/spigot-api/1.21.8?color=yellow)](https://spigotmc.org)
&nbsp;[![License](https://badgen.net/static/license/GPLv3)](https://www.gnu.org/licenses/gpl-3.0)

### RoadBlock
This plugin allows you to protect roads you create on your server from being broken by players. Each individual block that your road is made of is marked as protected, so your roads can twist and turn and meander through the landscape, and still be protected from breakage. Selecting blocks from protection (or de-protection) is simple and efficient. Simply give yourself the custom tool via command, then right or left click to protect or unprotect blocks of the configured materials.

### Features
*   Use a custom tool to select RoadBlocks for protection.
*   Customizable tool attributes. (material, name, lore)
*   Quickly add or remove protection to blocks.
*   Blocks are highlighted when protecting or unprotecting. Highlight is removed after a short time, or when switching away from the custom tool in your inventory.
*   Customizable blocks to be used for highlighting protected/unprotected blocks.
*   Customizable language support.
*   Customizable sound effects.
*   Snow plow option prevents snow from forming on RoadBlocks. (v1.0.3)
*   Experimental: reduced mob targeting range when players are traveling on protected roads. Reduced target distance is configurable in the config.yml file.

### Usage
*   Give yourself or admins the appropriate permission, and then use the command to give yourself the custom RoadBlock tool.
*   Make sure the material your road is constructed with is listed as a valid RoadBlock material in the config.yml file.
*   Right click on a block of your road. Any adjacent blocks that are valid RoadBlock materials will also be selected, up to a configurable distance. (Default: 100 blocks)
*   Left clicking will remove the protection from all adjacent blocks in exactly the same way.

Note that block selection will only spread on the same vertical level. This is intentional, to limit the consequences of accidentally protecting any buildings that are made from valid RoadBlock materials and are adjacent to the road being protected.  

### Commands
| Command                      | Description                                                                                                                   |
|------------------------------|-------------------------------------------------------------------------------------------------------------------------------|
| `/roadblock reload`          | reloads the configuration without needing to restart the server.                                                              |
| `/roadblock show <distance>` | highlights protected RoadBlocks within specified distance. Uses `show-distance` setting in config.yml if argument is omitted. |
| `/roadblock status`          | displays configuration settings.                                                                                              |
| `/roadblock tool`            | places the custom roadblock tool in your inventory (if permission allows it).                                                 |
| `/rb [subcommand]`           | command alias                                                                                                                 |

### Permissions
| Permission         | Description                                                                                                           | Default |
|--------------------|-----------------------------------------------------------------------------------------------------------------------|---------|
| `roadblock.admin`  | Allows a player access to all RoadBlock commands and allows use of RoadBlock tool to protect/unprotect blocks.        | op      |
| `roadblock.break`  | Allows breaking RoadBlock protected blocks. (Not included in roadblock.admin permission set, must be set explicitly.) | op      |
| `roadblock.reload` | Allows reloading the config file.                                                                                     | op      |
| `roadblock.status` | Allows display of config settings.                                                                                    | op      |
| `roadblock.show`   | Allows use of show command to highlight nearby RoadBlocks.                                                            | op      |
| `roadblock.unset`  | Allows removing RoadBlock protection on blocks.                                                                       | op      |
| `roadblock.tool`   | Allows creating a RoadBlock tool in inventory.                                                                        | op      |
