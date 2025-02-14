package org.wildfly.extension.mcp.server;

class McpException extends Exception {

    private static final long serialVersionUID = 3142589829095593984L;

    private final int jsonRpcError;

    McpException(String message, Throwable cause, int jsonRpcError) {
        super(message, cause);
        this.jsonRpcError = jsonRpcError;
    }

    McpException(String message, int jsonRpcError) {
        super(message);
        this.jsonRpcError = jsonRpcError;
    }

    int getJsonRpcError() {
        return jsonRpcError;
    }

}
