package com.ledgersense.importer;

// NEW: describes ONE bank's CSV layout. Columns are 0-based (first column = 0).
// Either amountColumn is set, OR both debitColumn and creditColumn are.
public record ColumnMapping(
        char delimiter,          // ',' or ';'
        int skipLines,           // junk lines above the header (account info, etc.)
        boolean hasHeader,
        String datePattern,      // e.g. "uuuu/MM/dd" (see note below)
        int dateColumn,
        int descriptionColumn,
        Integer amountColumn,    // Integer (not int) so it can be null
        Integer debitColumn,
        Integer creditColumn,
        Integer balanceColumn
){
    public ColumnMapping{
        if(amountColumn == null && (debitColumn == null || creditColumn == null)){
            throw new IllegalArgumentException("Mapping needs an amount column, or both debit and credit column");
        }
        if(datePattern == null || datePattern.isBlank()){
            throw new IllegalArgumentException("Mapping needs a date pattern");
        }
        if (skipLines < 0) {
            throw new IllegalArgumentException("skipLines can't be negative");
        }
    }public int maxColumnIndex(){
        int max = Math.max(dateColumn, descriptionColumn);
        for(Integer column : new Integer[]{amountColumn, debitColumn, creditColumn, balanceColumn}){
            if(column != null && column > max){
                max = column;
            }
        }
        return max;
    }
}
