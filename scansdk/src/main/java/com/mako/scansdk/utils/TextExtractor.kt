package com.mako.scansdk.utils

import com.google.mlkit.vision.text.Text

internal object TextExtractor {
    /**
     * Extracts text from the recognized data
     * @param text recognized by SDK
     * @return list of strings
     */
    fun extractAllTexts(text: Text): List<String> {
        val result = mutableListOf<String>()
        for (block in text.textBlocks) {
            for (line in block.lines) {
                val lineText = line.text
                result.add(lineText)
            }
        }

        return result.toList()
    }
}