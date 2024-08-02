package example.com.plugins

import example.com.services.findAnnotation
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.IOException
//
fun validateParams(call: ApplicationCall): List<String> {
    val missingParams = mutableListOf<String>()
    if (call.request.queryParameters["rac"] == null) missingParams.add("rac")
    if (call.request.queryParameters["lap"] == null) missingParams.add("lap")
    if (call.request.queryParameters["rap"] == null) missingParams.add("rap")
    if (call.request.queryParameters["refKey"] == null) missingParams.add("refKey")
    if (call.request.queryParameters["file"] == null) missingParams.add("file")
    return missingParams
}

suspend fun handleInfoRequest(call: ApplicationCall) {
    val missingParams = validateParams(call)
    if (missingParams.isNotEmpty()) {
        val message = if (missingParams.size == 1) {
            "400 Bad Request: Не был предоставлен параметр: ${missingParams[0]}"
        } else {
            "400 Bad Request: Не были предоставлены параметры: ${missingParams.joinToString(", ")}"
        }
        call.respond(HttpStatusCode.BadRequest, message)
        return
    }

    val rac = call.request.queryParameters["rac"]!!
    val lap = call.request.queryParameters["lap"]!!.toInt()
    val rap = call.request.queryParameters["rap"]!!.toInt()
    val refKey = call.request.queryParameters["refKey"]!!
    val file = call.request.queryParameters["file"]!!

    try {
        val result = findAnnotation(rac, lap, rap, refKey, file, call.application.environment)
        if (result != null) {
            call.respond(result)
        } else {
            call.respond(HttpStatusCode.NotFound, "404 Not Found: Для введенного запроса аннотаций не найдено")
        }
    } catch (e: IllegalArgumentException) {
        call.respond(HttpStatusCode.BadRequest, "400 Bad Request: ${e.message}")
    }
}

fun Application.configureRouting(environment: ApplicationEnvironment) {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is IllegalArgumentException -> call.respondText("400: ${cause.message}", status = HttpStatusCode.BadRequest)
                is IOException -> call.respondText("500: Ошибка ввода-вывода", status = HttpStatusCode.InternalServerError)
                else -> call.respondText("500: Внутренняя ошибка сервера", status = HttpStatusCode.InternalServerError)
            }
            environment.log.error("Unhandled exception: ${cause.message}", cause)
        }
    }
    routing {
        get("/about") {
            call.respondText("REST API для предоставления информации об аннотации генетических вариантов на Kotlin/Ktor\n")
        }
        get("/info") {
            handleInfoRequest(call)
        }
    }
}
