package error;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ErrorType {
    INVALID_SYMBOL,
    REDEFINED_IDENT,
    UNDEFINED_IDENT,
    FUNCPARAMS_NUM_UNMATCHED,
    FUNCPARAM_TYPE_UNMATCHED,
    VOIDFUNC_RETURN_EXP,
    INTFUNC_MISS_RETURN,
    CONSTANT_ASSIGNING,
    SEMICOLON_MISSING,
    RPARENT_MISSING,
    RBRACK_MISSING,
    FORMAT_CHAR_NUM_UNMATCHED,
    BREAK_CONTINUE_OUT_LOOP,
    UNDEFINED_ERROR,
    CORRECT;

    public static String error2type(ErrorType errortype) {
        Map<ErrorType, String> error2kind = Stream.of(new Object[][] {
                {INVALID_SYMBOL, "a"},
                {REDEFINED_IDENT,"b"},
                {UNDEFINED_IDENT, "c"},
                {FUNCPARAMS_NUM_UNMATCHED, "d"},
                {FUNCPARAM_TYPE_UNMATCHED, "e"},
                {VOIDFUNC_RETURN_EXP, "f"},
                {INTFUNC_MISS_RETURN, "g"},
                {CONSTANT_ASSIGNING, "h"},
                {SEMICOLON_MISSING, "i"},
                {RPARENT_MISSING, "j"},
                {RBRACK_MISSING, "k"},
                {FORMAT_CHAR_NUM_UNMATCHED, "l"},
                {BREAK_CONTINUE_OUT_LOOP, "m"},
                {UNDEFINED_ERROR, "n"}
        }).collect(Collectors.toMap(data -> (ErrorType) data[0], data -> (String) data[1]));
        return error2kind.get(errortype);
    }

}
