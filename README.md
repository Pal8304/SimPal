# SimPal

#### Just a rudimentary interpreted language with a pun name

It is built with help of [Crafting Interpreters](https://www.craftinginterpreters.com). Frankly built following it completely.

## How to execute SimPal Code

- Make sure you have [Java](https://www.java.com/en/download/manual.jsp) installed 
- Using jar file: 
  - Download the ``SimPal.jar`` file from [release](https://github.com/Pal8304/SimPal/releases)
  - Run this command: 
    - For terminal execution: `` java -cp simpal.jar src/simpal/SimPal.java``
    - For execution from a input text file   ``java -cp simpal.jar src/simpal/SimPal.java <path of input text file>``
    - For output in text file as well ``java -cp simpal.jar src/simpal/SimPal.java <path of input text file> <path of output text file`` ( this is yet to released in jar file )
- Using git project clone/fork:
  - Clone  ``` git clone https://github.com/Pal8304/SimPal.git ``` 
  - Go to SimPal directory `` cd src/simpal``
  - Run the following commands:
    - For execution in terminal: ``java SimPal.java``
    - For execution from input file: ``java SimPal.java <input text file>``
    - For output of code in text file: ``java SimPal.java <input text file> <output text file>``

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
- [x] Modulo operator (``%``)
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
- [x] Handle redundant brackets ( ``(((a + b)))`` )
- [ ] Math Functions like absolute, sin, cos, etc
- [x] Output file support like we have for input 

## SimPal Syntax

### Keywords

- **true** : Truthy boolean
- **false**: Falsey boolean   
- **print**: Prints output 
- **or** : Short-circuited boolean `or` operator 
- **and**: Short-circuited boolean `and` operator
- **nil**: Null value 
- **var**: Declares variable
- **if**: Conditional `if` statement 
- **else**: Conditional `else` statement
- **for**: Looping `for` statement
- **while**: Looping `while` statement
- **fun**: Declaration of functions 

### Sample code

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
print a % b; // Modulus 
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

