# SimPal 
#### Just a rudimentary interpreted language with a pun name

## Language Grammar

```
Language Grammar

program        → declaration* EOF ;

declaration    → funDeclaration
               |varDeclaration
               | statement ;

funDeclaration → "fun" function ;

function       → IDENTIFIER "(" parameters? ")" block ;

varDeclaration → "var" IDENTIFIER ( "=" expression )? ";" ;

statement      → completeExpression
               | forStatement
               | ifStatement
               | printStatement
               | returnStatement
               | whileStatement
               | block ;

returnStatement → "return" expression? ";" ;

whileStatement  → "while" "(" expression ")" statement ;

forStatement    → "for" "(" ( varDeclaration | completeExpression | ";" )
                 expression? ";"
                 expression? ")" statement ;

ifStatement     → "if" "(" expression ")" statement
               ( "else" statement )? ;

block           → "{" declaration* "}" ;

completeExpression → expression ";" ;

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
Currently, it supports: 

- [x] Binary operations ( +, -, /, *) 
- [ ] Power operator 
- [ ] Modulo operator 
- [x] Unary operations ( -, ~)
- [x] Print statement 
- [ ] User input 
- [x] Variable declaration
- [x] Conditional Statements ( if, else ) 
- [x] Boolean operations ( and, or )
- [x] Looping statements ( for loop, while loops )
- [ ] Break and continue statements 
- [x] User defined functions 
- [ ] User defined classes and objects
- [ ] Built-in functions 
- [ ] Import support 
- [ ] Mutable variables
- [ ] Report unused local variables