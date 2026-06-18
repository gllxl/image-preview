package com.github.gllxl.imagepreview

import com.github.gllxl.imagepreview.extractor.JsImageReferenceExtractor
import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.json.psi.JsonValue
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.psi.PsiElement
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.lang.reflect.Array as ReflectArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JsImageReferenceExtractorTest {

  @Test
  fun testJsVariableImageLiteralIsExtracted() {
    val literal = jsStringLiteral("https://cdn.example.com/assets/logo.png")
    val variable = proxy<JSVariable> { method, _ ->
      when (method.name) {
        "getInitializer" -> literal
        else -> defaultValue(method.returnType)
      }
    }

    assertEquals("https://cdn.example.com/assets/logo.png", JsImageReferenceExtractor.extractUrl(variable))
  }

  @Test
  fun testJsObjectPropertyImageLiteralIsExtracted() {
    val literal = jsStringLiteral("./assets/card.webp")
    val property = proxy<JSProperty> { method, _ ->
      when (method.name) {
        "getValue" -> literal
        else -> defaultValue(method.returnType)
      }
    }

    assertEquals("./assets/card.webp", JsImageReferenceExtractor.extractUrl(property))
  }

  @Test
  fun testJsArrayImageLiteralIsExtracted() {
    val array = proxy<JSArrayLiteralExpression> { method, _ -> defaultValue(method.returnType) }
    val literal = jsStringLiteral("../images/hero.svg", parent = array)

    assertEquals("../images/hero.svg", JsImageReferenceExtractor.extractUrl(literal))
  }

  @Test
  fun testJsonObjectPropertyImageLiteralIsExtracted() {
    val literal = jsonStringLiteral("/public/banner.jpeg")
    val property = proxy<JsonProperty> { method, _ ->
      when (method.name) {
        "getValue" -> literal
        else -> defaultValue(method.returnType)
      }
    }

    assertEquals("/public/banner.jpeg", JsImageReferenceExtractor.extractUrl(property))
  }

  @Test
  fun testJsonArrayImageLiteralIsExtracted() {
    val array = proxy<JsonArray> { method, _ -> defaultValue(method.returnType) }
    val literal = jsonStringLiteral("https://cdn.example.com/gallery/one.jpg", parent = array)

    assertEquals("https://cdn.example.com/gallery/one.jpg", JsImageReferenceExtractor.extractUrl(literal))
  }

  @Test
  fun testJsonPropertyNamesAreIgnored() {
    val literal = jsonStringLiteral("https://cdn.example.com/key.png", isPropertyName = true)

    assertNull(JsImageReferenceExtractor.extractUrl(literal))
  }

  @Test
  fun testNonImageStringsAreIgnored() {
    val array = proxy<JSArrayLiteralExpression> { method, _ -> defaultValue(method.returnType) }
    val literal = jsStringLiteral("https://cdn.example.com/readme.txt", parent = array)

    assertNull(JsImageReferenceExtractor.extractUrl(literal))
    assertNull(JsImageReferenceExtractor.normalizeImageReference("plain text"))
  }

  @Test
  fun testInterpolatedTemplateStringsAreIgnored() {
    val array = proxy<JSArrayLiteralExpression> { method, _ -> defaultValue(method.returnType) }
    val argument = jsStringLiteral("avatar")
    val literal = proxy<JSStringTemplateExpression> { method, _ ->
      when (method.name) {
        "getParent" -> array
        "getStringValue" -> "./assets/${'$'}{name}.png"
        "getValue" -> "./assets/${'$'}{name}.png"
        "getText" -> "`./assets/${'$'}{name}.png`"
        "getArguments" -> arrayOf<JSExpression>(argument)
        else -> defaultValue(method.returnType)
      }
    }

    assertNull(JsImageReferenceExtractor.extractUrl(literal))
  }

  private fun jsStringLiteral(value: String, parent: PsiElement? = null): JSLiteralExpression {
    return proxy { method, _ ->
      when (method.name) {
        "getParent" -> parent
        "getStringValue" -> value
        "getValue" -> value
        "getText" -> "\"$value\""
        else -> defaultValue(method.returnType)
      }
    }
  }

  private fun jsonStringLiteral(
    value: String,
    isPropertyName: Boolean = false,
    parent: PsiElement? = null,
  ): JsonStringLiteral {
    return proxy { method, _ ->
      when (method.name) {
        "getParent" -> parent
        "getValue" -> value
        "isPropertyName" -> isPropertyName
        "getText" -> "\"$value\""
        else -> defaultValue(method.returnType)
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  private inline fun <reified T> proxy(noinline handler: (Method, Array<Any?>?) -> Any?): T {
    return Proxy.newProxyInstance(
      T::class.java.classLoader,
      arrayOf(T::class.java),
    ) { instance, method, args ->
      when (method.name) {
        "equals" -> instance === args?.firstOrNull()
        "hashCode" -> System.identityHashCode(instance)
        "toString" -> "Proxy(${T::class.java.simpleName})"
        else -> handler(method, args)
      }
    } as T
  }

  private fun defaultValue(returnType: Class<*>): Any? {
    return when {
      returnType == Boolean::class.javaPrimitiveType -> false
      returnType == Int::class.javaPrimitiveType -> 0
      returnType == Long::class.javaPrimitiveType -> 0L
      returnType == Double::class.javaPrimitiveType -> 0.0
      returnType == Float::class.javaPrimitiveType -> 0f
      returnType.isArray -> ReflectArray.newInstance(returnType.componentType, 0)
      JsonValue::class.java.isAssignableFrom(returnType) -> null
      JSExpression::class.java.isAssignableFrom(returnType) -> null
      PsiElement::class.java.isAssignableFrom(returnType) -> null
      else -> null
    }
  }
}
