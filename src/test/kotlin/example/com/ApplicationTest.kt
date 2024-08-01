package example.com

import example.com.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        client.get("/about").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("REST API для предоставления информации об аннотации генетических вариантов на Kotlin/Ktor\n", bodyAsText())
        }
    }

    @Test
    fun testInfoRouteMissingParam() = testApplication {//пропущен параметр
        client.get("/info?rac=NC_000002.12&lap=0&rap=0&refKey=C").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            assertTrue(bodyAsText().contains("400 Bad Request: Не был предоставлен параметр: file"))
        }
    }

    @Test
    fun testInfoRouteMissingMultipleParams() = testApplication {//пропущены несколько параметров
        client.get("/info").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            assertTrue(bodyAsText().contains("400 Bad Request: Не были предоставлены параметры: rac, lap, rap, refKey, file"))
        }
    }

    @Ignore("Не пройдет при сборке для Docker")
    @Test
    fun testInfoRouteAnnotationFound() = testApplication {
        client.get("/info?rac=NC_000002.12&lap=32136583&rap=32136585&refKey=C&file=testfiles\\clinvar_20220430.aka.gz").apply {
            assertEquals(HttpStatusCode.OK, status)
            val responseBody = bodyAsText()
            val expectedJson = """
                {"rac":"NC_000002.12","lap":32136583,"rap":32136585,"refkey":"C","vcfId":"1344043","clnsig":"Uncertain_significance","clnrevstat":"criteria_provided,_single_submitter","clnvc":"single_nucleotide_variant"}
            """.trimIndent()
            assertEquals(expectedJson, responseBody)
        }
    }

    @Ignore("Не пройдет при сборке для Docker")
    @Test
    fun testInfoRouteAnnotationNotFound() = testApplication {
        client.get("/info?rac=NC_000002.12&lap=32136583&rap=32136584&refKey=C&file=testfiles\\clinvar_20220430.aka.gz").apply {
            assertEquals(HttpStatusCode.NotFound, status)
            assertTrue(bodyAsText().contains("404 Not Found: Для введенного запроса аннотаций не найдено"))
        }
    }

    @Test
    fun testInfoRouteInvalidParams() = testApplication {
        client.get("/info?rac=NC_000001.11&lap=abc&rap=925953&refKey=A&file=testfile").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            assertTrue(bodyAsText().contains("400 Bad Request: "))
        }
    }
}
