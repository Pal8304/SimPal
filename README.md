# SimPal

#### Just a rudimentary interpreted language with a pun name

It is built with help of [Crafting Interpreters](https://www.craftinginterpreters.com). Frankly built following it completely.

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

- [x] Binary operations ( ``+, -, /, *``)
- [ ] Power operator
- [ ] Modulo operator
- [x] Unary operations (``-, ~``)
- [x] Print statement ( ``print``)
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
- [x] Single line comments ( ``//`` )
- [ ] Multi line comments (``/* */``)
- [ ] Print multiple values ( ``print a, b`` )
- [ ] Handle redundant brackets ( ``print (a + b)`` )

## SimPal Syntax

#### Variable Declaration

```
var a; // initialized to null 
var b = 1; // stores value 1 
var c = "abc"; // stores string "abc" 
```

### User Input/Output

```
var a = 1;
print a; // prints 1 on terminal 
```

### Operators

```
var a = 1;
var b = 2;

print a + b; // Addition
print a - b; // Subtraction 
print a * b; // Multiplication
print a / b; // Division
```

### Conditional Statements 
```
if ( 1 > 2 ) {
    print "Greater";
}
else {
    print "Lesser";
}
```

### For Loop 
```
for(var i = 0; i < 10; i = i + 1) {
    print i;
}
```
### While Loop
```
var i = 1;
while( i < 10 ) {
    print i;
    i = i + 1;
}
```

### User defined functions
```
fun fib(n) {
  if (n <= 1) return n;
  return fib(n - 2) + fib(n - 1);
}

for (var i = 0; i < 20; i = i + 1) {
  print fib(i);
}
```