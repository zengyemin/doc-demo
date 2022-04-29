package com.doc.demo.exception;

public class DocParamException extends RuntimeException {

    private static final long serialVersionUID = -5084793305272426830L;
    private String message;


    public DocParamException(String message) {
        super(message, null, false, false);//关闭构造器中父类的栈写入
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 重写栈追踪，后续抛出异常时不会，打印方法栈调用链
     *
     * @return 返回当前对象 {@link DocParamException}
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
