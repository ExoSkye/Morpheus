# Morpheus

Morpheus is an esolang that is based on my cat (called Morpheus) who's loud as fuck (his yowls could be used as a bloody foghorn). 
Hence, why the only two symbols in this language are `yowl` and `:` (I needed a separator).

## Specification

### Commands

Commands are structured as follows:
`prefix:command:arguments:`

| Prefix         | Command            | Argument format                         | Description                                                                              |
|----------------|--------------------|-----------------------------------------|------------------------------------------------------------------------------------------|
| `yowl`         |                    |                                         | Control commands                                                                         |
|                | `yowl`             | None                                    | Exits the program                                                                        |
|                | `yowlyowl`         | `:<statement number>:<register number>` | Goes to a statement (if the specified register is equal to 0)                            |
|                | `yowlyowlyowl`     | `:<statement number>`                   | Goes to a statement (unconditional)                                                      |
| `yowlyowl`     |                    |                                         | Maths commands                                                                           |
|                | `yowl`             | `:<register number>`                    | Resets the specified register                                                            |
|                | `yowlyowl`         | `:<register number>:<immediate value>`  | Adds the immediate value to the specified register, stores result in the register        |
|                | `yowlyowlyowl`     | `:<register number>:<immediate value>`  | Subtracts the immediate value from the specified register, stores result in the register |
|                | `yowlyowl`         | `::<register number>:<register number>` | Adds the two registers together, stores the result in the first register                 |
|                | `yowlyowlyowl`     | `::<register number>:<register number>` | Subtracts the second register from the first register, stores in the first register      |
|                | `yowlyowlyowlyowl` | `:<register number>:<register number>`  | Moves the value from the second register into the first                                  |
| `yowlyowlyowl` |                    |                                         | I/O commands                                                                             |
|                | `yowl`             | `:<register number>`                    | Prints the value of the specified register                                               |
|                | `yowlyowl`         | `:<register number>`                    | Reads a number from stdin and stores it in the specified register                        |
|                | `yowl`             | `::<register number>`                   | Prints the ASCII character of the value of the specified register                        |
|                | `yowlyowl`         | `::<register number>`                   | Reads a character from stdin and stores it in the specified register                     |

### Registers

There are 64 registers, numbered from 0 to 63. They are all initialised to 0.

### Number format

In code, numbers are represented by two colons and then a number of `yowl`s. For example, `::yowlyowlyowl` is 3, `::yowl` in 1 and `::` is 0.

### Statements

Statements are numbered from 0 to *n*-1, where *n-1* is the number of statements in the program. The first statement is always statement 0.

### Comments

Comments are can go between tokens (`yowl` or `:`) and are delimited by `//` on each side. For example, `yowlyowlyowl// comment in here //:yowlyowlyowl` is a comment. 

### Examples

#### Sample command

```
yowlyowlyowl:yowlyowl:::::yowlyowlyowlyowl:
```

This command reads a character from stdin and stores its ASCII value in register 3.

