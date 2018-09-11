package edu.usf.carrt.mohamed.drawingboard

import edu.usf.carrt.mohamed.drawingboard.PolarCord
import java.util.*
import kotlin.collections.ArrayList

class PointToRobotConverter (val screenHeight: Int, val screenWidth: Int) {
    val A_UPPER_BOUND = 23100
    val A_LOWER_BOUND = - A_UPPER_BOUND
    val B_UPPER_BOUND = 179000
    val B_LOWER_BOUND = 0
    val R_TO_COUNT_CONVERSION = 2105.58
    val DEGREE_TO_COUNT_CONVERSION = 356.15
    val X_LENGTH = 80
    val Y_LENGTH = 52.5
    val X_CONVERSION_RATE = X_LENGTH / screenWidth.toFloat()
    val Y_CONVERSION_RATE = Y_LENGTH / screenHeight.toFloat()

    private var position : PolarCord? = PolarCord(0f,0f)
    private var motorA = 0 // Position of rotational motor
    private var motorB = 0 // Position of horizontal motor

    private var output : ArrayList<String> = ArrayList()

    private fun initialize() {
        output = ArrayList()
        output.add("SH\n")
        output.add("PA 0,0,0;BG;AM;")
        placePenUp()
    }

    private fun endMovements() {
        output.add("PAC = -1000\n")
        output.add("BGC;AM\n")
        output.add("PA 0,0, -1000\n")
        output.add("BG;AM\n")
        placePenUp();
        output.add("MOAB;WT10;MOAB;END;\n")

        position = PolarCord(0f,0f)
    }

    private fun moveToPoint(point: PolarCord) : Boolean {
        val rCount = point.r * R_TO_COUNT_CONVERSION
        val thetaCounts = point.theta * DEGREE_TO_COUNT_CONVERSION
        val rOffset = rCount - motorB
        val tOffset = thetaCounts - motorA

        val expectedMotorB = motorB + rOffset
        val expectedMotorA = motorA + tOffset
        if     (expectedMotorB > B_UPPER_BOUND || expectedMotorB < B_LOWER_BOUND ||
                expectedMotorA > A_UPPER_BOUND || expectedMotorA < A_LOWER_BOUND
        ) {
            output.add("ERROR: CANT MOVE B:$motorB to position $rOffset OR A:$motorA to position $tOffset\n")
            return false
        }

        motorA += tOffset.toInt()
        motorB += rOffset.toInt()

        position = PolarCord(point.r, point.theta)

        output.add("LM A\n")
        output.add("LI ${tOffset.toInt()}\n")
//        output.add("LM AB\n")
//        output.add("LI $tOffset.toInt(), $rOffset.toInt()\n")
        output.add("LE;BGS;AM\n")
        return true
    }

    private fun placePenDown() {
        output.add("PAC = 1000\n")
        output.add("BGC;AMC\n")
    }

    private fun placePenUp() {
        output.add("PAC = -1000\n")
        output.add("BGC;AMC\n")
    }

    fun drawPicture(imageToDraw: ImageToDraw) : String {
        initialize()


        for(segment in imageToDraw.segments) {
            moveToPoint(segment.lines[0])
            placePenDown()
            drawLinesWithLI(segment.lines)
            placePenUp()
        }

        endMovements()

        return output.joinToString("")
    }

    private fun drawLinesWithLI(lines: ArrayList<PolarCord>) {
        var errorFound = false;
//        output.add("LM AB\n")
        output.add("LM A\n")
        for(line in lines) {
            val rCount = line.r * R_TO_COUNT_CONVERSION
            val thetaCounts = line.theta * DEGREE_TO_COUNT_CONVERSION
            val rOffset = (rCount - motorB).toInt()
            val tOffset = (thetaCounts - motorA).toInt()

            if     (motorB + rOffset > B_UPPER_BOUND || motorB + rOffset < B_LOWER_BOUND ||
                    motorA + tOffset > A_UPPER_BOUND || motorA + rOffset < A_LOWER_BOUND
                    ) {
                errorFound = true
                output.add("ERROR: CANT MOVE B:$motorB by adding offset:$rOffset OR" +
                        "\n ERROR: CANT MOVE A:$motorA  by adding offset:$tOffset\n")
                break
            }

            if(rOffset == 0 && tOffset == 0)
                continue

            if(tOffset == 0)
                continue

            motorA += tOffset
            motorB += rOffset

            position = PolarCord(line.r, line.theta)

//            output.add("LI $tOffset, $rOffset\n")
            output.add("LI ${tOffset}\n")
        }


        if(errorFound) {
            // Error managment
            output.add("\nMO\n")
            return
        }


        output.add("LE;BGS;AM\n")
//        output.add("PA $motorA, $motorB, 1000\n")
        output.add("PA $motorA,, 1000\n")
        output.add("BG;AM\n")
    }


    fun convertToPolarBasedOffScreenSize(originalX: Float, originalY: Float) : PolarCord {
        val x = (originalX * X_CONVERSION_RATE).toDouble() + 40
        val y = if (originalY > screenHeight * 0.5) (originalY - (screenHeight*0.5)) * Y_CONVERSION_RATE * -1 else (screenHeight*0.5 - originalY) * Y_CONVERSION_RATE


        val r = Math.sqrt(Math.pow(x,2.0) + Math.pow(y, 2.0))
        val theta = Math.atan2(y, x) * (180/Math.PI) * 0.55

        return PolarCord(r.toFloat() - 40, theta.toFloat())
    }

}

