# FC Orbit - Editor for Fantastic Contraption
[Play the game](http://www.fantasticcontraption.com/original/) | [Discord server](https://discord.gg/NdwRfAj)
---

The in-game editor lacked features.

sk89q's FCML editor, the first external editor, offered a lot more functionality,
but still had limitations disguised as safety, and very annoyingly rounds all numbers during export, even angles.

ID36 then created an editor with much more flexibility, and doing more stuff clientside,
but it lacked nice features like copy paste or undo redo.

FC Orbit is now maintained by [komiamiko](https://github.com/komiamiko), and aims to solve all these problems.
Text editor and graphical editor side by side with real time updates and a large toolkit.

# FAQ
## Why can't I upload levels from within the editor?
Not in this version, not in the near future, but it may come in a later update.

## What is the language used by the text editor?
Orbit uses a relaxed version of FCML (Fantastic Contraption Markup Language). FCML normally looks like this:
```
Type#index (center_x, center_y), (width, height), rotation_degrees, [joint_indices...]
BuildArea (-200, -100), (210, 210), 0
GoalArea (200, -50), (110, 110), 0
StaticRect (0, 20), (1000, 40), 0
StaticCircle (0, 20), (60, 60), 0
GoalRect#0 (0, -30), (30, 90), 90
GoalCircle#1 (0, -30), (40, 40), 0, [0]
```
Orbit's parser is very generous. This produces the same result:
```
Type#index center_x center_y width height rotation_degrees joint_indices...
BA -200 -100 210
GA 200 -50 110
SR 0 20 1000 40
SC 0 20 60
GR#0 0 -30 30 90 90
GC#1 0 -30 40 40 0 0
```
Leaving out the rotation makes it assumed to be 0.
Leaving out the height makes it assumed to be equal to the width.
For circles this is very convenient because rotation does nothing and width and height both need to be equal to the diameter anyway.

Type names have aliases so you don't need to worry too much about remembering the exact name, except for CW meaning clockwise and CCW meaning counterclockwise.

Commas, brackets, and other stuff that was previously necessary is now completely ignored by the parser.
It can be considered syntactic salt.

Design pieces have indices and optionally joints.
Indices count from 0 and are uniquely assigned.
Joints say which other pieces a piece is connected to.
The list is one way - you reference the lower indexed piece from the higher indexed piece, the game infers the opposite direction.
In this example, piece \#1, the goal circle, is connected to piece \#0, the goal rectangle, and it is implied that it is the center that connects.
For rods, if both sides are plausible, the left side of the higher indexed piece breaks the tie. Other nuances of jointing aren't really researched since it's really up to the game.

# Controls and hotkeys reference
## General
There is a divider bar between the graphical editor and the text editor.
Move it to your liking.

The editor will either be in "graphical mode" or "text mode" depending on where your mouse is.
When your mouse is over the text area, the editor is in text mode, and typing creates text.
When your mouse is over the graphical area, the editor is in graphical mode, and responds to various hotkeys.
It is not possible to type while in graphical mode.

Caveats:
1. On Mac, Alt will be Option instead, and Control is still Control (not Command).

## Viewport
**Scroll** to zoom.

**Shift + scroll** to move vertically.

**Alt + Shift + scroll** to move horizontally.

It is possible to pan to about 3 times the game's view bounds.

**Z** to toggle wireframe.
Hyper-thin wireframe using **Z** for high precision editing is planned for a future update.

**Shift + Z** to toggle an overlay for the game's view bounds. Anything outside this boundary will never be visible in-game.
Toggle for legacy view bounds using **Shift + Z** is planned for a future update.

## Selection
**Click** to select one object, replacing the current selection.

**Shift + Click** to select or deselect one object, otherwise leaving the current selection unchanged.

**Click + Drag** to add all objects in the box to the current selection.

**Shift + Click + Drag** to remove all objects in the box from the current selection.

Text editor selection is synced with the graphical editor, if possible.

## Undo/Redo
**Control + Z** to undo one step.

**Control + Shift + Z** or **Control + Y** to redo one step.

These also work in the text editor.

## Tools

**X** to delete the selected objects.

**G** to translate the selected objects. Left click to confirm, right click to cancel.
* **X** to translate only on the x-axis. Toggles between no axis lock, global x-axis lock, and local x-axis lock.
* **Y** to translate only on the y-axis. Toggles between no axis lock, global y-axis lock, and local y-axis lock.
* Grid snapping by holding **Control** is planned for a future update.
* Fine movement by holding **Shift** is planned for a future update.

# For developers

This project uses maven. (ever since the modernize update)

## Build

```sh
mvn package
```

## Run

```
java -jar target/fcorbit*.jar
```

Or just open the jar directly. (double-click open)

## Versioning

We have auto version bump set up with GitHub Actions. Every PR will, by default, bump the patch version.

You can override this with the `version:major` or `version:minor` labels on your PR.