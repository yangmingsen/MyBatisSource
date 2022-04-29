/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.plugin;

import java.util.Properties;

/**
 * @author Clinton Begin
 */
/**
 * 拦截器
 * 执行原理：
 *  1. 在做xml解析或其他(初始化)时,会有调用 ` configuration.addInterceptor(interceptorInstance);`比如XMLConfigBuider.java,
 *      这个过程会调用到InterceptorChain.addInterceptor(Interceptor),将拦截器add到 InterceptorChain的拦截列表中。
 *
 *  2. 在做Configuration做ParameterHandler,ResultSetHandler,StatementHandler,Executor初始化时会调用InterceptorChain.pluginAll(Target target),
 *      这个时候InterceptorChain会应用它所有的拦截链给当前target(实现是通过动态代理,调用Interceptor.plugin(target),
 *      在Mybaits官方提供的例子ExamplePlugin的plugin方法就是调用 Plugin.wrap(target, this)， 而Plugin.wrap就是创建动态代理). 那么以后当
 *       ParameterHandler,ResultSetHandler,StatementHandler,Executor中任意一个执行方法时就会调用动态代理的Plguin.invoke()方法,
 *       Plguin.invoke()主要业务逻辑就是调用Interceptor.intercept()方法。
 */
public interface Interceptor {

  //拦截  //jdk动态代码中的InvocationHandler.invoke()方法执行里，这个方法会被调用
  Object intercept(Invocation invocation) throws Throwable;

  //插入  //生成一个代理对象
  Object plugin(Object target);

  //设置属性
  void setProperties(Properties properties);

}
