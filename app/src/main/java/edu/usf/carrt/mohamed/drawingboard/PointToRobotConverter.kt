package edu.usf.carrt.mohamed.drawingboard

class PointToRobotConverter(val screenHeight: Int, val screenWidth: Int) {
    val A_THETA_UPPER_BOUND = 37000
    val A_THETA_LOWER_BOUND = 0
    val B_HORIZONATAL_UPPER_BOUND = 220000
    val B_LOWER_BOUND = 0
    val X_LENGTH = 155.0
    val Y_LENGTH = 205.0
    val R_TO_COUNT_CONVERSION = B_HORIZONATAL_UPPER_BOUND / 135
    val DEGREE_TO_COUNT_CONVERSION = 336.3

    private var position: PolarCord? = PolarCord(0.0, 0.0)
    private var motorA = 0 // Position of rotational motor
    private var motorB = 0 // Position of horizontal motor

    private var output: ArrayList<String> = ArrayList()

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

        position = PolarCord(0.0, 0.0)
        motorA = 0;
        motorB = 0;
    }

    private fun moveToPoint(point: PolarCord): Boolean {
        val rCount = point.r *  R_TO_COUNT_CONVERSION
        val thetaCounts = point.theta * DEGREE_TO_COUNT_CONVERSION + (A_THETA_UPPER_BOUND/2)
        val rOffset = (rCount - motorB).toInt()
        val tOffset = (thetaCounts - motorA).toInt()

        val expectedMotorA = motorA + tOffset
        val expectedMotorB = motorB + rOffset
        if (expectedMotorB > B_HORIZONATAL_UPPER_BOUND || expectedMotorB < B_LOWER_BOUND) {
//            endMovements()
            output.add("REM ERROR: CANT MOVE B:$motorB to position $rOffset\n")
            return false
        }

        if(expectedMotorA > A_THETA_UPPER_BOUND || expectedMotorA < A_THETA_LOWER_BOUND) {
//            endMovements()
            output.add("REM ERROR: CANT MOVE A:$motorA to position $tOffset\n")
            return false
        }

        motorA += tOffset
        motorB += rOffset

        position = PolarCord(point.r, point.theta)

        output.add("LM AB\n")
        output.add("LI $tOffset, $rOffset\n")
        output.add("LE;BGS;AMA;AMB;AMC;\n")
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

    fun drawPicture(imageToDraw: ImageToDraw): String {
        initialize()


        for (segment in imageToDraw.segments) {
            moveToPoint(segment.lines[0])
            placePenDown()
            drawLinesWithLI(segment.lines)
            placePenUp()
        }

        endMovements()

        return output.joinToString("")
    }

    private fun drawLinesWithLI(lines: ArrayList<PolarCord>) {
        output.add("LM AB\n")
        for (line in lines) {
            val rCount = line.r * R_TO_COUNT_CONVERSION
            val thetaCounts = line.theta * DEGREE_TO_COUNT_CONVERSION + (A_THETA_UPPER_BOUND/2)
            val rOffset = (rCount - motorB).toInt()
            val tOffset = (thetaCounts - motorA).toInt()

            if (motorB + rOffset > B_HORIZONATAL_UPPER_BOUND || motorB + rOffset < B_LOWER_BOUND) {
                output.add("REM ERROR: CANT MOVE B:$motorB by adding offset:$rOffset\n")
                continue
            }
            if(motorA + tOffset > A_THETA_UPPER_BOUND || motorA + tOffset < A_THETA_LOWER_BOUND) {
                output.add("REM ERROR: CANT MOVE A:$motorA  by adding offset:$tOffset\n")
                continue
            }

            if (rOffset == 0 && tOffset == 0)
                continue

            if (tOffset == 0)
                continue

            motorA += tOffset
            motorB += rOffset

            position = PolarCord(line.r, line.theta)

            output.add("LI $tOffset, $rOffset\n")
        }

        output.add("LE;BGS;AM\n")
        output.add("PA $motorA, $motorB, 1000\n")
        output.add("BG;AM\n")
    }


    fun convertToPolarBasedOffScreenSize(originalX: Float, originalY: Float): PolarCord {
        val x = originalX
        val y = originalY
        val l1 = 45.0
        val l2 = 10.0
        val h = 30.0
        val startT = -43
        val armT = Math.atan2(h, l1+l2) * (180/Math.PI)
        val armR = Math.sqrt(Math.pow(h,2.0) + Math.pow(l1+l2,2.0))
        // Y calculations
        val halfScreen = (screenHeight/2);
        val newY = halfScreen - originalY
        val percentY = newY/ halfScreen


        val posX = (x/screenWidth)* X_LENGTH + l1
        val posY = percentY*.5* Y_LENGTH

        val penT = Math.atan2(posY, posX) * (180/Math.PI)
        val penR = Math.sqrt(Math.pow(posX,2.0) + Math.pow(posY,2.0))

        val encoderT = penT - armT - startT
        val encoderR = Math.sqrt(Math.pow(penR,2.0)- Math.pow(h,2.0)) - armR

        return PolarCord(encoderR,encoderT)
    }

}

