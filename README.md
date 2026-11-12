# ECE373FinalProject
Repository for the ECE 373 Final project

Added: September 28th, 2026

Team 8
Members:  
Ahmet Mert Degirmenci  
Corwyn Dominic Lail  
Kamil Frem Mendiola  
Xtanley J Quiambao  


## Build and test

Install JDK 17 or later and Maven. From the project root, where
`pom.xml` is located, run:

```sh
mvn clean test
```

## Line breaking

The presentation package uses the `LineBreaker` interface to wrap
paragraph text at a selected width. Each implementation returns a list
of lines.

- `GreedyLineBreaker` fits as many complete words as possible on each line.
- `BalancedLineBreaker` chooses breaks that minimize the sum of squared
  unused spaces across all lines, including the last line.

For `aaa bb cc ddddd` at width 10:

| Strategy | First line | Second line |
| --- | --- | --- |
| Greedy | `aaa bb cc` | `ddddd` |
| Balanced | `aaa bb` | `cc ddddd` |

Both strategies require a positive width and non-null text. Blank input
returns an empty list. Whitespace between words is normalized to one
space in the output. A word wider than the selected width raises an error.

The tests cover wrapping, boundary cases, and whitespace handling.
The capacity tests verify that both strategies preserve word order and
respect the selected width on a paragraph over 100,000 characters.
