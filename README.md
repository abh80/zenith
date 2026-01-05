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
g is integer = 10 but constant

# Mutable variable (type inferred or generic)
speed = 100

# Explicit mutable
force = 50 but mutable
```

### types

- `integer`: Whole numbers.
- `string`: Text.

### Literals

- **Integers**: Standard numeric literals (e.g., `42`).
- **Strings (Quoted)**: Standard string literals (e.g., `"Hello"`).
- **Strings (Unquoted)**: A sequence of identifiers is automatically treated as a space-separated string.
    ```text
    greeting = Welcome to the simulation  # Becomes string "Welcome to the simulation"
    ```

## License

Copyright (C) 2025 abh80.
This project is licensed under the GNU General Public License v3.0. See [LICENSE](LICENSE) for details.