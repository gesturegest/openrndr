package org.openrndr.internal

import org.openrndr.draw.*
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3

/**
 * Created by voorbeeld on 11/23/17.
 */
class PerformanceLineDrawer {
    val vertices: VertexBuffer = VertexBuffer.createDynamic(VertexFormat().apply {
        position(3)
        attribute("instance", 1, VertexElementType.FLOAT32)
        attribute("vertexOffset", 1, VertexElementType.FLOAT32)
    }, 1024 * 1024)

    val shaderManager: ShadeStyleManager

    init {
        shaderManager = ShadeStyleManager.fromGenerators(Driver.instance.shaderGenerators::fastLineVertexShader,
                Driver.instance.shaderGenerators::fastLineFragmentShader)
    }

    fun drawLineSegments3(drawContext: DrawContext,
                          drawStyle: DrawStyle, segments: Iterable<Vector3>) {

        val shader = shaderManager.shader(drawStyle.shadeStyle, vertices.vertexFormat)
        shader.begin()
        drawContext.applyToShader(shader)
        drawStyle.applyToShader(shader)
        val w = vertices.shadow.writer()

        w.rewind()
        var segmentCount = 0
        segments.forEachIndexed { index, it ->
            w.write(it)
            w.write((index / 2).toFloat())
            w.write((index % 2).toFloat())
            segmentCount++
        }
        vertices.shadow.uploadElements(0, segmentCount)

        Driver.instance.setState(drawStyle)
        Driver.instance.drawVertexBuffer(shader, listOf(vertices), DrawPrimitive.LINES, 0, 6)
        shader.end()
    }

    fun drawLineSegments2(drawContext: DrawContext,
                          drawStyle: DrawStyle, segments: Iterable<Vector2>) {

        val shader = shaderManager.shader(drawStyle.shadeStyle, vertices.vertexFormat)
        shader.begin()
        drawContext.applyToShader(shader)
        drawStyle.applyToShader(shader)
        val w = vertices.shadow.writer()

        w.rewind()

        var segmentCount = 0
        segments.forEachIndexed { index, it ->
            w.write(it); w.write(0.0f)
            w.write((index / 2).toFloat())
            w.write((index % 2).toFloat())
            segmentCount++
        }
        vertices.shadow.uploadElements(0, segmentCount)

        Driver.instance.setState(drawStyle)
        Driver.instance.drawVertexBuffer(shader, listOf(vertices), DrawPrimitive.LINES, 0, 6)
        shader.end()
    }

    fun drawLineLoops(drawContext: DrawContext,
                      drawStyle: DrawStyle, loops: Iterable<List<Vector2>>) {

        val shader = shaderManager.shader(drawStyle.shadeStyle, vertices.vertexFormat)
        shader.begin()
        drawContext.applyToShader(shader)
        drawStyle.applyToShader(shader)
        val w = vertices.shadow.writer()
        w.rewind()
        var vertexCount = 0

        loops.forEachIndexed { loopIndex, loop ->
            //loop.forEachIndexed { index, it ->
            for (i in 0 until loop.size - 1) {
                w.write(loop[i]); w.write(0.0f)
                w.write(loopIndex.toFloat())
                w.write(1.0f)
                w.write(loop[i + 1]); w.write(0.0f)
                w.write(loopIndex.toFloat())
                w.write(1.0f)

                vertexCount += 2
            }
            //}
        }
        vertices.shadow.uploadElements(0, vertexCount)
        Driver.instance.setState(drawStyle)
        Driver.instance.drawVertexBuffer(shader, listOf(vertices), DrawPrimitive.LINES, 0, vertexCount)
        shader.end()
    }
}