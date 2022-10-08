import org.antlr.v4.kotlinruntime.CharStream
import org.antlr.v4.kotlinruntime.CharStreams
import kotlin.test.Test
import kotlin.test.assertEquals

class OperationTests {

    fun make_charstream(input: String): CharStream {
        return CharStreams.fromString(input)
    }

    fun assert_regs(final_listener: Listener, expected_regs: Array<Int>) {
        for (i in 0..63) {
            assertEquals(expected_regs[i], final_listener.regs[i])
        }
    }

    @Test
    fun test_add() {
        val expected_regs: Array<Int> = Array(64) { 0 }
        expected_regs[0] = 1
        expected_regs[1] = 2
        expected_regs[2] = 3
        assert_regs(runScript(
            make_charstream(
                "yowlyowl:yowlyowl::::::yowl:" +
                "yowlyowl:yowlyowl:::yowl:::yowlyowl:" +
                "yowlyowl:yowlyowl:::yowlyowl:::yowlyowlyowl:" +
                "yowl:yowl:"
        ), true), expected_regs)
    }

    @Test
    fun test_sub() {
        val expected_regs: Array<Int> = Array(64) { 0 }
        assert_regs(runScript(
            make_charstream(
                "yowlyowl:yowlyowl::::::yowl:" +
                "yowlyowl:yowlyowl:::yowl:::yowlyowl:" +
                "yowlyowl:yowlyowl:::yowlyowl:::yowlyowlyowl:" +
                "yowlyowl:yowlyowlyowl::::::yowl:" +
                "yowlyowl:yowlyowlyowl:::yowl:::yowlyowl:" +
                "yowlyowl:yowlyowlyowl:::yowlyowl:::yowlyowlyowl:" +
                "yowl:yowl:"
            ), true), expected_regs)
    }

    @Test
    fun test_add_reg() {
        val expected_regs: Array<Int> = Array(64) { 0 }
        expected_regs[0] = 1
        expected_regs[1] = 3
        assert_regs(runScript(
            make_charstream(
                "yowlyowl:yowlyowl::::::yowl:" +
                "yowlyowl:yowlyowl:::yowl:::yowlyowl:" +
                "yowlyowl:yowlyowl:::::::yowl:" +
                "yowl:yowl:"
            ), true), expected_regs)
    }

    @Test
    fun test_sub_reg() {
        val expected_regs: Array<Int> = Array(64) { 0 }
        expected_regs[0] = 1
        expected_regs[1] = 1
        assert_regs(runScript(
            make_charstream(
                "yowlyowl:yowlyowl::::::yowl:" +
                "yowlyowl:yowlyowl:::yowl:::yowlyowl:" +
                "yowlyowl:yowlyowlyowl::::yowl::::" +
                "yowl:yowl:"
            ), true), expected_regs)
    }

    @Test
    fun test_goto() {
        val expected_regs: Array<Int> = Array(64) { 0 }
        expected_regs[0] = 0
        expected_regs[1] = 10
        assert_regs(runScript(
            make_charstream(
                "yowlyowl:yowlyowl::::::yowlyowlyowlyowlyowlyowlyowlyowlyowlyowl:" +  // add 10 to reg 0
                "yowlyowl:yowlyowl:::yowl:::yowl:" + // add 1 to reg 1
                "yowlyowl:yowlyowlyowl::::::yowl:" + // subtract 1 from reg 0
                "yowl:yowlyowl:::yowlyowlyowlyowlyowl::::" + // goto line 6 if reg 0 is 0
                "yowl:yowlyowlyowl:::yowl:" + // goto line 2
                "yowl:yowl:" // exit
            ), true), expected_regs)
    }
}