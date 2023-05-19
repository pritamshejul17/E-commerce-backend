package bytecode.io.ecommerce.exception;

import javax.swing.*;

public class SpringECommerceException extends RuntimeException{
    public SpringECommerceException(String message, Exception ex) {
        super(message, ex);
    }
    public SpringECommerceException(String message) {
        super(message);
    }
}
