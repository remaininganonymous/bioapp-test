package example.com.plugins

import example.com.services.findAnnotation
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    } //TODO
    routing {
        get("/about") {
            call.respondText("REST API для предоставления информации об аннотации генетических вариантов на Kotlin/Ktor\n")
        }

        get("/info") {
            val rac = call.request.queryParameters["rac"]
            val lap = call.request.queryParameters["lap"]
            val rap = call.request.queryParameters["rap"]
            val refKey = call.request.queryParameters["refKey"]
            val file = call.request.queryParameters["file"]

            val missingParams = mutableListOf<String>()

            if (rac == null) missingParams.add("rac")
            if (lap == null) missingParams.add("lap")
            if (rap == null) missingParams.add("rap")
            if (refKey == null) missingParams.add("refKey")
            if (file == null) missingParams.add("file")

            if (missingParams.isNotEmpty()) {
                if (missingParams.size == 1) {
                    call.respond(HttpStatusCode.BadRequest,"400 Bad Request: Не был предоставлен параметр: ${missingParams[0]}")
                } else {
                    call.respond(HttpStatusCode.BadRequest,"400 Bad Request: Не был(-и) предоставлен(-ы) параметр(-ы): ${missingParams.joinToString(", ")}")
                }
                return@get
            }

            rac?.let { r ->
                lap?.let { l ->
                    rap?.let { rp ->
                        refKey?.let { rk ->
                            file?.let { file ->
                                try {
                                    val result = findAnnotation(r, l.toInt(), rp.toInt(), rk, file, call.application.environment)
                                    if (result != null) {
                                        call.respond(result)
                                    } else {
                                        call.respond(HttpStatusCode.NotFound, "404 Not Found: Ни одна аннотация не была найдена для введенного запроса")
                                    }
                                } catch (e: IllegalArgumentException) {
                                    call.respond(HttpStatusCode.BadRequest, "400 Bad Request: ${e.message}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
