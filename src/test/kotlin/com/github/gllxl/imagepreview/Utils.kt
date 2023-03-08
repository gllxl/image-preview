package com.github.gllxl.imagepreview

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class Utils : BasePlatformTestCase() {

    fun testIsImageUrl() {
        val imageUrl = "https://g.alicdn.com/buc-fe/sso-civil/1.19.0/assets/931f81d3d9d65243c35f0a481bd2872a.png"

        assertEquals(true, isImageUrl(imageUrl))
    }
}
