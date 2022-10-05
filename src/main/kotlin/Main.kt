import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeWalker


fun wait_for_input(): String {
    var value: String?
    while (true) {
        value = readlnOrNull()

        if (value != null) {
            return value
        }
    }
}

fun parse_number(num: ParseTree): Int {
    return num.getChild(2).childCount
}

class Listener : morpheusBaseListener() {
    var regs = Array(64) { 0 }
    override fun enterPrint(ctx: morpheusParser.PrintContext) {
        println(regs[parse_number(ctx.getChild(2))])
    }

    override fun enterAdd(ctx: morpheusParser.AddContext) {
        regs[parse_number(ctx.getChild(3))] += parse_number(ctx.getChild(5))
    }

    override fun enterReset_reg(ctx: morpheusParser.Reset_regContext) {
        regs[parse_number(ctx.getChild(3))] = 0
    }

    override fun enterSub(ctx: morpheusParser.SubContext) {
        regs[parse_number(ctx.getChild(3))] -= parse_number(ctx.getChild(5))
    }

    override fun enterGoto_uncond(ctx: morpheusParser.Goto_uncondContext) {
        val walker = ParseTreeWalker()
        walker.walk(this, ctx.parent.parent.parent.getChild(parse_number(ctx.getChild(3)) - 1))
    }

    override fun enterGoto_if_zero(ctx: morpheusParser.Goto_if_zeroContext) {
        if (regs[parse_number(ctx.getChild(5))] == 0) {
            val walker = ParseTreeWalker()
            walker.walk(this, ctx.parent.parent.parent.getChild(parse_number(ctx.getChild(3)) - 1))
        }
    }

    override fun enterRead(ctx: morpheusParser.ReadContext) {
        print("> ")
        regs[parse_number(ctx.getChild(3))] = wait_for_input().toInt()
    }

    override fun enterExit(ctx: morpheusParser.ExitContext?) {
        throw Exception("Exiting...")
    }
}

fun main() {
    val filename = System.getenv("FILENAME") ?: "failed"
    if (filename == "failed") {
        println("Please define file to run via the FILENAME env variable")
        return
    }

    val file = CharStreams.fromFileName(filename)

    val lexer = morpheusLexer(file)
    val tokens = CommonTokenStream(lexer)
    val parser = morpheusParser(tokens)
    parser.buildParseTree = true
    val listener = Listener()
    val entrypoint = parser.morpheus_script()
    val walker = ParseTreeWalker()
    walker.walk(listener, entrypoint)
}