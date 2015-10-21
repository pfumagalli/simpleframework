package org.simpleframework.http.message;

import org.simpleframework.http.Address;
import org.simpleframework.http.parse.AddressParser;

public class MockHeader extends RequestConsumer {

    private AddressParser parser;
    private final String address;

    public MockHeader(String address) {
        this.address = address;
    }
    @Override
    public Address getAddress() {
        if(parser == null) {
            parser = new AddressParser(address);
        }
        return parser;
    }


}
