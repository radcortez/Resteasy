package org.jboss.resteasy.core;

import org.jboss.resteasy.spi.ApplicationException;
import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.InternalServerErrorException;
import org.jboss.resteasy.spi.MethodInjector;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.Types;

import javax.ws.rs.WebApplicationException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class MethodInjectorImpl implements MethodInjector
{
   protected Method method;
   protected Method invokedMethod;
   protected Class rootClass;
   protected ValueInjector[] params;
   protected ResteasyProviderFactory factory;

   public MethodInjectorImpl(Class root, Method method, ResteasyProviderFactory factory)
   {
      this.method = method;
      this.rootClass = root;

      // invokedMethod is for when the target object might be a proxy and
      // resteasy is getting the bean class to introspect.
      // An example is a proxied Spring bean that is a resource
      this.invokedMethod = findInterfaceBasedMethod(root, method);
      this.factory = factory;
      params = new ValueInjector[method.getParameterTypes().length];
      /*
          We get the genericParameterTypes for the case of:

          interface Foo<T> {
             @PUT
             void put(List<T> l);
          }

          public class FooImpl implements Foo<Customer> {
              public void put(List<Customer> l) {...}
          }
       */
      Type[] genericParameterTypes = Types.getGenericParameterTypesOfGenericInterfaceMethod(root, method);
      for (int i = 0; i < method.getParameterTypes().length; i++)
      {
         Class type = method.getParameterTypes()[i];
         Type genericType = genericParameterTypes[i];
         Annotation[] annotations = method.getParameterAnnotations()[i];
         params[i] = InjectorFactoryImpl.getParameterExtractor(root, method, type, genericType, annotations, factory);
      }
   }

   public static Method findInterfaceBasedMethod(Class root, Method method)
   {
      if (method.getDeclaringClass().isInterface() || root.isInterface()) return method;

      for (Class intf : root.getInterfaces())
      {
         try
         {
            return intf.getMethod(method.getName(), method.getParameterTypes());
         }
         catch (NoSuchMethodException ignored) {}
      }

      if (root.getSuperclass() == null || root.getSuperclass().equals(Object.class)) return method;
      return findInterfaceBasedMethod(root.getSuperclass(), method);

   }

   public Object[] injectArguments(HttpRequest input, HttpResponse response)
   {
      try
      {
         Object[] args = null;
         if (params != null && params.length > 0)
         {
            args = new Object[params.length];
            int i = 0;
            for (ValueInjector extractor : params)
            {
               args[i++] = extractor.inject(input, response);
            }
         }
         return args;
      }
      catch (WebApplicationException we)
      {
         throw we;
      }
      catch (Failure f)
      {
         throw f;
      }
      catch (Exception e)
      {
         BadRequestException badRequest = new BadRequestException("Failed processing arguments of " + method.toString(), e);
         badRequest.setLoggable(true);
         throw badRequest;
      }
   }

   public Object invoke(HttpRequest request, HttpResponse httpResponse, Object resource) throws Failure, ApplicationException, WebApplicationException
   {
      Object[] args = injectArguments(request, httpResponse);
      try
      {
         return invokedMethod.invoke(resource, args);
      }
      catch (IllegalAccessException e)
      {
         throw new InternalServerErrorException("Not allowed to reflect on method: " + method.toString(), e);
      }
      catch (InvocationTargetException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof WebApplicationException)
         {
            WebApplicationException wae = (WebApplicationException) cause;
            throw wae;
         }
         throw new ApplicationException(cause);
      }
      catch (IllegalArgumentException e)
      {
         String msg = "Bad arguments passed to " + method.toString() + "  (";
         if (args != null)
         {
            boolean first = false;
            for (Object arg : args)
            {
               if (!first)
               {
                  first = true;
               }
               else
               {
                  msg += ",";
               }
               if (arg == null)
               {
                  msg += " null";
                  continue;
               }
               msg += " " + arg.getClass().getName() + " " + arg;
            }
         }
         msg += " )";
         throw new InternalServerErrorException(msg, e);
      }
   }

}
