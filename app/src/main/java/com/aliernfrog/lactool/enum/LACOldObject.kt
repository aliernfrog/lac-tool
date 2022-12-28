package com.aliernfrog.lactool.enum

enum class LACOldObject(
    val objectName: String,
    val replaceObjectName: String,
    val replaceScale: String? = null,
    val replaceColor: String? = null
) {
    BLOCK_1BY1(
        objectName = "Block_1by1_Editor",
        replaceObjectName = "Block_Scalable_Editor",
        replaceScale = "1.0,1.0,1.0"
    ),

    BLOCK_3BY6(
        objectName = "Block_3by6_Editor",
        replaceObjectName = "Block_Scalable_Editor",
        replaceScale = "3.0,6.0,1.0"
    ),

    RED_SOFA(
        objectName = "Sofa_Chunk_Red_Editor",
        replaceObjectName = "Sofa_Chunk_Editor",
        replaceColor = "color{1.00,0.00,0.00}"
    )
}