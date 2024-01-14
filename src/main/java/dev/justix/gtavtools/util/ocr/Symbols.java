package dev.justix.gtavtools.util.ocr;

import lombok.Getter;

@Getter
public enum Symbols {

    SPECIAL(':', '!', ',', '.', '-', '$', '%', '&'),
    NUMBERS('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'),
    LOWERCASE_LETTERS('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
             'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
             'u', 'v', 'w', 'x', 'y', 'z', 'ä', 'ö', 'ü'),
    UPPERCASE_LETTERS('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
             'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
             'U', 'V', 'W', 'X', 'Y', 'Z', 'Ä', 'Ö', 'Ü');

    private final char[] chars;

    Symbols(char... chars) {
        this.chars = chars;
    }

    @Override
    public String toString() {
        return new String(this.chars);
    }

}
