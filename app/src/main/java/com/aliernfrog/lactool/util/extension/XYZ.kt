package com.aliernfrog.lactool.util.extension

import com.aliernfrog.lactool.data.XYZ

/**
 * Adds [xyz] and returns the new [XYZ]
 * @param xyz [XYZ] to add
 * @return new [XYZ]
 */
fun XYZ.add(xyz: XYZ): XYZ {
    return XYZ(
        x = this.x + xyz.x,
        y = this.y + xyz.y,
        z = this.z + xyz.z
    )
}

/**
 * Creates a string separated by [separator]
 * @return [String]
 */
fun XYZ.joinToString(separator: String = ","): String {
    return listOf(this.x, this.y, this.z).joinToString(separator)
}