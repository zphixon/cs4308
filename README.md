# CS4308 Programming Language Concepts

This is the Spring 2021 CS4308 Section 1 group project of William Ajagba, Hunter Reece, and Zack Hixon.

## Design decisions

We can't really check for unknown identifiers because variable scope is non-lexical. For example, in `example4.bas` the
variable `HEX$` is defined before it's used from the standpoint of the interpreter, but it's used before it's defined
from the parser's point of view. Until the halting problem gets solved, we can't really do anything about that.