package org.jboss.resteasy.test.resource.param.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import org.jboss.resteasy.test.resource.param.HeaderParamsAsPrimitivesTest;
import org.junit.jupiter.api.Assertions;

@Path("/wrappers")
public class HeaderParamsAsPrimitivesResourceWrappers implements HeaderParamsAsPrimitivesWrappersProxy {
    @GET
    @Produces("application/boolean")
    public String doGet(@HeaderParam("boolean") Boolean v) {
        Assertions.assertEquals(true, v.booleanValue(), HeaderParamsAsPrimitivesTest.ERROR_MESSAGE);
        return "content";
    }

    @GET
    @Produces("application/byte")
    public String doGet(@HeaderParam("byte") Byte v) {
        Assertions.assertTrue(127 == v.byteValue(), HeaderParamsAsPrimitivesTest.ERROR_MESSAGE);

        return "content";
    }

    @GET
    @Produces("application/short")
    public String doGet(@HeaderParam("short") Short v) {
        Assertions.assertTrue(32767 == v.shortValue(), HeaderParamsAsPrimitivesTest.ERROR_MESSAGE);
        return "content";
    }

    @GET
    @Produces("application/int")
    public String doGet(@HeaderParam("int") Integer v) {
        Assertions.assertEquals(2147483647, v.intValue(), HeaderParamsAsPrimitivesTest.ERROR_MESSAGE);
        return "content";
    }

    @GET
    @Produces("application/long")
    public String doGet(@HeaderParam("long") Long v) {
        Assertions.assertEquals(9223372036854775807L, v.longValue(), HeaderParamsAsPrimitivesTest.ERROR_MESSAGE);
        return "content";
    }

    @GET
    @Produces("application/float")
    public String doGet(@HeaderParam("float") Float v) {
        Assertions.assertEquals(3.14159265f, v.floatValue(), 0.0f, HeaderParamsAsPrimitivesTest.ERROR_MESSAGE);
        return "content";
    }

    @GET
    @Produces("application/double")
    public String doGet(@HeaderParam("double") Double v) {
        Assertions.assertEquals(3.14159265358979d, v.doubleValue(), 0.0, HeaderParamsAsPrimitivesTest.ERROR_MESSAGE);
        return "content";
    }

    @GET
    @Produces("application/char")
    public String doGet(@HeaderParam("char") Character v) {
        Assertions.assertEquals('a', v.charValue(), HeaderParamsAsPrimitivesTest.ERROR_MESSAGE);
        return "content";
    }
}
