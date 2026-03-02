package org.wildfly.extension.mcp.server;

import java.io.Serial;

class MCPException extends Exception {

    @Serial
    private static final long serialVersionUID = 3142589829095593984L;

    private final int jsonRpcError;

    MCPException(String message, Throwable cause, int jsonRpcError) {
        super(message, cause);
        this.jsonRpcError = jsonRpcError;
    }

    MCPException(String message, int jsonRpcError) {
        super(message);
        this.jsonRpcError = jsonRpcError;
    }

    int getJsonRpcError() {
        return jsonRpcError;
    }

}
