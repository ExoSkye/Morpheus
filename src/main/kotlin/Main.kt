import org.antlr.v4.runtime.CharStream
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

    fun debugPrint(imm: Int, optype: String) {
        debugPrint("Running ${optype} with the immediate value ${imm}")
    }

    fun debugPrint(reg: Int, imm: Int, optype: String) {
        debugPrint("Running ${optype} with the register ${reg} (value = ${regs[reg]}) and immediate value ${imm}")
    }

    override fun enterInst(ctx: morpheusParser.InstContext?) {
        statement += 1
    }
    override fun enterPrint(ctx: morpheusParser.PrintContext) {
        val reg_num = parse_number(ctx.getChild(2))
        debugPrint("print", reg_num)
        println(regs[reg_num])
    }

    override fun enterAdd(ctx: morpheusParser.AddContext) {
        val reg_num = parse_number(ctx.getChild(3))
        val imm_val = parse_number(ctx.getChild(5))
        debugPrint(reg_num, imm_val, "add")
        regs[reg_num] += imm_val
    }

    override fun enterAdd_reg(ctx: morpheusParser.Add_regContext) {
        val reg_a_num = parse_number(ctx.getChild(6))
        val reg_b_num = parse_number(ctx.getChild(4))
        debugPrint("add register", reg_a_num, reg_b_num)
        regs[reg_a_num] += regs[reg_b_num]
    }

    override fun enterSub_reg(ctx: morpheusParser.Sub_regContext) {
        val reg_a_num = parse_number(ctx.getChild(5))
        val reg_b_num = parse_number(ctx.getChild(7))
        debugPrint("subtract register", reg_a_num, reg_b_num)
        regs[reg_a_num] -= regs[reg_b_num]
    }

    override fun enterReset_reg(ctx: morpheusParser.Reset_regContext) {
        debugPrint("reset register", parse_number(ctx.getChild(3)))
        regs[parse_number(ctx.getChild(3))] = 0
    }

    override fun enterSub(ctx: morpheusParser.SubContext) {
        val reg_num = parse_number(ctx.getChild(4))
        val imm_val = parse_number(ctx.getChild(6))
        debugPrint(reg_num, imm_val, "subtract")
        regs[reg_num] -= imm_val
    }

    override fun enterGoto_uncond(ctx: morpheusParser.Goto_uncondContext) {
        debugPrint(parse_number(ctx.getChild(4)), "goto")
        statement = parse_number(ctx.getChild(4))
    }

    override fun enterGoto_if_zero(ctx: morpheusParser.Goto_if_zeroContext) {
        debugPrint(parse_number(ctx.getChild(5)), parse_number(ctx.getChild(3)), "conditional goto")
        if (regs[parse_number(ctx.getChild(5))] == 0) {
            statement = parse_number(ctx.getChild(3))
        }
    }

    override fun enterRead(ctx: morpheusParser.ReadContext) {
        debugPrint("read", parse_number(ctx.getChild(3)))
        print("> ")
        regs[parse_number(ctx.getChild(3))] = wait_for_input().toInt()
    }

    override fun enterExit(ctx: morpheusParser.ExitContext?) {
        debugPrint("Exiting")
        statement = -1
    }

    override fun enterCopy_reg(ctx: morpheusParser.Copy_regContext) {
        debugPrint("copy register", parse_number(ctx.getChild(7)), parse_number(ctx.getChild(5)))
        regs[parse_number(ctx.getChild(5))] = regs[parse_number(ctx.getChild(7))]
    }

    override fun enterPrint_char(ctx: morpheusParser.Print_charContext) {
        debugPrint("print char", parse_number(ctx.getChild(3)))
        print(regs[parse_number(ctx.getChild(3))].toChar())
    }

    override fun exitRead_char(ctx: morpheusParser.Read_charContext) {
        debugPrint("read char", parse_number(ctx.getChild(3)))
        print("> ")
        regs[parse_number(ctx.getChild(3))] = wait_for_input().first().code
    }
}

fun run_script(script: CharStream, debug: Boolean): Listener {
    val lexer = morpheusLexer(script)
    val tokens = CommonTokenStream(lexer)
    val parser = morpheusParser(tokens)
    parser.buildParseTree = true

    val listener = Listener(debug)
    val entrypoint = parser.morpheus_script()
    while (listener.statement != -1) {
        val node = entrypoint.getChild(listener.statement)
        val walker = ParseTreeWalker()
        walker.walk(listener, node)
    }

    return listener
}

fun main() {
    val filename = System.getenv("FILENAME") ?: "failed"
    if (filename == "failed") {
        println("Please define file to run via the FILENAME env variable")
        return
    }

    val file = CharStreams.fromFileName(filename)

    run_script(file, (System.getenv("DEBUG") ?: "0") == "1")
}