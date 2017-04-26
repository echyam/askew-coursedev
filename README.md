![Askew](https://github.com/TrevorEdwards/mantis-ink/blob/develop/art/askewLoading.png)

---

# Askew: A vine-swinging puzzle-platformer!

---

[![forthebadge](http://forthebadge.com/images/badges/made-with-crayons.svg)](http://forthebadge.com)
[![forthebadge](http://forthebadge.com/images/badges/contains-technical-debt.svg)](http://forthebadge.com)
[![forthebadge](http://forthebadge.com/images/badges/does-not-contain-treenuts.svg)](http://forthebadge.com)

---

# **Beta Prototype Manual**

## Objective
Playing as Flow, you must swing from vine to vine to reach the owl, the goal! Reaching the goal requires colliding with the owl, which will immediataely bring you to the next stage!

## PC Controls

We now have keyboard support! The PC controls use a one-arm variant control scheme. The controls are:

- *Arrow Keys*: Swing active arm
- *Left click*: Grab
- *Right click*: Safe transfer (if the free hand is over a grabbable object, grabs it and releases opposite hand)

To navigate the menus, use the arrow keys to hover over options and enter to select. Tap d to enter the level editor.

## Xbox Controls

### Grabbing
_Used to take hold of vines and other parts of the stage!_

- _**Left bumper (LB)**_: grab (_holding the bumper_) or release (_letting go of the bumper_) an object with **left** hand
- _**Right bumper (LB)**_: grab (_holding the bumper_) or release (_letting go of the bumper_) an object with **right** hand

_**NEW: Whenever you press RB or LB, a small yellow circle will appear over the respective hand. This circle will turn blue once that hand is grabbing an object.**_

_**NEW: Whenever you give joystick input, the corresonding arm with have a faint glow (light arm - blue, dark arm - white)**_


#### Rules for Grabbing

You are only able to grab branches and vines.


### Swinging
_Used to gain momentum on vines to swing yourself across the stage!_

- _**Left joystick**_: swing **darker** arm in whichever direction you tilt
- _**Right joystick**_: swing **lighter** arm in whichever direction you tilt

_**Note:** 360 degrees of motion is possible here_

**Tips:**
- If you ever forget which arm is which, my trick is that the black arm is on the left, and both words have l's in them, so I use the left joystick and button to use the that arm.
- Another trick is that the **right** arm is the arm with the b**right** color.


#### _Tips for Swinging **(Updated)**_

**Swinging has been improved since the last prototype.** Move the joystick of your respective arm applies a torque in the direction you tilt, causing the arm to swing. It is much easier to use one arm to swing now, but you will still be rewarded for using both joysticks (stages are designed assuming that you do not do this however).

- **Use your free arm to swing.** As before, using your heavy arms to bulid momentum will help you swing higher on vines. Think of swinging on a swing and how you move your legs to gain altitude. 
- **Move individual joysticks if you confuse the arms.** When you're confused about which arm is which, or you just don't know which way to position your arms, letting go of the joystick while still grabbing will help you reorient yourself.
- **Jump!** If you get stuck on the ground, try swinging your arms in opposite directions to the direction you want to go. The more your arms rotate, the further you jump. One of our emergent behaviors from swinging is that this motion will cause you to fly off the ground slightly, which should be enough height to get you level with a grabbable object.


### Misc.
- _**Start button**_: pause the game, allowing you to restart the stage or return to the main menu
- _**Y button**_: (if in a stage) activate the level editor for the given stage
- _**A button**_: restarts the level (used for faster playtesting)
- _**Any other button**_: nothing


## Hints for Completing Stages

- **You can grab any point on the vine.** If you find yourself being just short of reaching the vine or traveling to the vine too slowly, try swinging and launching yourself from a different height on the vine! 
- **Your player is actually very mobile.** With 360 degrees of movement, your character is able to swing himself around when grabbing an object. This is particularly useful for climbing up vines or for swinging yourself around to a different side of the vine!
- _**Utilize momentum.** Your arms are quite heavy. If you find yourself stuck hanging off of a branch, try to fling your free arm over to swing around the branch!_

## Known Issue(s)

- We have a fullscreen mode for the game that scales very well, but unfortunately this doesn't work well with the GUI. The final submission of the game won't have this problem though as the level editor will not be there.
- ~~You can only scroll on the main menu using the directional pad and not the joystick.~~

## Maximizing Fun

~~If you reach the goal, you can actually grab its center point and swing around on it! Since we don't have a sequence for completing the stage, you can swing around it as much as you want! Yay! Congrats for reaching the victory spot!~~ You can helicopter on rigid objects, but unfortunately no longer on the victory spot.

## Level Editor

- The level editor can be accessed via the main menu either by pressing _D_ on the keyboard _Y_ on the Xbox controller (pressing _Y_ on the Xbox controller anywhere else brings you to the main menu)
- From the level editor, you can toggle between playing and editing a stage by pressing _X_ on the Xbox controller (make sure you place Flow as a starting point first though!)
- You can leave the level editor by pressing _D_ on the keyboard _Y_ on the Xbox controller again (while not playing a stage in the level editor)



