# Zenith

**Zenith** is a programming language (and transpiler) designed for "physics-oriented development". It currently targets JavaScript as its output language.

The goal of Zenith is to provide a highly readable, prose-like syntax for defining constants and variables, making it well-suited for configuration, descriptions, or educational purposes.

## Features

- **Transpilation to JavaScript**: Compiles Zenith code directly to executable JavaScript.
- **Readable Syntax**: Focuses on natural language constructs.
- **Physics-Oriented**: (Conceptual) Designed with physics definitions and constants in mind.
- **Identifier String Literals**: Unquoted sequences of identifiers are treated as strings, allowing for clean text definitions.

## Getting Started

### Prerequisites

- [Scala 3](https://www.scala-lang.org/) (specifically 3.3.6)
- [sbt](https://www.scala-sbt.org/) (Scala Build Tool)

### Building

Clone the repository and build the project using sbt:

```bash
git clone <repository-url>
cd zenith
sbt assembly
```

This will produce an executable JAR file in `target/scala-3.3.6/zenith.jar`.

## Usage

You can run the Zenith compiler using the generated JAR file:

```bash
java -jar target/scala-3.3.6/zenith.jar [options] <input-file>
```

### CLI Options

| Option | Description |
| :--- | :--- |
| `<input-file>` | The source file to compile. |
| `-o <file>`, `--output <file>` | Specify the output file path. |
| `--verbose` | Enable verbose logging. |
| `--show-ast` | Print the Abstract Syntax Tree (AST) to the console. |
| `--show-analysis` | Print the semantic analysis results to the console. |
| `--target <lang>` | Set the target language (default: `js`). |

### Example

Create a file named `physics.zenith`:

```text
gravity is integer = 10 but constant
message = Hello World
velocity = 25 but mutable
```

Run the compiler:

```bash
java -jar target/scala-3.3.6/zenith.jar physics.zenith -o physics.js
```

The output `physics.js` will contain the equivalent JavaScript code.

## Language Syntax

### Variable Declarations

Zenith uses a declarative syntax for variables and constants.

**Format:**
```text
<identifier> [is <type>] = <value> [but constant|mutable]
```

- **Default Mutability**: Variables are mutable by default if `but constant` is not specified.
- **Types**: Optional explicit type definition (`integer`, `string`).

**Examples:**

```text
# Constant integer
g = 10 but constant

# Mutable variable (type inferred)
speed = 100

# Explicit mutable
force = 50 but mutable

# Explicit float
pi is float = 3.14159
val is decimal = 10.5
```

### Print Statements

Print statements provide a declarative way to output values during simulations, ideal for debugging and logging physics calculations.

**Syntax:**
```text
print <expression>
```

**String Interpolation:**

Zenith supports string interpolation in print statements, allowing you to embed variables and expressions directly within strings:

- **Simple variable injection**: Use `$variable` to inject a variable's value
- **Expression injection**: Use `$(expression)` to inject the result of an expression

**Examples:**

```text
velocity = 100
altitude = 1000

# Simple string interpolation
print "Velocity is $velocity"
print "Altitude is $altitude"

# Expression interpolation
print "Sum is $(velocity plus altitude)"
print "Double velocity: $(velocity times 2)"

# Traditional separate prints (still supported)
print "Velocity is"
print velocity

print "Altitude is"
print altitude

# Print expressions
force = mass times g
print force
```

**Generated JavaScript:**
```javascript
// String interpolation uses template literals
console.log(`Velocity is ${velocity}`);
console.log(`Sum is ${(velocity + altitude)}`);

// Traditional prints
console.log("Velocity is");
console.log(velocity);
```

### Arithmetic Operators

Zenith supports verbose, readable arithmetic operators that make physics equations more intuitive.

**Binary Operators:**

| Operator | Syntax | Example | JavaScript |
|----------|--------|---------|------------|
| Addition | `plus`, `added to` | `a plus b`, `a added to b` | `a + b` |
| Subtraction | `minus`, `less` | `a minus b`, `a less b` | `a - b` |
| Multiplication | `times`, `multiplied by` | `a times b`, `a multiplied by b` | `a * b` |
| Division | `divided by`, `over` | `a divided by b`, `a over b` | `a / b` |
| Floor Division | `floor divided by` | `a floor divided by b` | `Math.floor(a / b)` |
| Modulo | `modulo` | `a modulo b` | `a % b` |
| Power | `to the power`, `raised to` | `a raised to b` | `a ** b` |
| Squared | `squared` | `a squared` | `a ** 2` |

**Unary Operators:**

| Operator | Syntax | Example | JavaScript |
|----------|--------|---------|------------|
| Negation | `negative`, `minus` | `negative a` | `-a` |
| Square Root | `square root of` | `square root of a` | `Math.sqrt(a)` |

**Examples:**

```text
# Physics calculations with readable operators
mass = 10
velocity = 20

# Kinetic energy: KE = (m * v²) / 2
kinetic_energy = mass times velocity squared divided by 2

# Force calculation
acceleration = 5
force = mass times acceleration

# Complex expressions with precedence
result = 2 added to 3 times 4  # Result: 14 (3*4 + 2)
result2 = (2 added to 3) times 4  # Result: 20

# Floor division for integer results
quotient = 10 floor divided by 3  # Result: 3

# Modulo operation
remainder = 10 modulo 3  # Result: 1

# Square root
distance = square root of 64  # Result: 8

# Negative values
temp = negative 5  # Result: -5
```

### Types

- `integer`: Whole numbers.
- `float` (or `decimal`): Floating-point numbers.
- `string`: Text.

### Literals

- **Integers**: Standard numeric literals (e.g., `42`).
- **Floats**: Decimal literals (e.g., `3.14`, `0.001`).
- **Strings (Quoted)**: Standard string literals (e.g., `"Hello"`).
- **Strings (Unquoted)**: A sequence of identifiers is automatically treated as a space-separated string.
    ```text
    greeting = Welcome to the simulation  # Becomes string "Welcome to the simulation"
    ```

### Verbose Arithmetic Operators

Zenith supports prose-like arithmetic operators that read like physics equations, using words instead of symbols for enhanced clarity.

**Basic Operations:**

- **Addition**: `plus`, `added to`
  ```text
  total = force plus friction
  result = 5 added to 3
  
  # String Concatenation
  greeting = "Hello" plus " World"
  msg = "Value is " added to 42
  ```

- **Subtraction**: `minus`, `less`
  ```text
  net_force = thrust minus drag
  difference = 10 less 2
  ```

- **Multiplication**: `times`, `multiplied by`
  ```text
  area = width times height
  product = mass multiplied by acceleration
  ```

- **Division**: `divided by`, `over`
  ```text
  speed = distance divided by time
  ratio = numerator over denominator
  ```

- **Modulus**: `modulo`
  ```text
  remainder = value modulo 360
  ```

- **Power**: `to the power`, `raised to`, `squared`
  ```text
  area = radius to the power 2
  volume = side raised to 3
  kinetic = mass times velocity squared
  ```

**Advanced Operations:**

- **Floor Division**: `floor divided by` (returns integer result)
  ```text
  quotient = 10 floor divided by 3  # Results in Math.floor(10 / 3)
  ```

- **Square Root**: `square root of` (unary prefix)
  ```text
  distance = square root of sum
  ```

- **Negation**: `negative`, `minus` (unary prefix)
  ```text
  opposite = negative velocity
  inverted = minus 5
  ```

**Precedence and Grouping:**

Operators follow standard mathematical precedence: Power > Multiplication/Division > Addition/Subtraction. Use parentheses for explicit grouping:

```text
# Precedence example
result = 2 added to 3 times 4        # Evaluates as 2 + (3 * 4) = 14

# Grouping with parentheses
result = (2 added to 3) times 4      # Evaluates as (2 + 3) * 4 = 20

# Complex physics equation
kinetic_energy = mass times velocity squared divided by 2
period = two times pi times square root of (length over gravity)
```

### Control Flow

Zenith provides two distinct syntaxes for conditional logic: a standard "programmatic" style and a "physics-flavored" style.

#### Standard Style (If-Then-Else)
Use `if ... then do ... end` blocks for standard conditional logic.

```text
if velocity greater 10 then do
  print "Slow down"
end if
else do
  print "Cruising"
end else
```

#### Physics Style (When-Holds-Otherwise)
Use `when ... holds do ... end` blocks for a more declarative, physics-oriented feel.

```text
when mass greater 0 holds do
  force = gravity times mass
end when
otherwise do
  force = 0
end otherwise
```

**Comparison Operators:**

| Operator | Syntax | JavaScript |
|----------|--------|------------|
| Greater Than | `greater` | `>` |
| Less Than | `less` | `<` |
| Equal To | `is` (context dependent) or `equal` (future) | `===` |

*Note: Currently `greater` is the primary exposed comparison operator.*

### Complete Example

```text
# Physics simulation with print statements
g = 10 but constant
mass = 50
velocity = 100

# Calculate kinetic energy
kinetic = mass times velocity squared divided by 2

# Print results
print "Physics Simulation"
print "Mass:"
print mass
print "Velocity:"
print velocity
print "Kinetic Energy:"
print kinetic

# Force calculation
acceleration = 5
force = mass times acceleration
print "Force (F = ma):"
print force
```

## License

Copyright (C) 2025 abh80.
This project is licensed under the GNU General Public License v3.0. See [LICENSE](LICENSE) for details.