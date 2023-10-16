@file:JvmName("Config")

package net.iceice666.lib

import java.io.File
import java.nio.file.Path

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Comment(val value: String)


class ConfigLoader<T : CustomConfig>(private val customConfig: T) {

    private var comment: MutableMap<String, String> = mutableMapOf()

    /**
     * update the config class
     * and will update the config map
     *
     * @param config the config map
     * @return the config class
     */

    private fun updateConfigClass(config: MutableMap<String, Any>) {

        val classFields = customConfig.javaClass.declaredFields
        for (classField in classFields) {
            if (classField.name == "configFilePath") {
                continue
            }
            classField.isAccessible = true
            val value = config[classField.name] ?: continue

            classField.set(
                customConfig, when (classField.type) {
                    Char::class.java -> value.toString().toCharArray()[0]
                    String::class.java -> value.toString()
                    Float::class.java -> value.toString().toFloat()
                    Int::class.java -> value.toString().toInt()
                    Boolean::class.java -> value.toString().toBoolean()
                    else -> value
                }
            )

            val comment = comment[classField.name]
            if (comment != null) {
                this.comment[classField.name] = comment
            } else {
                this.comment[classField.name] = classField
                    .getAnnotation(Comment::class.java)
                    ?.value
                    ?.trimIndent()
                    ?.split("\n")
                    ?.joinToString("\n") { "# $it" }
                    ?: ""
            }
        }
    }


    private fun generateConfigText(config: T): String {
        val classFields = config.javaClass.declaredFields
        var configText = ""
        for (classField in classFields) {
            if (classField.name == "configFilePath") {
                continue
            }
            classField.isAccessible = true

            val comment = this.comment[classField.name]
            if (comment != null) {
                configText += comment + "\n"
            }

            val value = classField.get(config)
            configText += "${classField.name}=$value\n\n"
        }

        configText += this.comment["_LAST_LINE"]
        return configText
    }


    private fun updateConfigFromFile(file: File) {

        val config: MutableMap<String, Any> = mutableMapOf()
        val comment: MutableMap<String, String> = mutableMapOf()
        val lines = file.readLines()
        var tempComment = ""
        for ((i, line) in lines.withIndex()) {
            if (i == lines.count() && (line == "" || line.startsWith("#"))) {
                tempComment += line.substring(1)

                comment["_LAST_LINE"] = tempComment
                break
            }

            if (line == "") {
                continue
            }

            if (line.startsWith("#")) {
                tempComment += line + "\n"
                continue
            }

            val split = line.split("=")
            if (split.size < 2) {
                throw RuntimeException("Config file format error at line ${i + 1}")
            }
            val key = split[0]
            config[key] = split.drop(0).joinToString { "=" }
            comment[key] = tempComment
            tempComment = ""
        }

        updateConfigClass(config)
    }


    fun loadConfig(): T {
        val file = customConfig.configFilePath.toFile()

        if (file.exists() && file.isFile) {
            updateConfigFromFile(
                customConfig.configFilePath.toFile()
            )
        } else {
            file.createNewFile()
            file.writeText(generateConfigText(customConfig))
        }

        return customConfig
    }


    fun saveConfig() {
        val file = customConfig.configFilePath.toFile()

        file.writeText(generateConfigText(customConfig))
    }
}


interface CustomConfig {
    val configFilePath: Path
}
