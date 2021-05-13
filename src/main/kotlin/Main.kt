import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val client: HttpClient = HttpClient {
            install(WebSockets)
        }

        client.ws(
            method = HttpMethod.Get,
            host = "127.0.0.1",
            port = 8080,
            path = "/ws/chat"
        ) {
//            send(Frame.Text("Hello, World!"))

            // Receive frame.
            while (true) {
                val frame = incoming.receive()
                when (frame) {
                    is Frame.Text -> println(frame.readText())
                    is Frame.Binary -> println(frame.readBytes())
                }
            }
        }
    }

}