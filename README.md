# DrawingRobotApp

How to start the robot:
1. Plug power in. 21.5 Volts- 23 and anything above 2 Amps for the current
2. Plug into Raspberry Pi
3. Turn on raspberry pi

How to draw something: 
1. Run this android app
2. Connect to the wifi network DrawingRobot from the Rapsberry Pi
3. Draw and hit send!


How to take a picture:
1. Click the third icon from the right
2. Adjust the slider to add or remove lines
3. Hit send
!Note this doesnt work quite well. Good luck on fixing this!


## Where the code is:

/DrawingRobotApp/app/src/main/java/edu/usf/carrt/mohamed/drawingboard/PointToRobotConverter.kt
PointToRobotConverter = Draws and takes points and creates the encoding code
/DrawingRobotApp/app/src/main/java/edu/usf/carrt/mohamed/drawingboard/EdgeDetectionActivity.kt
EdgeDetectionActivity= Takes an image from openCV, makes it into lines, and makes those lines into something the PointToRobotConverter can undestand


Some useful tools to help you get started:
https://kotlinlang.org/docs/tutorials/
Transformations of cartesian to polar coordinates
