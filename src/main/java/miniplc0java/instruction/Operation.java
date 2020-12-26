package miniplc0java.instruction;

public enum Operation {
    /**指令类型 */
    NOP,
    PUSH,
    POP,
    POPN,
    DUP,
    LOCA,
    ARGA,
    GLOBA,
    LOAD_8,
    LOAD_16,
    LOAD_32,
    LOAD_64,
    STORE_8,
    STORE_16,
    STORE_32,
    STORE_64,
    ALLOC,
    FREE,
    STACKALLOC,
    ADD_I,
    SUB_I,
    MUL_I,
    DIV_I,
    ADD_F,
    SUB_F,
    MUL_F,
    DIV_F,
    DIV_U,
    SHL,
    SHR,
    AND,
    OR,
    XOR,
    NOT,
    CMP_I,
    CMP_U,
    CMP_F,
    NEG_I,
    NEG_F,
    ITOF,
    FTOI,
    SHRL,
    SET_LT,
    SET_GT,
    BR,
    BR_FALSE,
    BR_TRUE,
    CALL,
    RET,
    CALLNAME,
    SCAN_I,
    SCAN_C,
    SCAN_F,
    PRINT_I,
    PRINT_C,
    PRINT_F,
    PRINT_S,
    PRINTLN,
    PANIC,
}
