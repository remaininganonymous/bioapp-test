package example.com.services

import example.com.dataclasses.AnnotationResult
import htsjdk.tribble.readers.TabixReader
import io.ktor.server.application.*
import java.io.IOException

fun findAnnotation(rac: String, lap: Int, rap: Int, refKey: String, file: String, environment: ApplicationEnvironment): AnnotationResult? {
    val filePath = environment.config.property("data.folderPath").getString() + file
    TabixReader(filePath).use { reader ->
        val query = "$rac:$lap-$rap"
        val iterator = reader.query(query)

        var line: String? = null
        try {
            while (iterator.next()?.also { line = it } != null) {
                val fields = line!!.split("\t")
                if (fields[0] == rac && fields[1].toInt() == lap && fields[2].toInt() == rap && fields[3] == refKey) {
                    val annotation = AnnotationResult(
                        rac = fields[0],
                        lap = fields[1].toInt(),
                        rap = fields[2].toInt(),
                        refkey = fields[3],
                        vcfId = fields[4],
                        clnsig = fields[5],
                        clnrevstat = fields[6],
                        clnvc = fields[7]
                    )
                    return annotation
                }
            }
        } catch (e: IOException) {
            println("Ошибка чтения: ${e.message}")
        }
        return null
    }
}