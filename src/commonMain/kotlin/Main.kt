import org.antlr.v4.kotlinruntime.CharStream
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.tree.ParseTree
import org.antlr.v4.kotlinruntime.tree.ParseTreeWalker

expect interface Args {
    open fun getSource(): String
    open fun getDebug(): Boolean
}

fun waitForInput(): String {
    var value: String?
    while (true) {
        value = readlnOrNull()

        if (value != null) {
            return value
        }
    }
}

fun parseNumber(num: ParseTree): Int {
    return num.getChild(2)!!.childCount
}

class Listener(private val debug: Boolean) : morpheusBaseListener() {
    var regs = Array(64) { 0 }
    var statement = 0

    var statementCount = 0

    private fun debugPrint(message: String) {
        if (debug) {
            println(message)
        }
    }
    private fun debugPrint(optype: String, reg_a: Int, reg_b: Int) {
        debugPrint("Running $optype with registers $reg_a (value = ${regs[reg_a]}) and $reg_b (value = ${regs[reg_b]})")
    }

    private fun debugPrint(optype: String, reg: Int) {
        debugPrint("Running $optype with the register $reg (value = ${regs[reg]})")
    }

    private fun debugPrint(imm: Int, optype: String) {
        debugPrint("Running $optype with the immediate value $imm")
    }

    private fun debugPrint(reg: Int, imm: Int, optype: String) {
        debugPrint("Running $optype with the register $reg (value = ${regs[reg]}) and immediate value $imm")
    }

    override fun enterInst(ctx: morpheusParser.InstContext) {
        statement += 1
        statementCount += 1

        if (statementCount % 100000 == 0) {
            println("Statement $statementCount")
        }
    }
    override fun enterPrint(ctx: morpheusParser.PrintContext) {
        val regNum = parseNumber(ctx.getChild(2)!!)
        debugPrint("print", regNum)
        println(regs[regNum])
    }

    override fun enterAdd(ctx: morpheusParser.AddContext) {
        val regNum = parseNumber(ctx.getChild(3)!!)
        val immVal = parseNumber(ctx.getChild(5)!!)
        debugPrint(regNum, immVal, "add")
        regs[regNum] += immVal
    }

    override fun enterAdd_reg(ctx: morpheusParser.Add_regContext) {
        val regANum = parseNumber(ctx.getChild(6)!!)
        val regBNum = parseNumber(ctx.getChild(4)!!)
        debugPrint("add register", regANum, regBNum)
        regs[regANum] += regs[regBNum]
    }

    override fun enterSub_reg(ctx: morpheusParser.Sub_regContext) {
        val regANum = parseNumber(ctx.getChild(5)!!)
        val regBNum = parseNumber(ctx.getChild(7)!!)
        debugPrint("subtract register", regANum, regBNum)
        regs[regANum] -= regs[regBNum]
    }

    override fun enterReset_reg(ctx: morpheusParser.Reset_regContext) {
        val regNum = parseNumber(ctx.getChild(2)!!)
        debugPrint("reset register", regNum)
        regs[regNum] = 0
    }

    override fun enterSub(ctx: morpheusParser.SubContext) {
        val regNum = parseNumber(ctx.getChild(4)!!)
        val immVal = parseNumber(ctx.getChild(6)!!)
        debugPrint(regNum, immVal, "subtract")
        regs[regNum] -= immVal
    }

    override fun enterGoto_uncond(ctx: morpheusParser.Goto_uncondContext) {
        debugPrint(parseNumber(ctx.getChild(4)!!), "goto")
        statement = parseNumber(ctx.getChild(4)!!)
    }

    override fun enterGoto_if_zero(ctx: morpheusParser.Goto_if_zeroContext) {
        val regNum = parseNumber(ctx.getChild(5)!!)
        val newStatement = parseNumber(ctx.getChild(3)!!)
        debugPrint(regNum, newStatement, "conditional goto")
        if (regs[regNum] == 0) {
            statement = newStatement
        }
    }

    override fun enterRead(ctx: morpheusParser.ReadContext) {
        val reg = parseNumber(ctx.getChild(3)!!)
        debugPrint("read", reg)
        print("> ")
        regs[reg] = waitForInput().toInt()
    }

    override fun enterExit(ctx: morpheusParser.ExitContext) {
        debugPrint("Exiting")
        statement = -1
    }

    override fun enterCopy_reg(ctx: morpheusParser.Copy_regContext) {
        val regANum = parseNumber(ctx.getChild(5)!!)
        val regBNum = parseNumber(ctx.getChild(7)!!)
        debugPrint("copy register", regBNum, regANum)
        regs[regANum] = regs[regBNum]
    }

    override fun enterPrint_char(ctx: morpheusParser.Print_charContext) {
        val reg = parseNumber(ctx.getChild(3)!!)
        debugPrint("print char", reg)
        print(regs[reg].toChar())
    }

    override fun exitRead_char(ctx: morpheusParser.Read_charContext) {
        val reg = parseNumber(ctx.getChild(3)!!)
        debugPrint("read char", reg)
        print("> ")
        regs[reg] = waitForInput().first().code
    }
}

fun runScript(script: CharStream, debug: Boolean): Listener {
    val lexer = morpheusLexer(script)
    val tokens = CommonTokenStream(lexer)
    val parser = morpheusParser(tokens)
    parser.buildParseTree = true

    val listener = Listener(debug)
    val entrypoint = parser.morpheus_script()
    while (listener.statement != -1) {
        val node = entrypoint.getChild(listener.statement)
        val walker = ParseTreeWalker()
        walker.walk(listener, node!!)
    }

    println("Executed ${listener.statementCount} statements")

    return listener
}

class ArgsImpl: Args {}

fun main() {
    val args = ArgsImpl()
    val file = CharStreams.fromString(args.getSource())

    runScript(file, args.getDebug())
}