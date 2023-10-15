@file:JvmName("Config")

package net.iceice666.lib

import java.io.File
import java.nio.file.Path

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Comment(val value: String)


class ConfigLoader<T : CustomConfig>(private val customConfig: T) {

    private var config: MutableMap<String, Pair<String, Any>> = mutableMapOf()

    /**
     * update the config class
     * and will update the config map
     *
     * @param config the config map
     * @return the config class
     */

    private fun updateConfigClass(config: MutableMap<String, Pair<String, Any>>): T {
        this.config = config

        val classFields = customConfig.javaClass.declaredFields
        for (classField in classFields) {
            if (classField.name == "configFilePath") {
                continue
            }
            classField.isAccessible = true
            val (_, value) = config[classField.name] ?: continue

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
        }


        return customConfig
    }


    private fun generateConfigText(config: T): String {
        val classFields = config.javaClass.declaredFields
        var configText = ""
        for (classField in classFields) {
            if (classField.name == "configFilePath") {
                continue
            }
            classField.isAccessible = true
            val comment = classField.getAnnotation(Comment::class.java)
            val value = classField.get(config)
            if (comment != null) {
                configText += comment.value.split("\n").joinToString("\n") { "# $it" } + "\n"
            }
            configText += "${classField.name}=$value\n\n"
        }
        return configText
    }

    private fun generateConfigText(config: MutableMap<String, Pair<String, Any>>): String {
        var configText = ""
        for ((key, value) in config) {
            val (comment, value) = value
            if (comment != "") {
                configText += comment + "\n"
            }
            configText += "$key=$value\n\n"
        }
        return configText
    }

    private fun getConfigFromFile(file: File): T {


        val configFromFile: MutableMap<String, Pair<String, Any>> = mutableMapOf()
        val lines = file.readLines()
        var comment = ""
        for ((i, line) in lines.withIndex()) {
            if (line == "") {
                continue
            }
            if (line.startsWith("#")) {
                comment += line + "\n"
                continue
            }
            val splited = line.split("=")
            if (splited.size != 2) {
                throw RuntimeException("Config file format error at line ${i + 1}")
            }
            val (key, value) = splited
            configFromFile[key] = Pair(comment, value)
            comment = ""
        }
        return updateConfigClass(configFromFile)

    }

    private fun getConfigFromClass(customConfig: T): T {
        val config: MutableMap<String, Pair<String, Any>> = mutableMapOf()
        val classFields = customConfig.javaClass.declaredFields
        for (classField in classFields) {
            if (classField.name == "configFilePath") {
                continue
            }

            classField.isAccessible = true
            val comment = classField.getAnnotation(Comment::class.java)
            val value = classField.get(customConfig)
            if (comment != null) {
                config[classField.name] =
                    Pair((comment.value.trimIndent().split("\n").joinToString("\n") { "# $it" }), value)
            } else {
                config[classField.name] = Pair("", value)
            }
        }
        return updateConfigClass(config)
    }

    fun loadConfig(): T {
        val file = customConfig.configFilePath.toFile()
        val config = if (file.exists() && file.isFile) {
            getConfigFromFile(
                customConfig.configFilePath.toFile()
            )
        } else {
            val configFromClass = getConfigFromClass(customConfig)
            file.createNewFile()
            file.writeText(generateConfigText(configFromClass))
            configFromClass
        }

        return config
    }


    fun saveConfig() {
        val file = customConfig.configFilePath.toFile()

        getConfigFromClass(customConfig)
        file.writeText(generateConfigText(this.config))
    }


}


interface CustomConfig {

    val configFilePath: Path


}
