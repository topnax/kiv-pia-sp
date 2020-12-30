import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.impl.JWTParser
import com.zcu.kiv.pia.tictactoe.authentication.JwtConfig
import com.zcu.kiv.pia.tictactoe.model.User
import com.zcu.kiv.pia.tictactoe.service.SimpleMessageParser
import io.ktor.auth.jwt.*
import org.junit.Test
import kotlin.test.assertEquals


class SimpleMessageParserTest {
    @Test
    fun `should parse a simple message`() {
        val parser = SimpleMessageParser()
        val parsed = parser.parse("jwt;token")
        assert(parsed != null)
        assertEquals("jwt", parsed?.first)
        assertEquals("token", parsed?.second)
    }

   @Test
   fun `wip parsing of a JWT token`() {
       val domain = "localhost"
       val secret = "notsosecret"
       val audience = "tictactoe-audience"
       val realm = "kiv pia SP - tictactoe "
       val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJBdXRoZW50aWNhdGlvbiIsImlzcyI6ImxvY2FsaG9zdCIsImlkIjoyLCJleHAiOjE2MDkzNzAwMTgsImVtYWlsIjoiZm9vMkBiYXIuY3oiLCJ1c2VybmFtZSI6ImZvbzIifQ.rFR4QSYmrDdFNHk-RllhUJta6guJKOU2zmQdSjGfjyibMczD-rM1NpuWCwYDBTy-bHmorTXl9ZDtOp9pClEN-w"
       val jvtConfig = JwtConfig(domain, secret, 10 * 60)
       val payload = jvtConfig.verifier.verify(token)

       val user = User.fromJWTToken(JWTPrincipal(payload))
       assertEquals("foo2@bar.cz", user.email)
       assertEquals("foo2", user.username)
       assertEquals(2, user.id)
   }

    @Test
    fun `should not fail when parsing an invalid message`() {
        val parser = SimpleMessageParser()
        assert(parser.parse(";token") == null)
        assert(parser.parse(";") == null)
        assert(parser.parse("dasdasdasds;") == null)
        assert(parser.parse("dasdasdasds") == null)
    }
}