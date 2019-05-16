package edu.usf.carrt.mohamed.drawingboard

import java.io.Serializable

class Segment: Serializable {
    var lines: ArrayList<PolarCord> = ArrayList()
    var color: Int = 0
}
