package com.ettrema.pay;

/**
 *
 */
public class Result {
    /**
     * code identifying the result
     */
    public final String resultCode;
    /**
     * Text to explain to the user why the transaction failed
     *
     */
    public final String helpText;

    /**
     * True if successful
     */
    public final boolean isSuccess;

    /**
     * Unique reference to the successful transaction. Needed for refunds
     */
    public final String resultRef;

    public Result(String resultCode, String helpText, boolean isSuccess, String resultRef) {
        this.resultCode = resultCode;
        this.helpText = helpText;
        this.isSuccess = isSuccess;
        this.resultRef = resultRef;
    }

    
}
