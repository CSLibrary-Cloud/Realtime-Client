import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import java.util.*

data class LoginRequest(
    var userId: String = "",
    var userPassword: String = ""
)

data class LoginResponse(
    var userToken: String = ""
)

data class SeatSelectRequest(
    var seatNumber: Int
)

fun main() {
    val scanner: Scanner = Scanner(System.`in`)
    val baseUrl: String = "http://localhost:8080"
    val mockLoginRequest: LoginRequest = LoginRequest(
        userId = "kangdroid",
        userPassword = "testtest"
    )
    runBlocking {
        val client: HttpClient = HttpClient {
            install(WebSockets)
            install(JsonFeature) {
                serializer = JacksonSerializer()
            }
        }

        // Login Request
        val loginResponse: LoginResponse = client.post<LoginResponse>("${baseUrl}/api/v1/login") {
            contentType(ContentType.Application.Json)
            body = mockLoginRequest
        }

        // Reserve Seat
        client.post<String>("${baseUrl}/api/v1/seat") {
            contentType(ContentType.Application.Json)
            header("X-AUTH-TOKEN", loginResponse.userToken)
            body = SeatSelectRequest(5)
        }

        client.ws(
            method = HttpMethod.Get,
            host = "127.0.0.1",
            port = 8081,
            path = "/realtime/endpoint"
        ) {
            // Send Text Frame
            send(Frame.Text(loginResponse.userToken))

            // Receive frame. - Other Thread[Keep UI Updated]
            val job: Job = GlobalScope.launch {
                while (isActive) {
                    val frame = incoming.receive()
                    when (frame) {
                        is Frame.Text -> println(frame.readText())
                    }
                }
            }

            // Send Message to Server
            var inputString: String = ""
            while (true) {
                println("Input Command")
                inputString = scanner.nextLine()

                send(Frame.Text(inputString))
                if (inputString == "close") {
                    job.cancelAndJoin()
                    break
                }
            }
        }
    }

}