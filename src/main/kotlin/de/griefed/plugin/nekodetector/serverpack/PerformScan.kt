/*
 * MIT License
 *
 * Copyright (c) 2023 Griefed
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package de.griefed.plugin.nekodetector.serverpack

import me.cortex.jarscanner.Main
import me.cortex.jarscanner.Results
import org.apache.logging.log4j.LogManager
import java.awt.Desktop
import java.awt.Frame
import java.awt.Window
import java.nio.file.Path
import javax.swing.JDialog
import javax.swing.JOptionPane

abstract class PerformScan {
    private val pluginsLog = LogManager.getLogger("AddonsLogger")
    fun scan(destination: Path, extension: String) {
        pluginsLog.info("$extension - Scanning $destination for infections....")
        try {
            val output: (String) -> String = { "" }
            val run = Main.run(4, destination, true, output)
            val entries = getScanResults(run)
            var message = ""
            if (entries.isNotEmpty()) {
                for (entry in entries) {
                    pluginsLog.info(entry)
                    message += "$entry\n"
                }
                if (Desktop.isDesktopSupported() && entries.isNotEmpty()) {
                    val dialog = createNonModalDialog(message,extension)
                    dialog.isVisible = true
                }
            }
        } catch (ex: Exception) {
            pluginsLog.error("Error during scan.", ex)
        }
    }

    private fun createNonModalDialog(message: String, title: String) : JDialog {
        val pane = JOptionPane()
        val dialog: JDialog
        pane.optionType = JOptionPane.PLAIN_MESSAGE
        pane.message = message
        dialog = pane.createDialog(Frame.getFrames()[0],title)
        dialog.isModal = false
        dialog.defaultCloseOperation = JDialog.DISPOSE_ON_CLOSE
        return dialog
    }

    private fun getScanResults(results: Results?): List<String> {
        val output = mutableListOf<String>()
        if (results == null) {
            output.add("Scan failed. No results. Check the logs for possible reasons.")
            return output.toList()
        }
        if (results.stage1Detections.isNotEmpty()) {
            output.add("Stage 1 infections:")
            for (entry in results.stage1Detections) {
                output.add(entry)
                output.add("\n");
            }
        }
        if (results.stage2Detections.isNotEmpty()) {
            output.add("Stage 2 infections:")
            for (entry in results.stage2Detections) {
                output.add(entry)
                output.add("\n");
            }
        }

        return output.toList()
    }
}