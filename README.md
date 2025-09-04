# SimPal 
### Just a rudimentary interpreted basic 
### With a pun name

## Language Grammar

```
Initial Language Grammar:

expression     → literal
                | unary
                | binary
                | grouping ;

literal        → NUMBER | STRING | "true" | "false" | "nil" ;
grouping       → "(" expression ")" ;
unary          → ( "-" | "!" ) expression ;
binary         → expression operator expression ;
operator       → "==" | "!=" | "<" | "<=" | ">" | ">="
                | "+"  | "-"  | "*" | "/" ;
```

```
Language Grammar

program        → declaration* EOF ;

declaration    → funDeclaration
                |varDeclaration
               | statement ;

funDeclaration        → "fun" function ;

function       → IDENTIFIER "(" parameters? ")" block ;

varDeclaration        → "var" IDENTIFIER ( "=" expression )? ";" ;

statement      → completeExpression
                | forStatement
               | ifStatement
               | printStatement
               | returnStatement
               | whileStatement
               | block ;

returnStatement     → "return" expression? ";" ;

whileStatement      → "while" "(" expression ")" statement ;

forStatement        → "for" "(" ( varDeclaration | completeExpression | ";" )
                 expression? ";"
                 expression? ")" statement ;

ifStatement         → "if" "(" expression ")" statement
               ( "else" statement )? ;

block          → "{" declaration* "}" ;

completeExpression       → expression ";" ;

expression     → assignment ;

assignment     → IDENTIFIER "=" assignment
               | logic_or ;

logic_or       → logic_and ( "or" logic_and )* ;

logic_and      → equality ( "and" equality )* ;

equality       → comparison ( ( "!=" | "==" ) comparison )* ;

comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;

term           → factor ( ( "-" | "+" ) factor )* ;

factor         → unary ( ( "/" | "*" ) unary )* ;

unary          → ( "!" | "-" ) unary | call ;

call           → primary ( "(" arguments? ")" )* ;

arguments      → expression ( "," expression )* ;

primary        → "true" | "false" | "nil"
               | NUMBER | STRING
               | "(" expression ")"
               | IDENTIFIER ;

```
