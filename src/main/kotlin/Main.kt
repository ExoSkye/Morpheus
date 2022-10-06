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

class Listener(val debug: Boolean) : morpheusBaseListener() {
    var regs = Array(64) { 0 }
    var statement = 0


    fun debugPrint(message: String) {
        if (debug) {
            println(message)
        }
    }
    fun debugPrint(optype: String, reg_a: Int, reg_b: Int) {
        debugPrint("Running ${optype} with registers ${reg_a} (value = ${regs[reg_a]}) and ${reg_b} (value = ${regs[reg_b]})")
    }

    fun debugPrint(optype: String, reg: Int) {
        debugPrint("Running ${optype} with the register ${reg} (value = ${regs[reg]})")
    }

    fun debugPrint(reg: Int, imm: Int, optype: String) {
        debugPrint("Running ${optype} with the register ${reg} (value = ${regs[reg]}) and immediate value ${imm}")
    }
    override fun enterInst(ctx: morpheusParser.InstContext?) {
        if (debug) {
            statement += 1
        }
    }
    override fun enterPrint(ctx: morpheusParser.PrintContext) {
        val reg_num = parse_number(ctx.getChild(2))
        debugPrint("print", reg_num)
        println(regs[reg_num])
    }

    override fun enterAdd(ctx: morpheusParser.AddContext) {
        val reg_num = parse_number(ctx.getChild(3))
        val imm_val = parse_number(ctx.getChild(5))
        debugPrint("add", reg_num, imm_val)
        regs[reg_num] += imm_val
    }

    override fun enterReset_reg(ctx: morpheusParser.Reset_regContext) {
        regs[parse_number(ctx.getChild(3))] = 0
    }

    override fun enterSub(ctx: morpheusParser.SubContext) {
        regs[parse_number(ctx.getChild(4))] -= parse_number(ctx.getChild(6))
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

    override fun enterCopy_reg(ctx: morpheusParser.Copy_regContext) {
        regs[parse_number(ctx.getChild(5))] = regs[parse_number(ctx.getChild(7))]
    }
}

fun main(args: Array<String>) {
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
    val listener = Listener((System.getenv("DEBUG") ?: "0") == "1")
    val entrypoint = parser.morpheus_script()
    val walker = ParseTreeWalker()
    walker.walk(listener, entrypoint)
}