# DijWeb-Maker
_JavaFX application assisting to visually construct a web-node system, additionally providing the ability to simulate inner traversal via dijkstra algorithm._

### Features ###
- Fully dynamic sizing, responsive to window size and defined background-image
- Easily paint an accurate web-node system with intuitive controls
- Visually simulate traveral between dynamically defined points 
- Automatically saves and re-loads user-defined configuration

### Configurability ###
* **Rows** Represents number of vertical coordinates
* **Columns** Represents number of horizontal coordinates
* **X-Offset** Derives base number of columns (X) by this number
* **Y-Offset** Derives base number of rows (Y) by this number

### Controls ###
* **(Left Click)**
  * Left clicking near a path will enable it to be moved, left clicking again with either attach the path to the nearest node or reset to it's prior location. [The path will highlight in color when it's available to be re-positioned]
  * A left click near a node will allow the user to draw new path using the node as the source, Left clicking again, near another node, will attach a new line - otherwise the created path it will remove itself.
  * Additionally, If no paths are near the mouse when left clicked, the web may be panned in any direction provided the defined background-image is large enough.
  
* **(Right Click)**
  * Right clicking near an existing node or path will delete it's existance, if none are near a new node will be created - should another node be near the newly created node, a path will be attached to path.
  

### Use Cases ###
_Use DijWeb-Maker to create a web for your favorite game - or expand the horizons by developing an aerial pathing-system for your drone!_

**Examples**
* RuneScape
![](osrs.gif)

* Indianapolis
![](indy.gif)
