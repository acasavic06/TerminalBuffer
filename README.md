# TerminalBuffer Project

## Description

This project implements a **terminal text buffer**, the core data structure used by terminal emulators to store and manipulate displayed text. The buffer represents the terminal screen as a grid of character cells, each storing a character, foreground and background colors, and style flags (bold, italic, underline).

The buffer maintains a **cursor position** indicating where the next character will be written, and consists of two logical parts:

- **Screen**: the last N lines that fit the terminal dimensions (editable and visible to the user).
- **Scrollback**: lines that have scrolled off the screen, preserved for history. When the screen overflows, lines are moved to scrollback, respecting the configured maximum size.

This project implements all core requirements and some bonus tasks, including **handling of wide characters in principle**.

---

## Implementation Details

### Basic Operations

- Configurable initial width, height, and scrollback size.
- Setting and updating current attributes (foreground, background, style) for subsequent edits.
- Cursor movement (up/down/left/right) constrained within screen bounds.
- Writing text, inserting text lines (with wrapping), and filling lines with characters.
- Clearing the screen, scrollback, or both.
- Accessing characters, attributes, or lines from both screen and scrollback.

### Edge Cases Handling

All insertion and writing operations take **all characters into account**, including blank spaces. This ensures consistent rendering and proper cursor positioning.

Example of line insertion with blanks:

```text
Initial line: "Jet  " (width = 5)
Insert "Brains" after 'J':

line0 = "JBrai"
line1 = "nset_"
line2 = "_^   "    # '_' is blank space, '^' is cursor position

After executing writeText("Hey"):

line2 = "_Hey^"
```
### Content Access

- `getLine(row)` always returns the full line including trailing spaces.  
  To ignore trailing spaces, use `getLine(row).trimEnd()`.

- **Scrollback management**:  
  When the screen overflows, the oldest lines are moved to scrollback.  
  If scrollback is full, the oldest scrollback lines are discarded.

### Screen Resizing

- When resizing the screen, lines that no longer fit are pushed to scrollback.
- The screen content is updated according to the new dimensions while preserving as much content as possible.
- The cursor is moved to the same character it was pointing to before the resize, ensuring consistent editing and display.

### Design Decisions

- `copyAttributes()` returns a new object to avoid sharing references and prevent unintended modifications.
- Scrollback removes the oldest line when full to maintain the configured maximum size.
- `getLine(row)` returns the line with all blank spaces included to preserve the exact terminal representation.

### Testing

Comprehensive unit tests cover:

- Core operations (`write`, `insert`, `fill`, `clear`,`moving cursor in any direction`)
- Edge cases with blank spaces
- Scrollback behavior

Tests verify that all implemented buffer operations behave correctly and produce consistent results.

## Build & Run

This project uses **Gradle** as the build tool.

### Prerequisites
- Java JDK 17 or later installed
- Gradle installed (optional, can use the Gradle wrapper included in the project)

### Build the project

Run the following command in the project root folder:

- **Linux / Mac:**
```bash
./gradlew build
```

- **Windows(Command Prompt or PowerShell):**
```
gradlew.bat build
```

This will compile the Kotlin code and check for compilation errors.

### Run Test
To execute all unit tests:
- **Linux / Mac:**
```bash
./gradlew test
```

- **Windows(Command Prompt or PowerShell):**
```
gradlew.bat test
```

All implemented buffer operations are tested, including edge cases.

## Limitations

- Full support for wide characters is not implemented yet, but edge-case handling and strategy are described in the Future Improvements section.
- The buffer currently supports a subset of terminal colors (enum with a few colors) instead of the full 16 standard colors.
- There is no integration with a real shell — this is purely a data structure implementation.

## Future Improvements

Planned enhancements for wide character handling:

- Inserting wide characters at the end of a line
- Wrapping wide characters across multiple lines
- Cursor movement across wide characters
- Scrollback management for multi-cell characters
- Proper handling in trim, fill, and clear operations

**Author**: Aleksandar Savić  
**Github**: @acasavic06