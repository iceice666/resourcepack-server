package me.iceice666.rps

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.file.TomlFileReader
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import me.iceice666.rps.ModMain.ROOT_DIR
import java.nio.file.Path
import kotlin.io.path.isReadable
import kotlin.io.path.isRegularFile


@Serializable
data class Config(
    val port: Int,
    val resourcepackPath: String,
    val iAmSureWhatAmIDoing: Boolean
) {
    companion object {
        private val path: Path = ROOT_DIR.resolve("config/rps.toml")

        fun new(): Config {
            return when (path.isRegularFile() && path.isReadable()) {
                true -> TomlFileReader.decodeFromFile(
                    serializer(),
                    path.toString()
                )

                false -> Config(
                    11451,
                    "./rps/resourcepack.zip",
                    false
                )
            }
        }

        fun save() {
            val text = Toml.encodeToString(this)
            path.toFile().writeText(text)
        }
    }
}
