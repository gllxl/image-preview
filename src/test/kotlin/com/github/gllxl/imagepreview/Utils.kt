package com.github.gllxl.imagepreview

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class Utils : BasePlatformTestCase() {

    fun testIsImageUrl() {
        val pngImageUrl = "https://g.alicdn.com/buc-fe/sso-civil/1.19.0/assets/931f81d3d9d65243c35f0a481bd2872a.png"
        val jpgImageUrl = "https://img.zcool.cn/community/0147cb5f58366211013e3187e8c713.jpg"
        val httpImageUrl = "http://g.alicdn.com/buc-fe/sso-civil/1.19.0/assets/931f81d3d9d65243c35f0a481bd2872a.png"
        val noImageUrl = "//g.alicdn.com/buc-fe/sso-civil/1.19.0/assets/931f81d3d9d65243c35f0a481bd2872a.png"

        assertEquals(true, isImageUrl(pngImageUrl))
        assertEquals(true, isImageUrl(jpgImageUrl))
        assertEquals(true, isImageUrl(httpImageUrl))
        assertEquals(false, isImageUrl(noImageUrl))
    }
}
