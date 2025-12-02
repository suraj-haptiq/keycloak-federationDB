package com.rizky.keycloak.federationdb;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * responseCode unless otherwise defined, 0 success
 * used by createCustomerStoreDrfLink_wsvcClient() method of WebSvcClient class
 */
public abstract class AbstractResponse {
    public static Integer RC_OK = 0;

    protected Integer responseCode;
    protected String  responseDescription;
    protected String  exception_msg;
    protected String  stackTrace = "";

    private String  invalidFieldName = "";
    private String exceptionMessage = "";

    public Integer getResponseCode() {
        return responseCode;
    }
    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }
    public String getResponseDescription() {
        return responseDescription;
    }
    public void setResponseDescription(String responseDescription) {
        this.responseDescription = responseDescription;
    }
    public String getStackTrace() {
        return stackTrace;
    }
    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }
    public void setException(Exception e){
        stackTrace = ExceptionUtils.getStackTrace(e);
        exception_msg = e.getMessage();
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
            + ((exception_msg == null) ? 0 : exception_msg.hashCode());
        result = prime * result
            + ((responseCode == null) ? 0 : responseCode.hashCode());
        result = prime
            * result
            + ((responseDescription == null) ? 0 : responseDescription
            .hashCode());
        result = prime * result
            + ((stackTrace == null) ? 0 : stackTrace.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractResponse other = (AbstractResponse) obj;
        if (exception_msg == null) {
            if (other.exception_msg != null)
                return false;
        } else if (!exception_msg.equals(other.exception_msg))
            return false;
        if (responseCode == null) {
            if (other.responseCode != null)
                return false;
        } else if (!responseCode.equals(other.responseCode))
            return false;
        if (responseDescription == null) {
            if (other.responseDescription != null)
                return false;
        } else if (!responseDescription.equals(other.responseDescription))
            return false;
        if (stackTrace == null) {
            if (other.stackTrace != null)
                return false;
        } else if (!stackTrace.equals(other.stackTrace))
            return false;
        return true;
    }


    public String getException_msg() {
        return exception_msg;
    }
    public void setException_msg(String exception_msg) {
        this.exception_msg = exception_msg;
    }
    public String getInvalidFieldName() {
        return invalidFieldName;
    }
    public void setInvalidFieldName(String invalidFieldName) {
        this.invalidFieldName = invalidFieldName;
    }
    public String getExceptionMessage() {
        return exceptionMessage;
    }
    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    @Override
    public String toString() {
        return "AbstractResponse [responseCode=" + responseCode
            + ", responseDescription=" + responseDescription
            + ", exception_msg=" + exception_msg + ", stackTrace="
            + stackTrace + ", invalidFieldName=" + invalidFieldName
            + ", exceptionMessage=" + exceptionMessage + "]";
    }

}


