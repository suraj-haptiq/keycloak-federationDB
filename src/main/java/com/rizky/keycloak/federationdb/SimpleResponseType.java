package com.rizky.keycloak.federationdb;

public class SimpleResponseType extends AbstractResponse {

    protected String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "SimpleResponseType [id=" + id + ", responseCode="
            + responseCode + ", responseDescription=" + responseDescription
            + ", exception_msg=" + exception_msg + ", stackTrace="
            + stackTrace + "]";
    }

}
